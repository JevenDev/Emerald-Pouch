package com.jvn.emeraldpouch.client.neoforge;

import com.jvn.emeraldpouch.client.PouchClientSettings;
import com.jvn.emeraldpouch.config.EmeraldPouchClientConfig;

public final class PouchClientSettingsImpl {
    private PouchClientSettingsImpl() {
    }

    public static PouchClientSettings.HudPosition hudPosition() {
        return switch (EmeraldPouchClientConfig.hudPosition()) {
            case POSITION_1 -> PouchClientSettings.HudPosition.POSITION_1;
            case POSITION_2 -> PouchClientSettings.HudPosition.POSITION_2;
            case POSITION_3 -> PouchClientSettings.HudPosition.POSITION_3;
        };
    }

    public static PouchClientSettings.InventoryPosition inventoryPosition() {
        return switch (EmeraldPouchClientConfig.inventoryPosition()) {
            case POSITION_1 -> PouchClientSettings.InventoryPosition.POSITION_1;
            case POSITION_2 -> PouchClientSettings.InventoryPosition.POSITION_2;
            case POSITION_3 -> PouchClientSettings.InventoryPosition.POSITION_3;
        };
    }

    public static PouchClientSettings.MerchantPosition merchantPosition() {
        return switch (EmeraldPouchClientConfig.merchantPosition()) {
            case POSITION_1 -> PouchClientSettings.MerchantPosition.POSITION_1;
            case POSITION_2 -> PouchClientSettings.MerchantPosition.POSITION_2;
        };
    }

    public static boolean showBundleCountOverlay() {
        return EmeraldPouchClientConfig.showBundleCountOverlay();
    }

    public static PouchClientSettings.HudIconColor hudIconColor() {
        return switch (EmeraldPouchClientConfig.hudIconColor()) {
            case EMERALD -> PouchClientSettings.HudIconColor.EMERALD;
            case BLACK -> PouchClientSettings.HudIconColor.BLACK;
            case BLUE -> PouchClientSettings.HudIconColor.BLUE;
            case BROWN -> PouchClientSettings.HudIconColor.BROWN;
            case CYAN -> PouchClientSettings.HudIconColor.CYAN;
            case GRAY -> PouchClientSettings.HudIconColor.GRAY;
            case GREEN -> PouchClientSettings.HudIconColor.GREEN;
            case LIGHT_BLUE -> PouchClientSettings.HudIconColor.LIGHT_BLUE;
            case LIGHT_GRAY -> PouchClientSettings.HudIconColor.LIGHT_GRAY;
            case LIME -> PouchClientSettings.HudIconColor.LIME;
            case MAGENTA -> PouchClientSettings.HudIconColor.MAGENTA;
            case ORANGE -> PouchClientSettings.HudIconColor.ORANGE;
            case PINK -> PouchClientSettings.HudIconColor.PINK;
            case PURPLE -> PouchClientSettings.HudIconColor.PURPLE;
            case RED -> PouchClientSettings.HudIconColor.RED;
            case WHITE -> PouchClientSettings.HudIconColor.WHITE;
            case YELLOW -> PouchClientSettings.HudIconColor.YELLOW;
        };
    }
}
