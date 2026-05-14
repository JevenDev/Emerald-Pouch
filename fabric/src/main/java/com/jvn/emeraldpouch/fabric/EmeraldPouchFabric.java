package com.jvn.emeraldpouch.fabric;

import com.jvn.emeraldpouch.EmeraldPouchMod;
import com.jvn.emeraldpouch.fabric.event.FabricAutoPickupHandler;
import com.jvn.emeraldpouch.fabric.network.FabricModNetwork;
import dev.architectury.event.events.common.PlayerEvent;
import net.fabricmc.api.ModInitializer;

public final class EmeraldPouchFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        EmeraldPouchMod.init();
        FabricModNetwork.init();
        PlayerEvent.PICKUP_ITEM_POST.register(FabricAutoPickupHandler::onPickupItem);
    }
}