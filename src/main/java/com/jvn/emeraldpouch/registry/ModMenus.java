package com.jvn.emeraldpouch.registry;

import com.jvn.emeraldpouch.EmeraldPouchMod;
import com.jvn.emeraldpouch.menu.PouchMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, EmeraldPouchMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<PouchMenu>> POUCH_MENU = MENUS.register(
            "pouch",
            () -> IMenuTypeExtension.create(PouchMenu::new)
    );

    private ModMenus() {
    }

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
