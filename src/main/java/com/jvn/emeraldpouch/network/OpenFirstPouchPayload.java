package com.jvn.emeraldpouch.network;

import com.jvn.emeraldpouch.EmeraldPouchMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenFirstPouchPayload() implements CustomPacketPayload {
    public static final Type<OpenFirstPouchPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(EmeraldPouchMod.MOD_ID, "open_first_pouch"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenFirstPouchPayload> STREAM_CODEC =
            StreamCodec.unit(new OpenFirstPouchPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
