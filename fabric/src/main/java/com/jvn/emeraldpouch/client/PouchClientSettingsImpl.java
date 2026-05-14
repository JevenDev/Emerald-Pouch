package com.jvn.emeraldpouch.client.fabric;

import com.jvn.emeraldpouch.client.PouchClientSettings;
import com.jvn.emeraldpouch.config.EmeraldPouchFabricClientConfig;

public final class PouchClientSettingsImpl {
    private PouchClientSettingsImpl() {
    }

    public static PouchClientSettings.HudPosition hudPosition() {
        return EmeraldPouchFabricClientConfig.hudPosition();
    }

    public static boolean showBundleCountOverlay() {
        return EmeraldPouchFabricClientConfig.showBundleCountOverlay();
    }

    public static PouchClientSettings.HudIconColor hudIconColor() {
        return EmeraldPouchFabricClientConfig.hudIconColor();
    }
}