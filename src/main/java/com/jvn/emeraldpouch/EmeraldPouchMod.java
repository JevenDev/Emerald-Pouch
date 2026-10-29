package com.jvn.emeraldpouch;

import com.jvn.emeraldpouch.command.PouchCommands;
import com.jvn.emeraldpouch.compat.PouchDeathCompat;
import com.jvn.emeraldpouch.config.EmeraldPouchServerConfig;
import com.jvn.emeraldpouch.event.PouchDeathHandler;
import com.jvn.emeraldpouch.registry.ModEnchantments;
import com.jvn.emeraldpouch.compat.ShulkerBoxTooltipCompat;
import com.jvn.emeraldpouch.config.EmeraldPouchClientConfig;
import com.jvn.emeraldpouch.event.AutoPickupHandler;
import com.jvn.emeraldpouch.event.MerchantTradeHandler;
import com.jvn.emeraldpouch.event.PouchUseHandler;
import com.jvn.emeraldpouch.network.ModNetwork;
import com.jvn.emeraldpouch.registry.ModCreativeTabs;
import com.jvn.emeraldpouch.registry.ModItems;
import com.jvn.emeraldpouch.registry.ModMenus;
import com.jvn.emeraldpouch.registry.ModRecipeSerializers;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(EmeraldPouchMod.MOD_ID)
public final class EmeraldPouchMod {
    public static final String MOD_ID = "emeraldpouch";

    public EmeraldPouchMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modEventBus);
        ModMenus.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModRecipeSerializers.register(modEventBus);
        ModEnchantments.register(modEventBus);
        modEventBus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(PouchDeathCompat::register));
        MinecraftForge.EVENT_BUS.addListener((net.minecraftforge.event.server.ServerStartingEvent event) -> EmeraldPouchServerConfig.load());
        MinecraftForge.EVENT_BUS.addListener((net.minecraftforge.event.RegisterCommandsEvent event) -> PouchCommands.register(event.getDispatcher()));
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, PouchDeathHandler::onDeath);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, PouchDeathHandler::onDrops);
        MinecraftForge.EVENT_BUS.addListener(PouchDeathHandler::onClone);
        ModNetwork.register();
        ShulkerBoxTooltipCompat.registerPluginExtension();

        modEventBus.addListener(this::addCreativeTabItems);
        MinecraftForge.EVENT_BUS.addListener(AutoPickupHandler::onItemEntityPickup);
        MinecraftForge.EVENT_BUS.addListener(MerchantTradeHandler::onContainerOpened);
        MinecraftForge.EVENT_BUS.addListener(MerchantTradeHandler::onContainerClosed);
        MinecraftForge.EVENT_BUS.addListener(MerchantTradeHandler::onTradeWithVillager);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, PouchUseHandler::onRightClickItem);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, EmeraldPouchClientConfig.SPEC);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            com.jvn.emeraldpouch.client.EmeraldPouchClientExtensions.registerConfigScreen();
            modEventBus.addListener(com.jvn.emeraldpouch.client.EmeraldPouchClient::registerKeyMappings);
            modEventBus.addListener(com.jvn.emeraldpouch.client.EmeraldPouchClient::onClientSetup);
            MinecraftForge.EVENT_BUS.addListener(com.jvn.emeraldpouch.client.EmeraldPouchClient::onClientTick);
            MinecraftForge.EVENT_BUS.addListener(com.jvn.emeraldpouch.client.EmeraldPouchClient::onRenderGuiLayerPost);
            MinecraftForge.EVENT_BUS.addListener(com.jvn.emeraldpouch.client.EmeraldPouchClient::onContainerRenderForeground);
            MinecraftForge.EVENT_BUS.addListener(com.jvn.emeraldpouch.client.EmeraldPouchClient::onScreenMouseButtonPressedPre);
            MinecraftForge.EVENT_BUS.addListener(com.jvn.emeraldpouch.client.EmeraldPouchClient::onScreenMouseButtonPressedPost);
            MinecraftForge.EVENT_BUS.addListener(com.jvn.emeraldpouch.client.EmeraldPouchClient::onMouseButtonInputPre);
        }
    }

    private void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            for (var item : ModItems.allPouchItems()) {
                event.accept(item.get());
            }
        }
    }
}
