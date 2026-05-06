package com.jvn.emeraldpouch.util;

import net.minecraft.world.item.ItemStack;

public final class StackHelper {
    private StackHelper() {
    }

    public static boolean sameItemData(ItemStack first, ItemStack second) {
        return ItemStack.isSameItemSameTags(first, second);
    }

    public static ItemStack copyWithCount(ItemStack stack, int count) {
        ItemStack copy = stack.copy();
        copy.setCount(count);
        return copy;
    }
}
