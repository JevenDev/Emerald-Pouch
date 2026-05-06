package com.jvn.emeraldpouch.network;

import com.jvn.emeraldpouch.event.MerchantTradeHandler;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public record MerchantTradeClickPayload(boolean shiftResultClick) {
    public static void encode(MerchantTradeClickPayload payload, FriendlyByteBuf buffer) {
        buffer.writeBoolean(payload.shiftResultClick());
    }

    public static MerchantTradeClickPayload decode(FriendlyByteBuf buffer) {
        return new MerchantTradeClickPayload(buffer.readBoolean());
    }

    public static void handle(MerchantTradeClickPayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer serverPlayer = context.getSender();
            if (serverPlayer == null) {
                return;
            }

            if (payload.shiftResultClick()) {
                MerchantTradeHandler.onShiftTradeResultClick(serverPlayer);
            } else {
                MerchantTradeHandler.onTradeSelectionClick(serverPlayer);
            }
        });
        context.setPacketHandled(true);
    }
}
