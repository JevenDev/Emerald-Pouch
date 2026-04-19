package com.jvn.emeraldpouch.network;

import com.jvn.emeraldpouch.event.MerchantTradeHandler;
import com.jvn.emeraldpouch.pouch.PouchInventoryAccess;
import com.jvn.emeraldpouch.pouch.PouchMenuOpener;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ModNetwork {
    private ModNetwork() {
    }

    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToServer(
                OpenFirstPouchPayload.TYPE,
                OpenFirstPouchPayload.STREAM_CODEC,
                ModNetwork::handleOpenFirstPouch
        );
        registrar.playToServer(
                MerchantTradeClickPayload.TYPE,
                MerchantTradeClickPayload.STREAM_CODEC,
                ModNetwork::handleMerchantTradeClick
        );
    }

    private static void handleOpenFirstPouch(OpenFirstPouchPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }

            PouchInventoryAccess.findFirstPouch(serverPlayer.getInventory())
                    .ifPresent(reference -> PouchMenuOpener.openFromReference(serverPlayer, reference));
        });
    }

    private static void handleMerchantTradeClick(MerchantTradeClickPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }

            if (payload.shiftResultClick()) {
                MerchantTradeHandler.onShiftTradeResultClick(serverPlayer);
            } else {
                MerchantTradeHandler.onTradeSelectionClick(serverPlayer);
            }
        });
    }
}
