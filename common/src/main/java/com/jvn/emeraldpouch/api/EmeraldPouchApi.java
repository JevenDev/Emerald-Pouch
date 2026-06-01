package com.jvn.emeraldpouch.api;

import com.jvn.emeraldpouch.pouch.PouchData;
import com.jvn.emeraldpouch.pouch.PouchInventoryAccess;
import com.jvn.emeraldpouch.pouch.PouchStackReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Public helper API for reading and updating Emerald Pouch contents from other mods.
 */
public final class EmeraldPouchApi {
    private EmeraldPouchApi() {
    }

    /**
     * Returns true when the given stack is an Emerald Pouch item.
     */
    public static boolean isPouch(ItemStack stack) {
        return PouchData.isPouchStack(stack);
    }

    /**
     * Returns true when the given stack is valid pouch slot content.
     */
    public static boolean isAllowedContent(ItemStack stack) {
        return PouchData.isAllowedContent(stack);
    }

    /**
     * Returns the number of storage slots available in the pouch stack.
     */
    public static int getSlotCount(ItemStack pouchStack) {
        return PouchData.getSlotCount(pouchStack);
    }

    /**
     * Returns a copy of the item in the requested pouch slot, or {@link ItemStack#EMPTY} if invalid.
     */
    public static ItemStack getSlot(ItemStack pouchStack, int slot) {
        int slotCount = PouchData.getSlotCount(pouchStack);
        if (slot < 0 || slot >= slotCount) {
            return ItemStack.EMPTY;
        }

        return PouchData.loadContents(pouchStack, slotCount).get(slot).copy();
    }

    /**
     * Returns an immutable list of copied pouch slot contents.
     */
    public static List<ItemStack> getContents(ItemStack pouchStack) {
        NonNullList<ItemStack> contents = PouchData.loadContents(pouchStack);
        List<ItemStack> copies = new ArrayList<>(contents.size());
        for (ItemStack stack : contents) {
            copies.add(stack.copy());
        }
        return Collections.unmodifiableList(copies);
    }

    /**
     * Replaces a single pouch slot with the given stack.
     */
    public static boolean setSlot(ItemStack pouchStack, int slot, ItemStack stack) {
        int slotCount = PouchData.getSlotCount(pouchStack);
        if (slot < 0 || slot >= slotCount) {
            return false;
        }

        ItemStack normalized = normalizeContentStack(stack);
        if (!stack.isEmpty() && normalized.isEmpty()) {
            return false;
        }

        NonNullList<ItemStack> contents = PouchData.loadContents(pouchStack, slotCount);
        contents.set(slot, normalized);
        PouchData.saveContents(pouchStack, contents);
        return true;
    }

    /**
     * Replaces all pouch contents. The provided list may be smaller than the pouch size; missing slots are emptied.
     */
    public static boolean setContents(ItemStack pouchStack, List<ItemStack> contents) {
        int slotCount = PouchData.getSlotCount(pouchStack);
        if (contents.size() > slotCount) {
            return false;
        }

        NonNullList<ItemStack> normalizedContents = NonNullList.withSize(slotCount, ItemStack.EMPTY);
        for (int slot = 0; slot < contents.size(); slot++) {
            ItemStack normalized = normalizeContentStack(contents.get(slot));
            if (!contents.get(slot).isEmpty() && normalized.isEmpty()) {
                return false;
            }
            normalizedContents.set(slot, normalized);
        }

        PouchData.saveContents(pouchStack, normalizedContents);
        return true;
    }

    /**
     * Inserts as much of the incoming stack as possible into the pouch and returns the remainder.
     */
    public static ItemStack insert(ItemStack pouchStack, ItemStack incoming, int maxToInsert) {
        return PouchData.insertIntoPouch(pouchStack, incoming, maxToInsert);
    }

    /**
     * Returns the pouch's total emerald value, counting emerald blocks as nine emeralds.
     */
    public static int getStoredEmeraldEquivalent(ItemStack pouchStack) {
        return PouchData.getStoredEmeraldEquivalent(pouchStack);
    }

    /**
     * Returns deterministic pouch references in the same priority order used by Emerald Pouch itself.
     */
    public static List<PlayerPouchReference> getPlayerPouches(Inventory inventory) {
        List<PouchStackReference> references = PouchInventoryAccess.getDeterministicPouchReferences(inventory);
        List<PlayerPouchReference> apiReferences = new ArrayList<>(references.size());
        for (PouchStackReference reference : references) {
            apiReferences.add(fromInternal(reference));
        }
        return Collections.unmodifiableList(apiReferences);
    }

    /**
     * Returns the first available pouch reference in the player's deterministic pouch order.
     */
    public static Optional<PlayerPouchReference> findFirstPlayerPouch(Inventory inventory) {
        return PouchInventoryAccess.findFirstPouch(inventory).map(EmeraldPouchApi::fromInternal);
    }

    /**
     * Returns a copy of the pouch stack for the given player pouch reference.
     */
    public static ItemStack getPlayerPouchStack(Inventory inventory, PlayerPouchReference reference) {
        return PouchInventoryAccess.getPouchStack(inventory, toInternal(reference)).copy();
    }

    /**
     * Replaces the stored pouch stack at the given player pouch reference.
     */
    public static void commitPlayerPouchStack(Inventory inventory, PlayerPouchReference reference, ItemStack pouchStack) {
        PouchInventoryAccess.commitPouchStack(inventory, toInternal(reference), pouchStack);
    }

    /**
     * Replaces a single slot inside the referenced player pouch and commits the updated pouch stack.
     */
    public static boolean setPlayerPouchSlot(Inventory inventory, PlayerPouchReference reference, int slot, ItemStack stack) {
        ItemStack pouchStack = PouchInventoryAccess.getPouchStack(inventory, toInternal(reference)).copy();
        if (!setSlot(pouchStack, slot, stack)) {
            return false;
        }

        PouchInventoryAccess.commitPouchStack(inventory, toInternal(reference), pouchStack);
        return true;
    }

    /**
     * Replaces all slot contents inside the referenced player pouch and commits the updated pouch stack.
     */
    public static boolean setPlayerPouchContents(Inventory inventory, PlayerPouchReference reference, List<ItemStack> contents) {
        ItemStack pouchStack = PouchInventoryAccess.getPouchStack(inventory, toInternal(reference)).copy();
        if (!setContents(pouchStack, contents)) {
            return false;
        }

        PouchInventoryAccess.commitPouchStack(inventory, toInternal(reference), pouchStack);
        return true;
    }

    /**
     * Inserts as much of the incoming stack as possible into the referenced player pouch and commits the result.
     */
    public static ItemStack insertIntoPlayerPouch(
            Inventory inventory,
            PlayerPouchReference reference,
            ItemStack incoming,
            int maxToInsert
    ) {
        ItemStack pouchStack = PouchInventoryAccess.getPouchStack(inventory, toInternal(reference)).copy();
        ItemStack remainder = PouchData.insertIntoPouch(pouchStack, incoming, maxToInsert);
        PouchInventoryAccess.commitPouchStack(inventory, toInternal(reference), pouchStack);
        return remainder;
    }

    /**
     * Inserts into the player's pouches using Emerald Pouch's normal deterministic priority order.
     */
    public static ItemStack insertIntoPlayerPouches(Inventory inventory, ItemStack stack) {
        return PouchInventoryAccess.insertIntoPouches(inventory, stack);
    }

    /**
     * Inserts only into player pouches that have auto-pickup enabled.
     */
    public static ItemStack insertIntoAutoPickupPouches(Inventory inventory, ItemStack stack) {
        return PouchInventoryAccess.insertIntoAutoPickupPouches(inventory, stack);
    }

    private static ItemStack normalizeContentStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (!PouchData.isAllowedContent(stack)) {
            return ItemStack.EMPTY;
        }

        int clampedCount = Math.min(stack.getCount(), stack.getMaxStackSize());
        return stack.copyWithCount(clampedCount);
    }

    private static PlayerPouchReference fromInternal(PouchStackReference reference) {
        return new PlayerPouchReference(
                switch (reference.type()) {
                    case INVENTORY -> PlayerPouchReference.Type.INVENTORY;
                    case CURIOS -> PlayerPouchReference.Type.CURIOS;
                    case ACCESSORIES_BELT -> PlayerPouchReference.Type.ACCESSORIES_BELT;
                },
                reference.slot()
        );
    }

    private static PouchStackReference toInternal(PlayerPouchReference reference) {
        return switch (reference.type()) {
            case INVENTORY -> PouchStackReference.inventory(reference.slot());
            case CURIOS -> PouchStackReference.curios(reference.slot());
            case ACCESSORIES_BELT -> PouchStackReference.accessoriesBelt(reference.slot());
        };
    }
}
