package com.jvn.emeraldpouch.fabric.client;

import com.jvn.emeraldpouch.client.PouchClientSettings;
import com.jvn.emeraldpouch.config.EmeraldPouchFabricClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class EmeraldPouchConfigScreen extends Screen {
    private static final int BUTTON_WIDTH = 260;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 24;

    private final Screen parent;
    private PouchClientSettings.HudPosition hudPosition;
    private PouchClientSettings.InventoryPosition inventoryPosition;
    private PouchClientSettings.MerchantPosition merchantPosition;
    private boolean showBundleCountOverlay;
    private PouchClientSettings.HudIconColor hudIconColor;

    public EmeraldPouchConfigScreen(Screen parent) {
        super(Component.translatable("emeraldpouch.configuration.title", "Emerald Pouch"));
        this.parent = parent;
        this.hudPosition = EmeraldPouchFabricClientConfig.hudPosition();
        this.inventoryPosition = EmeraldPouchFabricClientConfig.inventoryPosition();
        this.merchantPosition = EmeraldPouchFabricClientConfig.merchantPosition();
        this.showBundleCountOverlay = EmeraldPouchFabricClientConfig.showBundleCountOverlay();
        this.hudIconColor = EmeraldPouchFabricClientConfig.hudIconColor();
    }

    @Override
    protected void init() {
        super.init();

        int left = (this.width - BUTTON_WIDTH) / 2;
        int top = this.height / 4;

        this.addRenderableWidget(Button.builder(hudPositionMessage(), button -> {
            this.hudPosition = cycle(this.hudPosition, PouchClientSettings.HudPosition.values());
            button.setMessage(hudPositionMessage());
        }).bounds(left, top, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        this.addRenderableWidget(Button.builder(bundleCountMessage(), button -> {
            this.showBundleCountOverlay = !this.showBundleCountOverlay;
            button.setMessage(bundleCountMessage());
        }).bounds(left, top + BUTTON_SPACING, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        this.addRenderableWidget(Button.builder(inventoryPositionMessage(), button -> {
            this.inventoryPosition = cycle(this.inventoryPosition, PouchClientSettings.InventoryPosition.values());
            button.setMessage(inventoryPositionMessage());
        }).bounds(left, top + BUTTON_SPACING * 2, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        this.addRenderableWidget(Button.builder(merchantPositionMessage(), button -> {
            this.merchantPosition = cycle(this.merchantPosition, PouchClientSettings.MerchantPosition.values());
            button.setMessage(merchantPositionMessage());
        }).bounds(left, top + BUTTON_SPACING * 3, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        this.addRenderableWidget(Button.builder(hudIconColorMessage(), button -> {
            this.hudIconColor = cycle(this.hudIconColor, PouchClientSettings.HudIconColor.values());
            button.setMessage(hudIconColorMessage());
        }).bounds(left, top + BUTTON_SPACING * 4, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        int footerY = top + BUTTON_SPACING * 6;
        int actionWidth = (BUTTON_WIDTH - 10) / 2;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> saveAndClose())
                .bounds(left, footerY, actionWidth, BUTTON_HEIGHT)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
                .bounds(left + actionWidth + 10, footerY, actionWidth, BUTTON_HEIGHT)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        guiGraphics.drawCenteredString(
                this.font,
                Component.translatable("emeraldpouch.configuration.section.emeraldpouch.client"),
                this.width / 2,
                40,
                0xA0A0A0
        );
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    private void saveAndClose() {
        EmeraldPouchFabricClientConfig.apply(
                this.hudPosition,
                this.inventoryPosition,
                this.merchantPosition,
                this.showBundleCountOverlay,
                this.hudIconColor
        );
        onClose();
    }

    private Component hudPositionMessage() {
        return Component.translatable("emeraldpouch.configuration.hud_position")
                .append(": ")
                .append(hudPositionLabel(this.hudPosition));
    }

    private Component bundleCountMessage() {
        return Component.translatable("emeraldpouch.configuration.show_bundle_count_overlay")
                .append(": ")
                .append(Component.translatable(this.showBundleCountOverlay
                        ? "tooltip.emeraldpouch.state.on"
                        : "tooltip.emeraldpouch.state.off"));
    }

    private Component inventoryPositionMessage() {
        return Component.translatable("emeraldpouch.configuration.inventory_position")
                .append(": ")
                .append(inventoryPositionLabel(this.inventoryPosition));
    }

    private Component merchantPositionMessage() {
        return Component.translatable("emeraldpouch.configuration.merchant_position")
                .append(": ")
                .append(merchantPositionLabel(this.merchantPosition));
    }

    private Component hudIconColorMessage() {
        return Component.translatable("emeraldpouch.configuration.hud_icon_color")
                .append(": ")
                .append(hudIconColorLabel(this.hudIconColor));
    }

    private static Component hudPositionLabel(PouchClientSettings.HudPosition hudPosition) {
        return switch (hudPosition) {
            case POSITION_1 -> Component.translatable("emeraldpouch.configuration.hud_position.position_1");
            case POSITION_2 -> Component.translatable("emeraldpouch.configuration.hud_position.position_2");
            case POSITION_3 -> Component.translatable("emeraldpouch.configuration.hud_position.position_3");
        };
    }

    private static Component inventoryPositionLabel(PouchClientSettings.InventoryPosition inventoryPosition) {
        return switch (inventoryPosition) {
            case POSITION_1 -> Component.translatable("emeraldpouch.configuration.inventory_position.position_1");
            case POSITION_2 -> Component.translatable("emeraldpouch.configuration.inventory_position.position_2");
            case POSITION_3 -> Component.translatable("emeraldpouch.configuration.inventory_position.position_3");
        };
    }

    private static Component merchantPositionLabel(PouchClientSettings.MerchantPosition merchantPosition) {
        return switch (merchantPosition) {
            case POSITION_1 -> Component.translatable("emeraldpouch.configuration.merchant_position.position_1");
            case POSITION_2 -> Component.translatable("emeraldpouch.configuration.merchant_position.position_2");
        };
    }

    private static Component hudIconColorLabel(PouchClientSettings.HudIconColor hudIconColor) {
        return switch (hudIconColor) {
            case EMERALD -> Component.translatable("emeraldpouch.configuration.hud_icon_color.emerald");
            case BLACK -> Component.translatable("emeraldpouch.configuration.hud_icon_color.black");
            case BLUE -> Component.translatable("emeraldpouch.configuration.hud_icon_color.blue");
            case BROWN -> Component.translatable("emeraldpouch.configuration.hud_icon_color.brown");
            case CYAN -> Component.translatable("emeraldpouch.configuration.hud_icon_color.cyan");
            case GRAY -> Component.translatable("emeraldpouch.configuration.hud_icon_color.gray");
            case GREEN -> Component.translatable("emeraldpouch.configuration.hud_icon_color.green");
            case LIGHT_BLUE -> Component.translatable("emeraldpouch.configuration.hud_icon_color.light_blue");
            case LIGHT_GRAY -> Component.translatable("emeraldpouch.configuration.hud_icon_color.light_gray");
            case LIME -> Component.translatable("emeraldpouch.configuration.hud_icon_color.lime");
            case MAGENTA -> Component.translatable("emeraldpouch.configuration.hud_icon_color.magenta");
            case ORANGE -> Component.translatable("emeraldpouch.configuration.hud_icon_color.orange");
            case PINK -> Component.translatable("emeraldpouch.configuration.hud_icon_color.pink");
            case PURPLE -> Component.translatable("emeraldpouch.configuration.hud_icon_color.purple");
            case RED -> Component.translatable("emeraldpouch.configuration.hud_icon_color.red");
            case WHITE -> Component.translatable("emeraldpouch.configuration.hud_icon_color.white");
            case YELLOW -> Component.translatable("emeraldpouch.configuration.hud_icon_color.yellow");
        };
    }

    private static <T extends Enum<T>> T cycle(T current, T[] values) {
        return values[(current.ordinal() + 1) % values.length];
    }
}
