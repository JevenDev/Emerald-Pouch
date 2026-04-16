package com.jvn.emeraldpouch.pouch;

import com.jvn.emeraldpouch.item.PouchItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;

public final class PouchData {
    public static final String POUCH_TAG = "EmeraldPouch";
    public static final String AUTO_COMPACT_TAG = "AutoCompact";
    public static final String AUTO_PICKUP_TAG = "AutoPickup";

    private PouchData() {
    }

    public static boolean isPouchStack(ItemStack stack) {
        return stack.getItem() instanceof PouchItem;
    }

    public static int getSlotCount(ItemStack pouchStack) {
        if (pouchStack.getItem() instanceof PouchItem pouchItem) {
            return pouchItem.slotCount();
        }
        return 0;
    }

    public static boolean isAllowedContent(ItemStack stack) {
        return stack.is(Items.EMERALD) || stack.is(Items.EMERALD_BLOCK);
    }

    public static NonNullList<ItemStack> loadContents(ItemStack pouchStack) {
        return loadContents(pouchStack, getSlotCount(pouchStack));
    }

    public static NonNullList<ItemStack> loadContents(ItemStack pouchStack, int slotCount) {
        NonNullList<ItemStack> items = NonNullList.withSize(slotCount, ItemStack.EMPTY);
        ItemContainerContents contents = pouchStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        int upperBound = Math.min(slotCount, contents.getSlots());

        for (int slot = 0; slot < upperBound; slot++) {
            items.set(slot, contents.getStackInSlot(slot));
        }

        return items;
    }

    public static void saveContents(ItemStack pouchStack, List<ItemStack> items) {
        List<ItemStack> copies = new ArrayList<>(items.size());
        boolean hasAny = false;

        for (ItemStack item : items) {
            ItemStack copy = item.copy();
            copies.add(copy);
            hasAny |= !copy.isEmpty();
        }

        if (!hasAny) {
            pouchStack.remove(DataComponents.CONTAINER);
            return;
        }

        pouchStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(copies));
    }

    public static boolean isAutoCompactEnabled(ItemStack pouchStack) {
        return readToggle(pouchStack, AUTO_COMPACT_TAG);
    }

    public static boolean isAutoPickupEnabled(ItemStack pouchStack) {
        return readToggle(pouchStack, AUTO_PICKUP_TAG);
    }

    public static void setAutoCompactEnabled(ItemStack pouchStack, boolean enabled) {
        writeToggle(pouchStack, AUTO_COMPACT_TAG, enabled);
    }

    public static void setAutoPickupEnabled(ItemStack pouchStack, boolean enabled) {
        writeToggle(pouchStack, AUTO_PICKUP_TAG, enabled);
    }

    public static void toggleAutoCompact(ItemStack pouchStack) {
        setAutoCompactEnabled(pouchStack, !isAutoCompactEnabled(pouchStack));
    }

    public static void toggleAutoPickup(ItemStack pouchStack) {
        setAutoPickupEnabled(pouchStack, !isAutoPickupEnabled(pouchStack));
    }

    public static ItemStack insertIntoPouch(ItemStack pouchStack, ItemStack incoming, int maxToInsert) {
        if (incoming.isEmpty() || maxToInsert <= 0 || !isPouchStack(pouchStack) || !isAllowedContent(incoming)) {
            return incoming.copy();
        }

        int slotCount = getSlotCount(pouchStack);
        if (slotCount <= 0) {
            return incoming.copy();
        }

        NonNullList<ItemStack> contents = loadContents(pouchStack, slotCount);
        int targetInsert = Math.min(maxToInsert, incoming.getCount());
        int remaining = targetInsert;

        for (int slot = 0; slot < slotCount && remaining > 0; slot++) {
            ItemStack current = contents.get(slot);
            if (current.isEmpty() || !ItemStack.isSameItemSameComponents(current, incoming)) {
                continue;
            }

            int maxStack = Math.min(current.getMaxStackSize(), contents.get(slot).getMaxStackSize());
            int move = Math.min(remaining, maxStack - current.getCount());
            if (move <= 0) {
                continue;
            }

            current.grow(move);
            remaining -= move;
        }

        for (int slot = 0; slot < slotCount && remaining > 0; slot++) {
            if (!contents.get(slot).isEmpty()) {
                continue;
            }

            int move = Math.min(remaining, incoming.getMaxStackSize());
            contents.set(slot, incoming.copyWithCount(move));
            remaining -= move;
        }

        int inserted = targetInsert - remaining;
        if (inserted > 0) {
            saveContents(pouchStack, contents);
        }

        ItemStack remainder = incoming.copy();
        remainder.shrink(inserted);
        return remainder;
    }

    private static boolean readToggle(ItemStack pouchStack, String key) {
        CustomData customData = pouchStack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return false;
        }

        CompoundTag root = customData.copyTag();
        if (!root.contains(POUCH_TAG, Tag.TAG_COMPOUND)) {
            return false;
        }

        return root.getCompound(POUCH_TAG).getBoolean(key);
    }

    private static void writeToggle(ItemStack pouchStack, String key, boolean enabled) {
        CustomData.update(DataComponents.CUSTOM_DATA, pouchStack, customData -> {
            CompoundTag pouchTag = customData.contains(POUCH_TAG, Tag.TAG_COMPOUND) ? customData.getCompound(POUCH_TAG) : new CompoundTag();

            if (enabled) {
                pouchTag.putBoolean(key, true);
            } else {
                pouchTag.remove(key);
            }

            if (pouchTag.isEmpty()) {
                customData.remove(POUCH_TAG);
            } else {
                customData.put(POUCH_TAG, pouchTag);
            }
        });
    }
}
