package com.jvn.emeraldpouch.pouch;

import com.jvn.emeraldpouch.config.EmeraldPouchServerConfig;
import com.jvn.emeraldpouch.item.PouchItem;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.player.Inventory;

public final class PouchDeathRetention {
    public static final ResourceKey<Enchantment> SOULBOUND = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath("emeraldpouch", "soulbound"));

    private PouchDeathRetention() {
    }

    public static boolean shouldKeep(ItemStack stack) {
        return stack.getItem() instanceof PouchItem
                && !EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)
                && (EmeraldPouchServerConfig.keepOnDeath() || stack.getEnchantments().entrySet().stream()
                        .anyMatch(entry -> entry.getKey().is(SOULBOUND) && entry.getIntValue() > 0));
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
