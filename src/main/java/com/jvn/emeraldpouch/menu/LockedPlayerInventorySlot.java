package com.jvn.emeraldpouch.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class LockedPlayerInventorySlot extends Slot {
    private final int lockedInventorySlot;

    public LockedPlayerInventorySlot(Container container, int slot, int x, int y, int lockedInventorySlot) {
        super(container, slot, x, y);
        this.lockedInventorySlot = lockedInventorySlot;
    }

    @Override
    public boolean mayPickup(Player player) {
        return getContainerSlot() != lockedInventorySlot && super.mayPickup(player);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return getContainerSlot() != lockedInventorySlot && super.mayPlace(stack);
    }
}
