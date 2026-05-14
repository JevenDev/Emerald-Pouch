package com.jvn.emeraldpouch.neoforge.client;

import com.jvn.toucanlib.neoforge.config.ToucanConfigScreens;
import java.util.function.BiFunction;
import net.neoforged.fml.ModContainer;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

public final class EmeraldPouchClientExtensions {
    private EmeraldPouchClientExtensions() {
    }

    public static void registerConfigScreen(ModContainer modContainer) {
        ToucanConfigScreens.register(modContainer, (BiFunction<ModContainer, Screen, Screen>) ConfigurationScreen::new);
    }
}
