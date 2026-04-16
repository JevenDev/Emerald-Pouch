package com.jvn.emeraldpouch.pouch;

import com.jvn.emeraldpouch.compat.AccessoriesCompat;
import com.jvn.emeraldpouch.compat.CuriosCompat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

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

    public static ItemStack extractMatching(Inventory inventory, ItemStack matcher, int maxCount) {
        if (maxCount <= 0 || matcher.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int extracted = 0;
        for (int slot : getDeterministicInventorySlots()) {
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

        if (extracted <= 0) {
            return ItemStack.EMPTY;
        }

        inventory.setChanged();
        return matcher.copyWithCount(extracted);
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

    public static List<Integer> getDeterministicInventorySlots() {
        List<Integer> slots = new ArrayList<>(Inventory.INVENTORY_SIZE + 1);
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            slots.add(slot);
        }
        slots.add(Inventory.SLOT_OFFHAND);
        return slots;
    }

    public static List<PouchStackReference> getDeterministicPouchReferences(Inventory inventory) {
        List<PouchStackReference> references = new ArrayList<>();

        // Prioritize equipped slot pouches over inventory pouches.
        int accessoriesBeltSlots = AccessoriesCompat.getBeltSlotCount(inventory.player);
        for (int slot = 0; slot < accessoriesBeltSlots; slot++) {
            if (PouchData.isPouchStack(AccessoriesCompat.getBeltStackInSlot(inventory.player, slot))) {
                references.add(PouchStackReference.accessoriesBelt(slot));
            }
        }

        int curiosSlots = CuriosCompat.getSlotCount(inventory.player);
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
}
