package com.jvn.emeraldpouch.fabric;

import com.jvn.emeraldpouch.EmeraldPouchMod;
import com.jvn.emeraldpouch.fabric.network.FabricModNetwork;
import net.fabricmc.api.ModInitializer;

public final class EmeraldPouchFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        EmeraldPouchMod.init();
        FabricModNetwork.init();
    }
}
