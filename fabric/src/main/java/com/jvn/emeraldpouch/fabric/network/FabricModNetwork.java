package com.jvn.emeraldpouch.fabric.network;

import com.jvn.emeraldpouch.event.MerchantTradeHandler;
import com.jvn.emeraldpouch.network.MerchantTradeClickPayload;
import com.jvn.emeraldpouch.network.OpenFirstPouchPayload;
import com.jvn.emeraldpouch.pouch.PouchInventoryAccess;
import com.jvn.emeraldpouch.pouch.PouchMenuOpener;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class FabricModNetwork {
    private FabricModNetwork() {
    }

    public static void init() {
        PayloadTypeRegistry.playC2S().register(OpenFirstPouchPayload.TYPE, OpenFirstPouchPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(MerchantTradeClickPayload.TYPE, MerchantTradeClickPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(OpenFirstPouchPayload.TYPE, (payload, context) ->
                context.server().execute(() ->
                        PouchInventoryAccess.findFirstPouch(context.player().getInventory())
                                .ifPresent(reference -> PouchMenuOpener.openFromReference(context.player(), reference))
                )
        );
        ServerPlayNetworking.registerGlobalReceiver(MerchantTradeClickPayload.TYPE, (payload, context) ->
                context.server().execute(() -> {
                    switch (payload.clickType()) {
                        case SELECTION -> MerchantTradeHandler.onTradeSelectionClick(context.player());
                        case RESULT -> MerchantTradeHandler.onTradeResultClick(context.player(), false);
                        case SHIFT_RESULT -> MerchantTradeHandler.onTradeResultClick(context.player(), true);
                    }
                })
        );
    }
}