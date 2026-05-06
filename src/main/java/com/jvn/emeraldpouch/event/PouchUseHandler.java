package com.jvn.emeraldpouch.event;

import com.jvn.emeraldpouch.compat.AccessoriesCompat;
import com.jvn.emeraldpouch.compat.CuriosCompat;
import com.jvn.emeraldpouch.compat.ModCompat;
import com.jvn.emeraldpouch.pouch.PouchData;
import com.jvn.emeraldpouch.pouch.PouchMenuOpener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public final class PouchUseHandler {
    private PouchUseHandler() {
    }

    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!ModCompat.hasSlotCompatLoaded()) {
            return;
        }

        ItemStack heldStack = event.getItemStack();
        if (!PouchData.isPouchStack(heldStack)) {
            return;
        }

        if (event.getEntity().isShiftKeyDown()) {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                boolean equipped = AccessoriesCompat.tryEquipFromHand(serverPlayer, event.getHand());
                if (!equipped) {
                    equipped = CuriosCompat.tryEquipFromHand(serverPlayer, event.getHand());
                }

                if (equipped) {
                    event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
                    event.setCanceled(true);
                }
            }
            return;
        }

        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PouchMenuOpener.openFromHand(serverPlayer, event.getHand());
        }

        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
        event.setCanceled(true);
    }
}
