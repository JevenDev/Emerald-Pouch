package com.jvn.emeraldpouch.pouch;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PouchItemContainer implements Container {
    private final ItemStack pouchStack;
    private final int slotCount;
    private final NonNullList<ItemStack> items;
    private Runnable changeListener = () -> {
    };

    public PouchItemContainer(ItemStack pouchStack, int slotCount) {
        this.pouchStack = pouchStack;
        this.slotCount = Math.max(slotCount, 0);
        this.items = PouchData.loadContents(pouchStack, this.slotCount);
    }

    public void setChangeListener(Runnable listener) {
        this.changeListener = listener;
    }

    @Override
    public int getContainerSize() {
        return slotCount;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack item : items) {
            if (!item.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return isSlotValid(slot) ? items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (!isSlotValid(slot)) {
            return ItemStack.EMPTY;
        }

        ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
        if (!removed.isEmpty()) {
            setChanged();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (!isSlotValid(slot)) {
            return ItemStack.EMPTY;
        }

        ItemStack existing = items.get(slot);
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }

        items.set(slot, ItemStack.EMPTY);
        return existing;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (!isSlotValid(slot)) {
            return;
        }

        if (!stack.isEmpty() && !PouchData.isAllowedContent(stack)) {
            return;
        }

        items.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public void setChanged() {
        if (PouchData.isAutoCompactEnabled(this.pouchStack)) {
            PouchData.compactContents(this.items);
        }
        PouchData.saveContents(this.pouchStack, this.items);
        this.changeListener.run();
    }

    @Override
    public boolean stillValid(Player player) {
        return PouchData.isPouchStack(this.pouchStack);
    }

    @Override
    public void clearContent() {
        for (int slot = 0; slot < slotCount; slot++) {
            items.set(slot, ItemStack.EMPTY);
        }
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return isSlotValid(slot) && PouchData.isAllowedContent(stack);
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    public ItemStack insert(ItemStack stack, int maxToInsert) {
        if (stack.isEmpty() || maxToInsert <= 0 || !PouchData.isAllowedContent(stack)) {
            return stack.copy();
        }

        int targetInsert = Math.min(maxToInsert, stack.getCount());
        int remaining = targetInsert;

        for (int slot = 0; slot < slotCount && remaining > 0; slot++) {
            ItemStack current = items.get(slot);
            if (current.isEmpty() || !ItemStack.isSameItemSameComponents(current, stack)) {
                continue;
            }

            int move = Math.min(remaining, current.getMaxStackSize() - current.getCount());
            if (move <= 0) {
                continue;
            }

            current.grow(move);
            remaining -= move;
        }

        for (int slot = 0; slot < slotCount && remaining > 0; slot++) {
            if (!items.get(slot).isEmpty()) {
                continue;
            }

            int move = Math.min(remaining, stack.getMaxStackSize());
            items.set(slot, stack.copyWithCount(move));
            remaining -= move;
        }

        int inserted = targetInsert - remaining;
        if (inserted > 0) {
            setChanged();
        }

        ItemStack remainder = stack.copy();
        remainder.shrink(inserted);
        return remainder;
    }

    private boolean isSlotValid(int slot) {
        return slot >= 0 && slot < slotCount;
    }
}
