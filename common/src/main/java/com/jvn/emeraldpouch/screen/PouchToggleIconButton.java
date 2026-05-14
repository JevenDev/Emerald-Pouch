package com.jvn.emeraldpouch.screen;

import java.util.function.BooleanSupplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class PouchToggleIconButton extends Button {
    private final ResourceLocation defaultSprite;
    private final ResourceLocation selectedSprite;
    private final ResourceLocation hoverSprite;
    private final BooleanSupplier selectedState;

    public PouchToggleIconButton(
            int x,
            int y,
            int size,
            ResourceLocation defaultSprite,
            ResourceLocation selectedSprite,
            ResourceLocation hoverSprite,
            BooleanSupplier selectedState,
            OnPress onPress,
            Component message
    ) {
        super(x, y, size, size, message, onPress, DEFAULT_NARRATION);
        this.defaultSprite = defaultSprite;
        this.selectedSprite = selectedSprite;
        this.hoverSprite = hoverSprite;
        this.selectedState = selectedState;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation sprite = this.isHovered
                ? this.hoverSprite
                : (this.selectedState.getAsBoolean() ? this.selectedSprite : this.defaultSprite);
        guiGraphics.blitSprite(sprite, this.getX(), this.getY(), this.width, this.height);
    }
}
