package com.jvn.emeraldpouch.client;

import com.jvn.emeraldpouch.network.MerchantTradeClickPayload;
import dev.architectury.injectables.annotations.ExpectPlatform;

public final class PouchClientNetworking {
    private PouchClientNetworking() {
    }

    @ExpectPlatform
    public static void sendOpenFirstPouch() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sendMerchantTradeClick(MerchantTradeClickPayload.ClickType clickType, int offerIndex) {
        throw new AssertionError();
    }
}