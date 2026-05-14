package com.jvn.emeraldpouch.client;

import dev.architectury.injectables.annotations.ExpectPlatform;

public final class PouchClientSettings {
    public enum HudPosition {
        POSITION_1,
        POSITION_2,
        POSITION_3
    }

    public enum HudIconColor {
        EMERALD,
        BLACK,
        BLUE,
        BROWN,
        CYAN,
        GRAY,
        GREEN,
        LIGHT_BLUE,
        LIGHT_GRAY,
        LIME,
        MAGENTA,
        ORANGE,
        PINK,
        PURPLE,
        RED,
        WHITE,
        YELLOW
    }

    private PouchClientSettings() {
    }

    @ExpectPlatform
    public static HudPosition hudPosition() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean showBundleCountOverlay() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static HudIconColor hudIconColor() {
        throw new AssertionError();
    }
}