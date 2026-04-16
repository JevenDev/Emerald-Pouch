package com.jvn.emeraldpouch.network;

import com.jvn.emeraldpouch.pouch.PouchInventoryAccess;
import com.jvn.emeraldpouch.pouch.PouchMenuOpener;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ModNetwork {
    private ModNetwork() {
    }

    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(
                OpenFirstPouchPayload.TYPE,
                OpenFirstPouchPayload.STREAM_CODEC,
                ModNetwork::handleOpenFirstPouch
        );
    }

    private static void handleOpenFirstPouch(OpenFirstPouchPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        int pouchSlot = PouchInventoryAccess.findFirstPouchSlot(serverPlayer.getInventory());
        if (pouchSlot >= 0) {
            PouchMenuOpener.openFromInventorySlot(serverPlayer, pouchSlot);
        }
    }
}
