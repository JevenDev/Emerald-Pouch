package com.jvn.emeraldpouch.config;

import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

public final class EmeraldPouchClientConfig {
    public enum HudPosition {
        POSITION_1("emeraldpouch.configuration.hud_position.position_1"),
        POSITION_2("emeraldpouch.configuration.hud_position.position_2"),
        POSITION_3("emeraldpouch.configuration.hud_position.position_3");

        private final String translationKey;

        HudPosition(String translationKey) {
            this.translationKey = translationKey;
        }

        public Component getTranslatedName() {
            return Component.translatable(this.translationKey);
        }
    }

    public enum HudIconColor {
        EMERALD("emeraldpouch.configuration.hud_icon_color.emerald"),
        BLACK("emeraldpouch.configuration.hud_icon_color.black"),
        BLUE("emeraldpouch.configuration.hud_icon_color.blue"),
        BROWN("emeraldpouch.configuration.hud_icon_color.brown"),
        CYAN("emeraldpouch.configuration.hud_icon_color.cyan"),
        GRAY("emeraldpouch.configuration.hud_icon_color.gray"),
        GREEN("emeraldpouch.configuration.hud_icon_color.green"),
        LIGHT_BLUE("emeraldpouch.configuration.hud_icon_color.light_blue"),
        LIGHT_GRAY("emeraldpouch.configuration.hud_icon_color.light_gray"),
        LIME("emeraldpouch.configuration.hud_icon_color.lime"),
        MAGENTA("emeraldpouch.configuration.hud_icon_color.magenta"),
        ORANGE("emeraldpouch.configuration.hud_icon_color.orange"),
        PINK("emeraldpouch.configuration.hud_icon_color.pink"),
        PURPLE("emeraldpouch.configuration.hud_icon_color.purple"),
        RED("emeraldpouch.configuration.hud_icon_color.red"),
        WHITE("emeraldpouch.configuration.hud_icon_color.white"),
        YELLOW("emeraldpouch.configuration.hud_icon_color.yellow");

        private final String translationKey;

        HudIconColor(String translationKey) {
            this.translationKey = translationKey;
        }

        public Component getTranslatedName() {
            return Component.translatable(this.translationKey);
        }
    }

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.EnumValue<HudPosition> HUD_POSITION = BUILDER
            .translation("emeraldpouch.configuration.hud_position")
            .comment(
                    "Controls where the pouch HUD indicator renders.",
                    "POSITION_1 = beside armor/health (left side).",
                    "POSITION_2 = beside hunger/air bar (right side).",
                    "POSITION_3 = beside hotbar and flips with active main-hand side."
            )
            .defineEnum("hudPosition", HudPosition.POSITION_1);
    private static final ForgeConfigSpec.BooleanValue SHOW_BUNDLE_COUNT_OVERLAY = BUILDER
            .translation("emeraldpouch.configuration.show_bundle_count_overlay")
            .comment("Render total pouch count centered on top of the pouch HUD icon.")
            .define("showBundleCountOverlay", false);
    private static final ForgeConfigSpec.EnumValue<HudIconColor> HUD_ICON_COLOR = BUILDER
            .translation("emeraldpouch.configuration.hud_icon_color")
            .comment("Choose which pouch color to render for HUD and inventory indicators.")
            .defineEnum("hudIconColor", HudIconColor.EMERALD);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private EmeraldPouchClientConfig() {
    }

    public static HudPosition hudPosition() {
        return HUD_POSITION.get();
    }

    public static void setHudPosition(HudPosition value) {
        HUD_POSITION.set(value);
    }

    public static boolean showBundleCountOverlay() {
        return SHOW_BUNDLE_COUNT_OVERLAY.get();
    }

    public static void setShowBundleCountOverlay(boolean value) {
        SHOW_BUNDLE_COUNT_OVERLAY.set(value);
    }

    public static HudIconColor hudIconColor() {
        return HUD_ICON_COLOR.get();
    }

    public static void setHudIconColor(HudIconColor value) {
        HUD_ICON_COLOR.set(value);
    }

    public static void save() {
        SPEC.save();
    }
}
