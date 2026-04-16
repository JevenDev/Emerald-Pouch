package com.jvn.emeraldpouch;

import com.jvn.emeraldpouch.event.AutoPickupHandler;
import com.jvn.emeraldpouch.event.PouchUseHandler;
import com.jvn.emeraldpouch.network.ModNetwork;
import com.jvn.emeraldpouch.registry.ModItems;
import com.jvn.emeraldpouch.registry.ModMenus;
import com.jvn.emeraldpouch.config.EmeraldPouchClientConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.api.distmarker.Dist;
import org.slf4j.Logger;

@Mod(EmeraldPouchMod.MOD_ID)
public final class EmeraldPouchMod {
    public static final String MOD_ID = "emeraldpouch";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EmeraldPouchMod(IEventBus modEventBus, ModContainer modContainer) {
        ModItems.register(modEventBus);
        ModMenus.register(modEventBus);

        modEventBus.addListener(this::addCreativeTabItems);
        modEventBus.addListener(ModNetwork::registerPayloadHandlers);
        NeoForge.EVENT_BUS.addListener(AutoPickupHandler::onItemEntityPickupPost);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, PouchUseHandler::onRightClickItem);
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
            NeoForge.EVENT_BUS.addListener(com.jvn.emeraldpouch.client.EmeraldPouchClient::onMouseButtonInputPre);
        }
    }

    private void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.POUCH.get());
            event.accept(ModItems.LARGE_POUCH.get());
        }
    }
}
