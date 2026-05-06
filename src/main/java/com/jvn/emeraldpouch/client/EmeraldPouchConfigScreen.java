package com.jvn.emeraldpouch.client;

import com.jvn.emeraldpouch.config.EmeraldPouchClientConfig;
import java.util.Arrays;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class EmeraldPouchConfigScreen extends Screen {
    private static final Component TITLE = Component.translatable("modmenu.nameTranslation.emeraldpouch");
    private static final Component HUD_POSITION_LABEL = Component.translatable("emeraldpouch.configuration.hud_position");
    private static final Component HUD_ICON_COLOR_LABEL = Component.translatable("emeraldpouch.configuration.hud_icon_color");
    private static final Component SHOW_BUNDLE_COUNT_LABEL = Component.translatable("emeraldpouch.configuration.show_bundle_count_overlay");

    private final Screen parent;
    private EmeraldPouchClientConfig.HudPosition hudPosition;
    private EmeraldPouchClientConfig.HudIconColor hudIconColor;
    private boolean showBundleCountOverlay;

    public EmeraldPouchConfigScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
        this.hudPosition = EmeraldPouchClientConfig.hudPosition();
        this.hudIconColor = EmeraldPouchClientConfig.hudIconColor();
        this.showBundleCountOverlay = EmeraldPouchClientConfig.showBundleCountOverlay();
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int top = this.height / 4;
        int controlWidth = 220;
        int buttonWidth = 100;

        this.addRenderableWidget(
                CycleButton.builder(EmeraldPouchClientConfig.HudPosition::getTranslatedName)
                        .withValues(Arrays.asList(EmeraldPouchClientConfig.HudPosition.values()))
                        .withInitialValue(hudPosition)
                        .create(centerX - controlWidth / 2, top, controlWidth, 20, HUD_POSITION_LABEL,
                                (button, value) -> this.hudPosition = value)
        );

        this.addRenderableWidget(
                CycleButton.onOffBuilder(showBundleCountOverlay)
                        .withInitialValue(showBundleCountOverlay)
                        .create(centerX - controlWidth / 2, top + 28, controlWidth, 20, SHOW_BUNDLE_COUNT_LABEL,
                                (button, value) -> this.showBundleCountOverlay = value)
        );

        this.addRenderableWidget(
                CycleButton.builder(EmeraldPouchClientConfig.HudIconColor::getTranslatedName)
                        .withValues(Arrays.asList(EmeraldPouchClientConfig.HudIconColor.values()))
                        .withInitialValue(hudIconColor)
                        .create(centerX - controlWidth / 2, top + 56, controlWidth, 20, HUD_ICON_COLOR_LABEL,
                                (button, value) -> this.hudIconColor = value)
        );

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onDone())
                .bounds(centerX - buttonWidth - 4, top + 100, buttonWidth, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
                .bounds(centerX + 4, top + 100, buttonWidth, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    private void onDone() {
        EmeraldPouchClientConfig.setHudPosition(hudPosition);
        EmeraldPouchClientConfig.setShowBundleCountOverlay(showBundleCountOverlay);
        EmeraldPouchClientConfig.setHudIconColor(hudIconColor);
        EmeraldPouchClientConfig.save();
        onClose();
    }
}
