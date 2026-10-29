package com.jvn.emeraldpouch.pouch;

import com.jvn.emeraldpouch.config.EmeraldPouchServerConfig;
import com.jvn.emeraldpouch.item.PouchItem;
import com.jvn.emeraldpouch.registry.ModEnchantments;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.player.Inventory;

public final class PouchDeathRetention {
    private PouchDeathRetention() {
    }

    public static boolean shouldKeep(ItemStack stack) {
        return stack.getItem() instanceof PouchItem
                && !EnchantmentHelper.hasVanishingCurse(stack)
                && (EmeraldPouchServerConfig.keepOnDeath()
                        || EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.SOULBOUND.get(), stack) > 0);
    }

    public static void restoreInventory(Inventory original, Inventory replacement) {
        for (int slot = 0; slot < original.getContainerSize(); slot++) {
            ItemStack stack = original.getItem(slot);
            if (stack.getItem() instanceof PouchItem) {
                original.setItem(slot, ItemStack.EMPTY);
                if (replacement.getItem(slot).isEmpty()) {
                    replacement.setItem(slot, stack);
                } else {
                    replacement.placeItemBackInInventory(stack);
                }
            }
        }
    }
}
