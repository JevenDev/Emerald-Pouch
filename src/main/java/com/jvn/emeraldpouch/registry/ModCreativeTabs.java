package com.jvn.emeraldpouch.registry;

import com.jvn.emeraldpouch.EmeraldPouchMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EmeraldPouchMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> EMERALD_POUCH_TAB =
            CREATIVE_MODE_TABS.register(
                    "emerald_pouch",
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.emeraldpouch"))
                            .icon(() -> new ItemStack(ModItems.POUCH.get()))
                            .displayItems((parameters, output) -> {
                                for (var item : ModItems.allPouchItems()) {
                                    output.accept(item.get());
                                }
                            })
                            .build()
            );

    private ModCreativeTabs() {
    }

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
