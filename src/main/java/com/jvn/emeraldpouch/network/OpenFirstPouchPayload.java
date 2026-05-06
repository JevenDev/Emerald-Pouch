package com.jvn.emeraldpouch.network;

import com.jvn.emeraldpouch.pouch.PouchInventoryAccess;
import com.jvn.emeraldpouch.pouch.PouchMenuOpener;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public record OpenFirstPouchPayload() {
    public static void encode(OpenFirstPouchPayload payload, FriendlyByteBuf buffer) {
    }

    public static OpenFirstPouchPayload decode(FriendlyByteBuf buffer) {
        return new OpenFirstPouchPayload();
    }

    public static void handle(OpenFirstPouchPayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer serverPlayer = context.getSender();
            if (serverPlayer == null) {
                return;
            }

            PouchInventoryAccess.findFirstPouch(serverPlayer.getInventory())
                    .ifPresent(reference -> PouchMenuOpener.openFromReference(serverPlayer, reference));
        });
        context.setPacketHandled(true);
    }
}
