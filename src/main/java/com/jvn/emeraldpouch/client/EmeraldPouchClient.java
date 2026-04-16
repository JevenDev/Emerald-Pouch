package com.jvn.emeraldpouch.client;

import com.jvn.emeraldpouch.EmeraldPouchMod;
import com.jvn.emeraldpouch.pouch.PouchData;
import com.jvn.emeraldpouch.network.OpenFirstPouchPayload;
import com.jvn.emeraldpouch.registry.ModItems;
import com.jvn.emeraldpouch.registry.ModMenus;
import com.jvn.emeraldpouch.screen.PouchScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class EmeraldPouchClient {
    private EmeraldPouchClient() {
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.POUCH_MENU.get(), PouchScreen::new);
    }

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ModKeyMappings.OPEN_FIRST_POUCH);
    }

    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(
                    ModItems.POUCH.get(),
                    ResourceLocation.fromNamespaceAndPath(EmeraldPouchMod.MOD_ID, "opened"),
                    EmeraldPouchClient::openedProperty
            );
            ItemProperties.register(
                    ModItems.LARGE_POUCH.get(),
                    ResourceLocation.fromNamespaceAndPath(EmeraldPouchMod.MOD_ID, "opened"),
                    EmeraldPouchClient::openedProperty
            );
        });
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null) {
            return;
        }

        while (ModKeyMappings.OPEN_FIRST_POUCH.consumeClick()) {
            PacketDistributor.sendToServer(new OpenFirstPouchPayload());
        }
    }

    private static float openedProperty(net.minecraft.world.item.ItemStack stack, net.minecraft.client.multiplayer.ClientLevel level, net.minecraft.world.entity.LivingEntity entity, int seed) {
        return PouchData.isOpenedVisualEnabled(stack) ? 1.0F : 0.0F;
    }
}
