package com.jvn.emeraldpouch.compat;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class CuriosCompat {
    private CuriosCompat() {
    }

    public static int getSlotCount(LivingEntity entity) {
        return 0;
    }

    public static ItemStack getStackInSlot(LivingEntity entity, int slot) {
        return ItemStack.EMPTY;
    }

    public static void setStackInSlot(LivingEntity entity, int slot, ItemStack stack) {
    }

    public static boolean tryEquipFromHand(Player player, InteractionHand hand) {
        return false;
    }
}
