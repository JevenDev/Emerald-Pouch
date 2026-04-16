package com.jvn.emeraldpouch.config;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.TranslatableEnum;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class EmeraldPouchClientConfig {
    public enum HudPosition implements TranslatableEnum {
        POSITION_1("emeraldpouch.configuration.hud_position.position_1"),
        POSITION_2("emeraldpouch.configuration.hud_position.position_2"),
        POSITION_3("emeraldpouch.configuration.hud_position.position_3");

        private final String translationKey;

        HudPosition(String translationKey) {
            this.translationKey = translationKey;
        }

        @Override
        public Component getTranslatedName() {
            return Component.translatable(this.translationKey);
        }
    }

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.EnumValue<HudPosition> HUD_POSITION = BUILDER
            .translation("emeraldpouch.configuration.hud_position")
            .comment(
                    "Controls where the pouch HUD indicator renders.",
                    "POSITION_1 = beside armor/health (left side).",
                    "POSITION_2 = beside hunger/air bar (right side).",
                    "POSITION_3 = beside hotbar and flips with active main-hand side."
            )
            .defineEnum("hudPosition", HudPosition.POSITION_1);
    private static final ModConfigSpec.BooleanValue SHOW_BUNDLE_COUNT_OVERLAY = BUILDER
            .translation("emeraldpouch.configuration.show_bundle_count_overlay")
            .comment("Render total pouch count centered on top of the pouch HUD icon.")
            .define("showBundleCountOverlay", false);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private EmeraldPouchClientConfig() {
    }

    public static HudPosition hudPosition() {
        return HUD_POSITION.get();
    }

    public static boolean showBundleCountOverlay() {
        return SHOW_BUNDLE_COUNT_OVERLAY.get();
    }
}
