package com.jvn.emeraldpouch.compat;

import dev.architectury.platform.Platform;

public final class ModCompat {
    private static final boolean SHULKER_TOOLTIP_LOADED =
        Platform.isModLoaded("shulkerboxtooltip") || Platform.isModLoaded("shulker_box_tooltip");
    private static final boolean CURIOS_LOADED = Platform.isModLoaded("curios");
    private static final boolean ACCESSORIES_LOADED = Platform.isModLoaded("accessories");
    private static final boolean TRINKETS_LOADED = Platform.isModLoaded("trinkets");

    private ModCompat() {
    }

    public static boolean isShulkerTooltipLoaded() {
        return SHULKER_TOOLTIP_LOADED;
    }

    public static boolean isCuriosLoaded() {
        return CURIOS_LOADED;
    }

    public static boolean isAccessoriesLoaded() {
        return ACCESSORIES_LOADED;
    }

    public static boolean isTrinketsLoaded() {
        return TRINKETS_LOADED;
    }

    public static boolean hasSlotCompatLoaded() {
        return CURIOS_LOADED || ACCESSORIES_LOADED || TRINKETS_LOADED;
    }
}
