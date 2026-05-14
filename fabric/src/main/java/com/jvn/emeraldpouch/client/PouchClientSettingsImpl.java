package com.jvn.emeraldpouch.client;

public final class PouchClientSettingsImpl {
    private PouchClientSettingsImpl() {
    }

    public static PouchClientSettings.HudPosition hudPosition() {
        return PouchClientSettings.HudPosition.POSITION_1;
    }

    public static boolean showBundleCountOverlay() {
        return false;
    }

    public static PouchClientSettings.HudIconColor hudIconColor() {
        return PouchClientSettings.HudIconColor.EMERALD;
    }
}