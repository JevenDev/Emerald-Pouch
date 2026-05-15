package com.jvn.emeraldpouch.fabric.client;

import com.jvn.emeraldpouch.EmeraldPouchMod;
import com.jvn.emeraldpouch.client.ModKeyMappings;
import com.jvn.emeraldpouch.client.PouchClientRuntime;
import com.jvn.emeraldpouch.config.EmeraldPouchFabricClientConfig;
import com.jvn.emeraldpouch.registry.ModItems;
import com.jvn.emeraldpouch.registry.ModMenus;
import com.jvn.emeraldpouch.screen.PouchScreen;
import com.jvn.toucanlib.util.ToucanResourceLocations;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.menu.MenuRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

public final class EmeraldPouchFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EmeraldPouchFabricClientConfig.load();
        MenuRegistry.registerScreenFactory(ModMenus.POUCH_MENU.get(), PouchScreen::new);
        ModKeyMappings.register();

                ResourceLocation openedPropertyId = ToucanResourceLocations.id(EmeraldPouchMod.MOD_ID, "opened");
                for (var item : ModItems.allPouchItems()) {
                        ItemProperties.register(item.get(), openedPropertyId, PouchClientRuntime::openedProperty);
                }
        ClientTickEvent.CLIENT_POST.register(PouchClientRuntime::onClientTick);
        ClientGuiEvent.RENDER_POST.register((screen, guiGraphics, mouseX, mouseY, deltaTracker) ->
                PouchClientRuntime.onScreenRenderPost(Minecraft.getInstance(), screen, guiGraphics, mouseX, mouseY)
        );
        ClientRawInputEvent.MOUSE_CLICKED_PRE.register((minecraft, button, action, mods) ->
                PouchClientRuntime.onMouseButtonInputPre(minecraft, button, action)
                        ? EventResult.interruptFalse()
                        : EventResult.pass()
        );
        ScreenEvents.AFTER_INIT.register((minecraft, screen, scaledWidth, scaledHeight) -> {
            ScreenMouseEvents.allowMouseClick(screen).register((attachedScreen, mouseX, mouseY, button) ->
                    PouchClientRuntime.onScreenMouseButtonPressedPre(Minecraft.getInstance(), attachedScreen, mouseX, mouseY, button)
            );
            ScreenMouseEvents.afterMouseClick(screen).register((attachedScreen, mouseX, mouseY, button) ->
                    PouchClientRuntime.onScreenMouseButtonPressedPost(Minecraft.getInstance(), attachedScreen, mouseX, mouseY, button, true)
            );
        });
    }
}