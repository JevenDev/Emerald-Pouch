package com.jvn.emeraldpouch;

import com.jvn.emeraldpouch.command.PouchCommands;
import com.jvn.emeraldpouch.compat.PouchDeathCompat;
import com.jvn.emeraldpouch.config.EmeraldPouchServerConfig;
import com.jvn.emeraldpouch.event.MerchantTradeHandler;
import com.jvn.emeraldpouch.registry.ModCreativeTabs;
import com.jvn.emeraldpouch.registry.ModItems;
import com.jvn.emeraldpouch.registry.ModMenus;
import com.jvn.emeraldpouch.registry.ModRecipeSerializers;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
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
        LifecycleEvent.SETUP.register(PouchDeathCompat::register);
        LifecycleEvent.SERVER_STARTING.register(server -> EmeraldPouchServerConfig.load());
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> PouchCommands.register(dispatcher));
        PlayerEvent.OPEN_MENU.register(MerchantTradeHandler::onContainerOpened);
        PlayerEvent.CLOSE_MENU.register(MerchantTradeHandler::onContainerClosed);

        for (var item : ModItems.allPouchItems()) {
            CreativeTabRegistry.append(CreativeModeTabs.TOOLS_AND_UTILITIES, item);
        }
    }
}