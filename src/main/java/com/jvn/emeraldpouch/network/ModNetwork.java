package com.jvn.emeraldpouch.network;

import com.jvn.emeraldpouch.event.MerchantTradeHandler;
import com.jvn.emeraldpouch.pouch.PouchInventoryAccess;
import com.jvn.emeraldpouch.pouch.PouchMenuOpener;
import com.jvn.toucanlib.neoforge.network.toucanNetwork;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ModNetwork {
    private static final String NETWORK_VERSION = "1";

    private ModNetwork() {
    }

    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        toucanNetwork network = toucanNetwork.create("emeraldpouch", NETWORK_VERSION, event);
        network.playToServer(
                OpenFirstPouchPayload.TYPE,
                OpenFirstPouchPayload.STREAM_CODEC,
                ModNetwork::handleOpenFirstPouch
        );
        network.playToServer(
                MerchantTradeClickPayload.TYPE,
                MerchantTradeClickPayload.STREAM_CODEC,
                ModNetwork::handleMerchantTradeClick
        );
    }

    private static void handleOpenFirstPouch(OpenFirstPouchPayload payload, IPayloadContext context) {
        toucanNetwork.enqueue(context, () -> toucanNetwork.withServerPlayer(context, serverPlayer ->
                PouchInventoryAccess.findFirstPouch(serverPlayer.getInventory())
                        .ifPresent(reference -> PouchMenuOpener.openFromReference(serverPlayer, reference))
        ));
    }

    private static void handleMerchantTradeClick(MerchantTradeClickPayload payload, IPayloadContext context) {
        toucanNetwork.enqueue(context, () -> toucanNetwork.withServerPlayer(context, serverPlayer -> {
            if (payload.shiftResultClick()) {
                MerchantTradeHandler.onShiftTradeResultClick(serverPlayer);
            } else {
                MerchantTradeHandler.onTradeSelectionClick(serverPlayer);
            }
        }));
    }
}
