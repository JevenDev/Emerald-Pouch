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

    public enum InventoryPosition implements TranslatableEnum {
        POSITION_1("emeraldpouch.configuration.inventory_position.position_1"),
        POSITION_2("emeraldpouch.configuration.inventory_position.position_2"),
        POSITION_3("emeraldpouch.configuration.inventory_position.position_3");

        private final String translationKey;

        InventoryPosition(String translationKey) {
            this.translationKey = translationKey;
        }

        @Override
        public Component getTranslatedName() {
            return Component.translatable(this.translationKey);
        }
    }

    public enum MerchantPosition implements TranslatableEnum {
        POSITION_1("emeraldpouch.configuration.merchant_position.position_1"),
        POSITION_2("emeraldpouch.configuration.merchant_position.position_2");

        private final String translationKey;

        MerchantPosition(String translationKey) {
            this.translationKey = translationKey;
        }

        @Override
        public Component getTranslatedName() {
            return Component.translatable(this.translationKey);
        }
    }

    public enum HudIconColor implements TranslatableEnum {
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
    private static final ModConfigSpec.EnumValue<InventoryPosition> INVENTORY_POSITION = BUILDER
            .translation("emeraldpouch.configuration.inventory_position")
            .comment(
                    "Controls where the pouch inventory indicator renders in the player inventory screen.",
                    "POSITION_1 = under crafting.",
                    "POSITION_2 = one slot above the offhand slot.",
                    "POSITION_3 = across from the helmet slot (same column as offhand)."
            )
            .defineEnum("inventoryPosition", InventoryPosition.POSITION_1);
    private static final ModConfigSpec.EnumValue<MerchantPosition> MERCHANT_POSITION = BUILDER
            .translation("emeraldpouch.configuration.merchant_position")
            .comment(
                    "Controls where the pouch indicator renders in the villager trading screen.",
                    "POSITION_1 = default position under crafting.",
                    "POSITION_2 = centered between the trade output slot and the right edge of the container."
            )
            .defineEnum("merchantPosition", MerchantPosition.POSITION_1);
    private static final ModConfigSpec.BooleanValue SHOW_BUNDLE_COUNT_OVERLAY = BUILDER
            .translation("emeraldpouch.configuration.show_bundle_count_overlay")
            .comment("Render total pouch count centered on top of the pouch HUD icon.")
            .define("showBundleCountOverlay", false);
    private static final ModConfigSpec.EnumValue<HudIconColor> HUD_ICON_COLOR = BUILDER
            .translation("emeraldpouch.configuration.hud_icon_color")
            .comment("Choose which pouch color to render for HUD and inventory indicators.")
            .defineEnum("hudIconColor", HudIconColor.EMERALD);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private EmeraldPouchClientConfig() {
    }

    public static HudPosition hudPosition() {
        return HUD_POSITION.get();
    }

    public static InventoryPosition inventoryPosition() {
        return INVENTORY_POSITION.get();
    }

    public static MerchantPosition merchantPosition() {
        return MERCHANT_POSITION.get();
    }

    public static boolean showBundleCountOverlay() {
        return SHOW_BUNDLE_COUNT_OVERLAY.get();
    }

    public static HudIconColor hudIconColor() {
        return HUD_ICON_COLOR.get();
    }
}
