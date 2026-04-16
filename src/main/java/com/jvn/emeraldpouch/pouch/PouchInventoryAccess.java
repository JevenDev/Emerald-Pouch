package com.jvn.emeraldpouch.pouch;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class PouchInventoryAccess {
    private PouchInventoryAccess() {
    }

    public static int findFirstPouchSlot(Inventory inventory) {
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            if (PouchData.isPouchStack(inventory.getItem(slot))) {
                return slot;
            }
        }

        if (PouchData.isPouchStack(inventory.getItem(Inventory.SLOT_OFFHAND))) {
            return Inventory.SLOT_OFFHAND;
        }

        return -1;
    }

    public static boolean hasAutoPickupPouch(Inventory inventory) {
        for (int pouchSlot : getDeterministicPouchSlots(inventory)) {
            if (PouchData.isAutoPickupEnabled(inventory.getItem(pouchSlot))) {
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
        for (int pouchSlot : getDeterministicPouchSlots(inventory)) {
            ItemStack pouchStack = inventory.getItem(pouchSlot);
            if (!PouchData.isAutoPickupEnabled(pouchStack)) {
                continue;
            }

            remainder = PouchData.insertIntoPouch(pouchStack, remainder, remainder.getCount());
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

    public static List<Integer> getDeterministicPouchSlots(Inventory inventory) {
        List<Integer> slots = new ArrayList<>();

        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            if (PouchData.isPouchStack(inventory.getItem(slot))) {
                slots.add(slot);
            }
        }

        if (PouchData.isPouchStack(inventory.getItem(Inventory.SLOT_OFFHAND))) {
            slots.add(Inventory.SLOT_OFFHAND);
        }

        return slots;
    }

    public static List<Integer> getDeterministicInventorySlots() {
        List<Integer> slots = new ArrayList<>(Inventory.INVENTORY_SIZE + 1);
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            slots.add(slot);
        }
        slots.add(Inventory.SLOT_OFFHAND);
        return slots;
    }
}
