package com.jvn.emeraldpouch.neoforge.client;

import com.jvn.emeraldpouch.EmeraldPouchMod;
import com.jvn.emeraldpouch.client.ModKeyMappings;
import com.jvn.emeraldpouch.client.PouchClientRuntime;
import com.jvn.emeraldpouch.registry.ModItems;
import com.jvn.emeraldpouch.registry.ModMenus;
import com.jvn.emeraldpouch.screen.PouchScreen;
import com.jvn.toucanlib.util.ToucanResourceLocations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

public final class EmeraldPouchClient {
    private EmeraldPouchClient() {
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.POUCH_MENU.get(), PouchScreen::new);
    }

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        ModKeyMappings.register();
    }

    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ResourceLocation openedPropertyId = id("opened");
            for (var item : ModItems.allPouchItems()) {
                ItemProperties.register(item.get(), openedPropertyId, PouchClientRuntime::openedProperty);
            }
        });
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        PouchClientRuntime.onClientTick(Minecraft.getInstance());
    }

    public static void onRenderGuiLayerPost(RenderGuiLayerEvent.Post event) {
        if (!VanillaGuiLayers.EXPERIENCE_LEVEL.equals(event.getName())) {
            return;
        }

        PouchClientRuntime.onRenderHud(Minecraft.getInstance(), event.getGuiGraphics());
    }

    public static void onScreenRenderPost(ScreenEvent.Render.Post event) {
        PouchClientRuntime.onScreenRenderPost(
                Minecraft.getInstance(),
                event.getScreen(),
                event.getGuiGraphics(),
                (int) event.getMouseX(),
                (int) event.getMouseY()
        );
    }

    public static void onScreenMouseButtonPressedPre(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!PouchClientRuntime.onScreenMouseButtonPressedPre(
                Minecraft.getInstance(),
                event.getScreen(),
                event.getMouseX(),
                event.getMouseY(),
                event.getButton()
        )) {
            event.setCanceled(true);
        }
    }

    public static void onScreenMouseButtonPressedPost(ScreenEvent.MouseButtonPressed.Post event) {
        PouchClientRuntime.onScreenMouseButtonPressedPost(
                Minecraft.getInstance(),
                event.getScreen(),
                event.getMouseX(),
                event.getMouseY(),
                event.getButton(),
                event.wasClickHandled()
        );
    }

    public static void onMouseButtonInputPre(InputEvent.MouseButton.Pre event) {
        if (PouchClientRuntime.onMouseButtonInputPre(Minecraft.getInstance(), event.getButton(), event.getAction())) {
            event.setCanceled(true);
        }
    }

    private static ResourceLocation id(String path) {
        return ToucanResourceLocations.id(EmeraldPouchMod.MOD_ID, path);
    }
}
