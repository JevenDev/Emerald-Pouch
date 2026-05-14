package com.jvn.emeraldpouch;

import com.jvn.emeraldpouch.event.MerchantTradeHandler;
import com.jvn.emeraldpouch.registry.ModCreativeTabs;
import com.jvn.emeraldpouch.registry.ModItems;
import com.jvn.emeraldpouch.registry.ModMenus;
import com.jvn.emeraldpouch.registry.ModRecipeSerializers;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.registry.CreativeTabRegistry;
import net.minecraft.world.item.CreativeModeTabs;

public final class EmeraldPouchMod {
    public static final String MOD_ID = "emeraldpouch";

    private static boolean initialized;

    private EmeraldPouchMod() {
    }

    public static void init() {
        if (initialized) {
            return;
        }

        initialized = true;
        ModItems.register();
        ModMenus.register();
        ModCreativeTabs.register();
        ModRecipeSerializers.register();
        PlayerEvent.OPEN_MENU.register(MerchantTradeHandler::onContainerOpened);
        PlayerEvent.CLOSE_MENU.register(MerchantTradeHandler::onContainerClosed);

        for (var item : ModItems.allPouchItems()) {
            CreativeTabRegistry.append(CreativeModeTabs.TOOLS_AND_UTILITIES, item);
        }
    }
}