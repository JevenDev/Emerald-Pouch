package com.jvn.emeraldpouch.neoforge;

import com.jvn.emeraldpouch.EmeraldPouchMod;
import com.jvn.emeraldpouch.client.ModKeyMappings;
import com.jvn.emeraldpouch.config.EmeraldPouchClientConfig;
import com.jvn.emeraldpouch.neoforge.network.ModNetwork;
import com.jvn.emeraldpouch.neoforge.event.AutoPickupHandler;
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
            com.jvn.emeraldpouch.neoforge.client.EmeraldPouchClientExtensions.registerConfigScreen(modContainer);
            modEventBus.addListener(com.jvn.emeraldpouch.neoforge.client.EmeraldPouchClient::registerScreens);
            ModKeyMappings.register();
            modEventBus.addListener(com.jvn.emeraldpouch.neoforge.client.EmeraldPouchClient::onClientSetup);
            NeoForge.EVENT_BUS.addListener(com.jvn.emeraldpouch.neoforge.client.EmeraldPouchClient::onClientTick);
            NeoForge.EVENT_BUS.addListener(com.jvn.emeraldpouch.neoforge.client.EmeraldPouchClient::onRenderGuiLayerPost);
            NeoForge.EVENT_BUS.addListener(com.jvn.emeraldpouch.neoforge.client.EmeraldPouchClient::onScreenRenderPost);
            NeoForge.EVENT_BUS.addListener(com.jvn.emeraldpouch.neoforge.client.EmeraldPouchClient::onScreenMouseButtonPressedPre);
            NeoForge.EVENT_BUS.addListener(com.jvn.emeraldpouch.neoforge.client.EmeraldPouchClient::onScreenMouseButtonPressedPost);
            NeoForge.EVENT_BUS.addListener(com.jvn.emeraldpouch.neoforge.client.EmeraldPouchClient::onMouseButtonInputPre);
        }
    }
}
