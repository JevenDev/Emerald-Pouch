package com.jvn.emeraldpouch.network;

import com.jvn.emeraldpouch.EmeraldPouchMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";
    private static int nextMessageId = 0;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(EmeraldPouchMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private ModNetwork() {
    }

    public static void register() {
        CHANNEL.registerMessage(
                nextMessageId++,
                OpenFirstPouchPayload.class,
                OpenFirstPouchPayload::encode,
                OpenFirstPouchPayload::decode,
                OpenFirstPouchPayload::handle
        );
        CHANNEL.registerMessage(
                nextMessageId++,
                MerchantTradeClickPayload.class,
                MerchantTradeClickPayload::encode,
                MerchantTradeClickPayload::decode,
                MerchantTradeClickPayload::handle
        );
    }
}
