package com.jvn.emeraldpouch.client.neoforge;

import com.jvn.emeraldpouch.network.MerchantTradeClickPayload;
import com.jvn.emeraldpouch.network.OpenFirstPouchPayload;
import net.neoforged.neoforge.network.PacketDistributor;

public final class PouchClientNetworkingImpl {
    private PouchClientNetworkingImpl() {
    }

    public static void sendOpenFirstPouch() {
        PacketDistributor.sendToServer(new OpenFirstPouchPayload());
    }

    public static void sendMerchantTradeClick(MerchantTradeClickPayload.ClickType clickType, int offerIndex) {
        PacketDistributor.sendToServer(new MerchantTradeClickPayload(clickType, offerIndex));
    }
}