package com.jvn.emeraldpouch.pouch;

import com.jvn.emeraldpouch.compat.AccessoriesCompat;
import com.jvn.emeraldpouch.compat.CuriosCompat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class PouchInventoryAccess {
    private PouchInventoryAccess() {
    }

    public static Optional<PouchStackReference> findFirstPouch(Inventory inventory) {
        for (PouchStackReference reference : getDeterministicPouchReferences(inventory)) {
            if (PouchData.isPouchStack(getPouchStack(inventory, reference))) {
                return Optional.of(reference);
            }
        }

        return Optional.empty();
    }

    public static boolean hasAutoPickupPouch(Inventory inventory) {
        for (PouchStackReference reference : getDeterministicPouchReferences(inventory)) {
            if (PouchData.isAutoPickupEnabled(getPouchStack(inventory, reference))) {
                return true;
            }
        }

        return false;
    }

    public static ItemStack insertIntoAutoPickupPouches(Inventory inventory, ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack remainder = stack.copy();
        for (PouchStackReference reference : getDeterministicPouchReferences(inventory)) {
            ItemStack pouchStack = getPouchStack(inventory, reference);
            if (!PouchData.isAutoPickupEnabled(pouchStack)) {
                continue;
            }

            int previousCount = remainder.getCount();
            remainder = PouchData.insertIntoPouch(pouchStack, remainder, remainder.getCount());
            if (remainder.getCount() != previousCount) {
                commitPouchStack(inventory, reference, pouchStack);
            }
            if (remainder.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }

        return remainder;
    }

    public static ItemStack insertIntoPouches(Inventory inventory, ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        return insertIntoPouches(inventory, getDeterministicPouchReferences(inventory), stack, 0);
    }

    public static ItemStack extractMatching(Inventory inventory, ItemStack matcher, int maxCount) {
        if (maxCount <= 0 || matcher.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int extracted = 0;
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            if (extracted >= maxCount) {
                break;
            }

            ItemStack stack = inventory.getItem(slot);
            if (!ItemStack.isSameItemSameComponents(stack, matcher)) {
                continue;
            }

            int take = Math.min(maxCount - extracted, stack.getCount());
            stack.shrink(take);
            if (stack.isEmpty()) {
                inventory.setItem(slot, ItemStack.EMPTY);
            }
            extracted += take;
        }

        if (extracted < maxCount) {
            ItemStack offhand = inventory.getItem(Inventory.SLOT_OFFHAND);
            if (ItemStack.isSameItemSameComponents(offhand, matcher)) {
                int take = Math.min(maxCount - extracted, offhand.getCount());
                offhand.shrink(take);
                if (offhand.isEmpty()) {
                    inventory.setItem(Inventory.SLOT_OFFHAND, ItemStack.EMPTY);
                }
                extracted += take;
            }
        }

        if (extracted <= 0) {
            return ItemStack.EMPTY;
        }

        inventory.setChanged();
        return matcher.copyWithCount(extracted);
    }

    public static ItemStack extractEmeraldsForTrade(Inventory inventory, int maxCount) {
        if (maxCount <= 0) {
            return ItemStack.EMPTY;
        }

        List<PouchStackReference> references = getDeterministicPouchReferences(inventory);
        List<TradePouchState> tradePouches = collectTradePouches(inventory, references, maxCount);
        if (tradePouches.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int remaining = maxCount;
        for (TradePouchState tradePouch : tradePouches) {
            remaining = removeLooseEmeralds(tradePouch.contents(), remaining);
            if (remaining <= 0) {
                break;
            }
        }

        int overflowStartIndex = tradePouches.getLast().referenceIndex();
        if (remaining > 0) {
            for (TradePouchState tradePouch : tradePouches) {
                int previousRemaining = remaining;
                remaining = removeEmeraldBlocks(tradePouch.contents(), remaining);
                if (remaining != previousRemaining) {
                    overflowStartIndex = tradePouch.referenceIndex();
                }
                if (remaining <= 0) {
                    break;
                }
            }
        }

        if (remaining > 0) {
            return ItemStack.EMPTY;
        }

        for (TradePouchState tradePouch : tradePouches) {
            PouchData.saveContents(tradePouch.pouchStack(), tradePouch.contents());
            commitPouchStack(inventory, tradePouch.reference(), tradePouch.pouchStack());
        }

        int overflowEmeralds = -remaining;
        if (overflowEmeralds > 0) {
            ItemStack overflow = new ItemStack(Items.EMERALD, overflowEmeralds);
            overflow = insertIntoPouches(inventory, references, overflow, overflowStartIndex);
            if (!overflow.isEmpty()) {
                inventory.placeItemBackInInventory(overflow);
            }
        }

        return new ItemStack(Items.EMERALD, maxCount);
    }

    public static void clearOpenedVisualFlags(Inventory inventory) {
        for (PouchStackReference reference : getDeterministicPouchReferences(inventory)) {
            ItemStack pouchStack = getPouchStack(inventory, reference);
            if (PouchData.isOpenedVisualEnabled(pouchStack)) {
                PouchData.setOpenedVisualEnabled(pouchStack, false);
                commitPouchStack(inventory, reference, pouchStack);
            }
        }
    }

    public static List<PouchStackReference> getDeterministicPouchReferences(Inventory inventory) {
        int accessoriesBeltSlots = AccessoriesCompat.getBeltSlotCount(inventory.player);
        int curiosSlots = CuriosCompat.getSlotCount(inventory.player);
        List<PouchStackReference> references = new ArrayList<>(accessoriesBeltSlots + curiosSlots + Inventory.INVENTORY_SIZE + 1);

        // Prioritize equipped slot pouches over inventory pouches.
        for (int slot = 0; slot < accessoriesBeltSlots; slot++) {
            if (PouchData.isPouchStack(AccessoriesCompat.getBeltStackInSlot(inventory.player, slot))) {
                references.add(PouchStackReference.accessoriesBelt(slot));
            }
        }

        for (int slot = 0; slot < curiosSlots; slot++) {
            if (PouchData.isPouchStack(CuriosCompat.getStackInSlot(inventory.player, slot))) {
                references.add(PouchStackReference.curios(slot));
            }
        }

        if (PouchData.isPouchStack(inventory.getItem(Inventory.SLOT_OFFHAND))) {
            references.add(PouchStackReference.inventory(Inventory.SLOT_OFFHAND));
        }

        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            if (PouchData.isPouchStack(inventory.getItem(slot))) {
                references.add(PouchStackReference.inventory(slot));
            }
        }

        return references;
    }

    public static ItemStack getPouchStack(Inventory inventory, PouchStackReference reference) {
        if (reference.type() == PouchStackReference.Type.INVENTORY) {
            int slot = reference.slot();
            if (slot < 0 || slot >= inventory.getContainerSize()) {
                return ItemStack.EMPTY;
            }
            return inventory.getItem(slot);
        }

        if (reference.type() == PouchStackReference.Type.ACCESSORIES_BELT) {
            return AccessoriesCompat.getBeltStackInSlot(inventory.player, reference.slot());
        }

        return CuriosCompat.getStackInSlot(inventory.player, reference.slot());
    }

    public static void commitPouchStack(Inventory inventory, PouchStackReference reference, ItemStack stack) {
        if (reference.type() == PouchStackReference.Type.INVENTORY) {
            int slot = reference.slot();
            if (slot < 0 || slot >= inventory.getContainerSize()) {
                return;
            }

            inventory.setItem(slot, stack);
            inventory.setChanged();
            return;
        }

        if (reference.type() == PouchStackReference.Type.ACCESSORIES_BELT) {
            AccessoriesCompat.setBeltStackInSlot(inventory.player, reference.slot(), stack);
        } else {
            CuriosCompat.setStackInSlot(inventory.player, reference.slot(), stack);
        }
        inventory.setChanged();
    }

    private static List<TradePouchState> collectTradePouches(
            Inventory inventory,
            List<PouchStackReference> references,
            int requiredEmeralds
    ) {
        List<TradePouchState> tradePouches = new ArrayList<>();
        int availableEmeralds = 0;

        for (int index = 0; index < references.size() && availableEmeralds < requiredEmeralds; index++) {
            PouchStackReference reference = references.get(index);
            ItemStack pouchStack = getPouchStack(inventory, reference);
            if (!PouchData.isPouchStack(pouchStack)) {
                continue;
            }

            int storedEmeralds = PouchData.getStoredEmeraldEquivalent(pouchStack);
            if (storedEmeralds <= 0) {
                continue;
            }

            tradePouches.add(new TradePouchState(index, reference, pouchStack, PouchData.loadContents(pouchStack)));
            availableEmeralds += storedEmeralds;
        }

        if (availableEmeralds < requiredEmeralds) {
            return List.of();
        }

        return tradePouches;
    }

    private static int removeLooseEmeralds(NonNullList<ItemStack> contents, int remaining) {
        for (int slot = 0; slot < contents.size() && remaining > 0; slot++) {
            ItemStack stack = contents.get(slot);
            if (!stack.is(Items.EMERALD)) {
                continue;
            }

            int taken = Math.min(remaining, stack.getCount());
            stack.shrink(taken);
            remaining -= taken;
            if (stack.isEmpty()) {
                contents.set(slot, ItemStack.EMPTY);
            }
        }

        return remaining;
    }

    private static int removeEmeraldBlocks(NonNullList<ItemStack> contents, int remaining) {
        for (int slot = 0; slot < contents.size() && remaining > 0; slot++) {
            ItemStack stack = contents.get(slot);
            if (!stack.is(Items.EMERALD_BLOCK)) {
                continue;
            }

            int blocksNeeded = (remaining + 8) / 9;
            int taken = Math.min(blocksNeeded, stack.getCount());
            stack.shrink(taken);
            remaining -= taken * 9;
            if (stack.isEmpty()) {
                contents.set(slot, ItemStack.EMPTY);
            }
        }

        return remaining;
    }

    private static ItemStack insertIntoPouches(
            Inventory inventory,
            List<PouchStackReference> references,
            ItemStack stack,
            int startIndex
    ) {
        if (stack.isEmpty() || references.isEmpty()) {
            return stack;
        }

        ItemStack remainder = stack.copy();
        int normalizedStart = Math.floorMod(startIndex, references.size());
        for (int offset = 0; offset < references.size() && !remainder.isEmpty(); offset++) {
            PouchStackReference reference = references.get((normalizedStart + offset) % references.size());
            ItemStack pouchStack = getPouchStack(inventory, reference);
            if (!PouchData.isPouchStack(pouchStack)) {
                continue;
            }

            int previousCount = remainder.getCount();
            remainder = PouchData.insertIntoPouch(pouchStack, remainder, remainder.getCount());
            if (remainder.getCount() != previousCount) {
                commitPouchStack(inventory, reference, pouchStack);
            }
        }

        return remainder;
    }

    private record TradePouchState(
            int referenceIndex,
            PouchStackReference reference,
            ItemStack pouchStack,
            NonNullList<ItemStack> contents
    ) {
    }
}
