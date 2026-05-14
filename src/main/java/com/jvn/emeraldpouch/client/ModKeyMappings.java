package com.jvn.emeraldpouch.client;

import com.jvn.emeraldpouch.EmeraldPouchMod;
import com.jvn.toucanlib.input.ToucanKeybinds;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class ModKeyMappings {
    private static final ToucanKeybinds KEYBINDS = ToucanKeybinds.create(EmeraldPouchMod.MOD_ID);

    public static final KeyMapping OPEN_FIRST_POUCH = KEYBINDS.key("open_first_pouch", GLFW.GLFW_KEY_N);

    private ModKeyMappings() {
    }

    public static void register() {
        KEYBINDS.register();
    }
}
