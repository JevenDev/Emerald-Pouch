package com.jvn.emeraldpouch.registry;

import com.jvn.emeraldpouch.EmeraldPouchMod;
import com.jvn.emeraldpouch.item.PouchItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEnchantments {
    private static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, EmeraldPouchMod.MOD_ID);
    private static final EnchantmentCategory POUCH = EnchantmentCategory.create("EMERALD_POUCH",
            item -> item instanceof PouchItem);
    public static final RegistryObject<Enchantment> SOULBOUND = ENCHANTMENTS.register("soulbound", Soulbound::new);

    private ModEnchantments() {
    }

    public static void register(IEventBus bus) {
        ENCHANTMENTS.register(bus);
    }

    private static final class Soulbound extends Enchantment {
        private Soulbound() {
            super(Rarity.RARE, POUCH, EquipmentSlot.values());
        }

        @Override
        public int getMinCost(int level) {
            return 25;
        }

        @Override
        public int getMaxCost(int level) {
            return 75;
        }

        @Override
        public boolean isTreasureOnly() {
            return true;
        }
    }
}
