package com.jvn.emeraldpouch.compat;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class PouchSlotAccess {
    private PouchSlotAccess() {
    }

    @ExpectPlatform
    public static int getAccessoriesBeltSlotCount(Player player) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static ItemStack getAccessoriesBeltStack(Player player, int slot) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void setAccessoriesBeltStack(Player player, int slot, ItemStack stack) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int getCuriosSlotCount(Player player) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static ItemStack getCuriosStack(Player player, int slot) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void setCuriosStack(Player player, int slot, ItemStack stack) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean tryEquipFromHand(Player player, InteractionHand hand) {
        throw new AssertionError();
    }
}