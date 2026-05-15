package com.jvn.emeraldpouch.fabric.mixin.client;

import com.jvn.emeraldpouch.client.PouchClientRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(
            method = "renderChat",
            at = @At("HEAD")
    )
    private void emeraldPouch$renderBeforeChat(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo callbackInfo) {
        PouchClientRuntime.onRenderHud(this.minecraft, guiGraphics);
    }
}