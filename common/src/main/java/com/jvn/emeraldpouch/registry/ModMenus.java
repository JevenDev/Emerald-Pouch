package com.jvn.emeraldpouch.registry;

import com.jvn.emeraldpouch.EmeraldPouchMod;
import com.jvn.emeraldpouch.menu.PouchMenu;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;

public final class ModMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(EmeraldPouchMod.MOD_ID, Registries.MENU);

    public static final RegistrySupplier<MenuType<PouchMenu>> POUCH_MENU = MENUS.register(
            "pouch",
        () -> MenuRegistry.ofExtended(PouchMenu::new)
    );

    private ModMenus() {
    }

    public static void register() {
        MENUS.register();
    }
}
