package com.jvn.emeraldpouch.network;

import com.jvn.emeraldpouch.EmeraldPouchMod;
import com.jvn.toucanlib.util.ToucanResourceLocations;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record MerchantTradeClickPayload(boolean shiftResultClick) implements CustomPacketPayload {
    public static final Type<MerchantTradeClickPayload> TYPE =
            new Type<>(ToucanResourceLocations.id(EmeraldPouchMod.MOD_ID, "merchant_trade_click"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MerchantTradeClickPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, payload) -> buffer.writeBoolean(payload.shiftResultClick()),
                    buffer -> new MerchantTradeClickPayload(buffer.readBoolean())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
