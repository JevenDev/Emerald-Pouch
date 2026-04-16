package com.jvn.emeraldpouch.compat;

import net.neoforged.fml.ModList;

public final class ModCompat {
    private static final boolean SHULKER_TOOLTIP_LOADED =
            ModList.get().isLoaded("shulkerboxtooltip") || ModList.get().isLoaded("shulker_box_tooltip");
    private static final boolean CURIOS_LOADED = ModList.get().isLoaded("curios");

    private ModCompat() {
    }

    public static boolean isShulkerTooltipLoaded() {
        return SHULKER_TOOLTIP_LOADED;
    }

    public static boolean isCuriosLoaded() {
        return CURIOS_LOADED;
    }
}
