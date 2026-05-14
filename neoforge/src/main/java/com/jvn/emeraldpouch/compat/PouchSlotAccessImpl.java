package com.jvn.emeraldpouch.compat;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class PouchSlotAccessImpl {
    private PouchSlotAccessImpl() {
    }

    public static int getAccessoriesBeltSlotCount(Player player) {
        return AccessoriesCompat.getBeltSlotCount(player);
    }

    public static ItemStack getAccessoriesBeltStack(Player player, int slot) {
        return AccessoriesCompat.getBeltStackInSlot(player, slot);
    }

    public static void setAccessoriesBeltStack(Player player, int slot, ItemStack stack) {
        AccessoriesCompat.setBeltStackInSlot(player, slot, stack);
    }

    public static int getCuriosSlotCount(Player player) {
        return CuriosCompat.getSlotCount(player);
    }

    public static ItemStack getCuriosStack(Player player, int slot) {
        return CuriosCompat.getStackInSlot(player, slot);
    }

    public static void setCuriosStack(Player player, int slot, ItemStack stack) {
        CuriosCompat.setStackInSlot(player, slot, stack);
    }
}