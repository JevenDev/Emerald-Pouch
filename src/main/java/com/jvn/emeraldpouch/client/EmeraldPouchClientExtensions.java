package com.jvn.emeraldpouch.client;

import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

public final class EmeraldPouchClientExtensions {
    private EmeraldPouchClientExtensions() {
    }

    public static void registerConfigScreen() {
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> new EmeraldPouchConfigScreen(parent))
        );
    }
}
