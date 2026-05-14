package com.jvn.emeraldpouch.screen;

import com.jvn.emeraldpouch.EmeraldPouchMod;
import com.jvn.emeraldpouch.menu.PouchMenu;
import com.jvn.toucanlib.util.ToucanResourceLocations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

public class PouchScreen extends AbstractContainerScreen<PouchMenu> {
    private static final ResourceLocation CONTAINER_BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final int BUTTON_SIZE = 12;
    private static final int BUTTON_GAP = 1;
    private static final int BUTTON_RIGHT_MARGIN = 7;
    private static final int BUTTON_TOP_MARGIN = 4;
    private static final ResourceLocation COMPACT_DEFAULT = id("pouch/compact_button");
    private static final ResourceLocation COMPACT_SELECTED = id("pouch/compact_button_selected");
    private static final ResourceLocation COMPACT_HOVER = id("pouch/compact_button_hover");
    private static final ResourceLocation PICKUP_DEFAULT = id("pouch/pickup_button");
    private static final ResourceLocation PICKUP_SELECTED = id("pouch/pickup_button_selected");
    private static final ResourceLocation PICKUP_HOVER = id("pouch/pickup_button_hover");
    private static final Component COMPACT_NAME = Component.translatable("screen.emeraldpouch.auto_compact.name");
    private static final Component PICKUP_NAME = Component.translatable("screen.emeraldpouch.auto_pickup.name");

    private final int rows;
    private Button compactButton;
    private Button pickupButton;
    private boolean openSoundPlayed;

    public PouchScreen(PouchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.rows = menu.storageRows();
        this.imageHeight = 114 + this.rows * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();

        playOpenSoundIfNeeded();

        int pickupX = this.leftPos + this.imageWidth - BUTTON_RIGHT_MARGIN - BUTTON_SIZE;
        int compactX = pickupX - BUTTON_GAP - BUTTON_SIZE;
        int buttonY = this.topPos + BUTTON_TOP_MARGIN;

        this.compactButton = addRenderableWidget(
                new PouchToggleIconButton(
                        compactX,
                        buttonY,
                        BUTTON_SIZE,
                        COMPACT_DEFAULT,
                        COMPACT_SELECTED,
                        COMPACT_HOVER,
                        this.menu::isAutoCompactEnabled,
                        button -> pressToggle(PouchMenu.BUTTON_TOGGLE_AUTO_COMPACT),
                        COMPACT_NAME
                )
        );

        this.pickupButton = addRenderableWidget(
                new PouchToggleIconButton(
                        pickupX,
                        buttonY,
                        BUTTON_SIZE,
                        PICKUP_DEFAULT,
                        PICKUP_SELECTED,
                        PICKUP_HOVER,
                        this.menu::isAutoPickupEnabled,
                        button -> pressToggle(PouchMenu.BUTTON_TOGGLE_AUTO_PICKUP),
                        PICKUP_NAME
                )
        );

        this.compactButton.setTooltip(Tooltip.create(COMPACT_NAME));
        this.pickupButton.setTooltip(Tooltip.create(PICKUP_NAME));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void onClose() {
        Minecraft minecraft = this.minecraft;
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BUNDLE_REMOVE_ONE, 1.0F, 0.5F));
        }
        super.onClose();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(CONTAINER_BACKGROUND, left, top, 0, 0, this.imageWidth, this.rows * 18 + 17);
        guiGraphics.blit(CONTAINER_BACKGROUND, left, top + this.rows * 18 + 17, 0, 126, this.imageWidth, 96);
    }

    private void pressToggle(int toggleButtonId) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, toggleButtonId);
        }
    }

    private void playOpenSoundIfNeeded() {
        if (openSoundPlayed) {
            return;
        }

        Minecraft minecraft = this.minecraft;
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BUNDLE_INSERT, 1.0F, 0.5F));
            openSoundPlayed = true;
        }
    }

    private static ResourceLocation id(String path) {
        return ToucanResourceLocations.id(EmeraldPouchMod.MOD_ID, path);
    }
}
