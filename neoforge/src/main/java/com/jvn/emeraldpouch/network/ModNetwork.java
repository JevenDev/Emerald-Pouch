package com.jvn.emeraldpouch.network;

import com.jvn.emeraldpouch.event.MerchantTradeHandler;
import com.jvn.emeraldpouch.pouch.PouchInventoryAccess;
import com.jvn.emeraldpouch.pouch.PouchMenuOpener;
import com.jvn.toucanlib.neoforge.network.ToucanNetwork;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ModNetwork {
    private static final String NETWORK_VERSION = "1";

    private ModNetwork() {
    }

    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        ToucanNetwork network = ToucanNetwork.create("emeraldpouch", NETWORK_VERSION, event);
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
        ToucanNetwork.enqueue(context, () -> ToucanNetwork.withServerPlayer(context, serverPlayer ->
                PouchInventoryAccess.findFirstPouch(serverPlayer.getInventory())
                        .ifPresent(reference -> PouchMenuOpener.openFromReference(serverPlayer, reference))
        ));
    }

    private static void handleMerchantTradeClick(MerchantTradeClickPayload payload, IPayloadContext context) {
        ToucanNetwork.enqueue(context, () -> ToucanNetwork.withServerPlayer(context, serverPlayer -> {
            switch (payload.clickType()) {
                case SELECTION -> MerchantTradeHandler.onTradeSelectionClick(serverPlayer);
                case RESULT -> MerchantTradeHandler.onTradeResultClick(serverPlayer, false);
                case SHIFT_RESULT -> MerchantTradeHandler.onTradeResultClick(serverPlayer, true);
            }
        }));
    }
}
