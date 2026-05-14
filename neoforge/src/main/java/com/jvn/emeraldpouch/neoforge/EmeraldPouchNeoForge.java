package com.jvn.emeraldpouch.neoforge;

import com.jvn.emeraldpouch.EmeraldPouchMod;
import com.jvn.emeraldpouch.event.AutoPickupHandler;
import com.jvn.emeraldpouch.network.ModNetwork;
import com.jvn.emeraldpouch.config.EmeraldPouchClientConfig;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.api.distmarker.Dist;

@Mod(EmeraldPouchMod.MOD_ID)
public final class EmeraldPouchNeoForge {
    public EmeraldPouchNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        EmeraldPouchMod.init();
        modEventBus.addListener(ModNetwork::registerPayloadHandlers);
        NeoForge.EVENT_BUS.addListener(AutoPickupHandler::onItemEntityPickupPost);
        modContainer.registerConfig(ModConfig.Type.CLIENT, EmeraldPouchClientConfig.SPEC);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            com.jvn.emeraldpouch.client.EmeraldPouchClientExtensions.registerConfigScreen(modContainer);
            modEventBus.addListener(com.jvn.emeraldpouch.client.EmeraldPouchClient::registerScreens);
            modEventBus.addListener(com.jvn.emeraldpouch.client.EmeraldPouchClient::registerKeyMappings);
            modEventBus.addListener(com.jvn.emeraldpouch.client.EmeraldPouchClient::onClientSetup);
            NeoForge.EVENT_BUS.addListener(com.jvn.emeraldpouch.client.EmeraldPouchClient::onClientTick);
            NeoForge.EVENT_BUS.addListener(com.jvn.emeraldpouch.client.EmeraldPouchClient::onRenderGuiLayerPost);
            NeoForge.EVENT_BUS.addListener(com.jvn.emeraldpouch.client.EmeraldPouchClient::onScreenRenderPost);
            NeoForge.EVENT_BUS.addListener(com.jvn.emeraldpouch.client.EmeraldPouchClient::onScreenMouseButtonPressedPre);
            NeoForge.EVENT_BUS.addListener(com.jvn.emeraldpouch.client.EmeraldPouchClient::onScreenMouseButtonPressedPost);
            NeoForge.EVENT_BUS.addListener(com.jvn.emeraldpouch.client.EmeraldPouchClient::onMouseButtonInputPre);
        }
    }
}
