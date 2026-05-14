package com.jvn.emeraldpouch.registry;

import com.jvn.emeraldpouch.EmeraldPouchMod;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ModCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(EmeraldPouchMod.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> EMERALD_POUCH_TAB =
            CREATIVE_MODE_TABS.register(
                    "emerald_pouch",
                () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
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

    public static void register() {
        CREATIVE_MODE_TABS.register();
    }
}
