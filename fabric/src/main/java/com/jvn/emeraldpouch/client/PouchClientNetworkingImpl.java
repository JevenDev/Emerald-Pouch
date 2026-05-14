package com.jvn.emeraldpouch.client.fabric;

import com.jvn.emeraldpouch.client.PouchClientNetworking;
import com.jvn.emeraldpouch.network.MerchantTradeClickPayload;
import com.jvn.emeraldpouch.network.OpenFirstPouchPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class PouchClientNetworkingImpl {
    private PouchClientNetworkingImpl() {
    }

    public static void sendOpenFirstPouch() {
        ClientPlayNetworking.send(new OpenFirstPouchPayload());
    }

    public static void sendMerchantTradeClick(MerchantTradeClickPayload.ClickType clickType, int offerIndex) {
        ClientPlayNetworking.send(new MerchantTradeClickPayload(clickType, offerIndex));
    }
}