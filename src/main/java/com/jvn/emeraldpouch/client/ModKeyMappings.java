package com.jvn.emeraldpouch.client;

import com.jvn.emeraldpouch.EmeraldPouchMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class ModKeyMappings {
    public static final String CATEGORY = "key.categories." + EmeraldPouchMod.MOD_ID;

    public static final KeyMapping OPEN_FIRST_POUCH = new KeyMapping(
            "key." + EmeraldPouchMod.MOD_ID + ".open_first_pouch",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_N,
            CATEGORY
    );

    private ModKeyMappings() {
    }
}
