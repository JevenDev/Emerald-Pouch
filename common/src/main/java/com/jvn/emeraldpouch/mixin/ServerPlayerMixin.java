package com.jvn.emeraldpouch.mixin;

import com.jvn.emeraldpouch.pouch.PouchDeathRetention;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @Inject(method = "restoreFrom", at = @At("RETURN"))
    private void emeraldpouch$restorePouches(ServerPlayer original, boolean alive, CallbackInfo ci) {
        ServerPlayer replacement = (ServerPlayer) (Object) this;
        if (!alive && !original.isSpectator()
                && !original.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            PouchDeathRetention.restoreInventory(original.getInventory(), replacement.getInventory());
        }
    }
}
