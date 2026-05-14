package com.jvn.emeraldpouch.network;

import com.jvn.emeraldpouch.EmeraldPouchMod;
import com.jvn.toucanlib.util.toucanResourceLocations;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record OpenFirstPouchPayload() implements CustomPacketPayload {
    public static final Type<OpenFirstPouchPayload> TYPE =
            new Type<>(toucanResourceLocations.id(EmeraldPouchMod.MOD_ID, "open_first_pouch"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenFirstPouchPayload> STREAM_CODEC =
            StreamCodec.unit(new OpenFirstPouchPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
