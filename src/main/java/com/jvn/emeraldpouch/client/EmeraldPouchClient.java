package com.jvn.emeraldpouch.client;

import com.jvn.emeraldpouch.EmeraldPouchMod;
import com.mojang.blaze3d.platform.InputConstants;
import com.jvn.emeraldpouch.config.EmeraldPouchClientConfig;
import com.jvn.emeraldpouch.network.OpenFirstPouchPayload;
import com.jvn.emeraldpouch.pouch.PouchData;
import com.jvn.emeraldpouch.registry.ModItems;
import com.jvn.emeraldpouch.registry.ModMenus;
import com.jvn.emeraldpouch.screen.PouchScreen;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.network.PacketDistributor;

public final class EmeraldPouchClient {
    private static final ResourceLocation POUCH_HUD_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(EmeraldPouchMod.MOD_ID, "textures/gui/hud/emerald_pouch.png");
    private static final ResourceLocation POUCH_HUD_HOVER_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(EmeraldPouchMod.MOD_ID, "textures/gui/hud/emerald_pouch_hover.png");
    private static final int HUD_ICON_SIZE = 16;
    private static final int XP_TEXT_COLOR = 8453920;
    private static final int WHITE_TEXT_COLOR = 16777215;
    private static final int TEXT_ICON_GAP = 0;
    private static final int HOTBAR_HALF_WIDTH = 91;
    private static final int POSITION_1_SIDE_MARGIN = 1;
    private static final int POSITION_2_SIDE_MARGIN = 1;
    private static final int POSITION_3_SIDE_MARGIN = 1;
    private static final int POSITION_1_Y_FROM_BOTTOM = 47;
    private static final int POSITION_2_Y_OFFSET = -3;
    private static final int POSITION_3_Y_FROM_BOTTOM = 19;
    private static final int INVENTORY_ICON_RIGHT_MARGIN = 6;
    private static final int INVENTORY_ICON_TOP_MARGIN = 62;
    private static final int HUD_TEXT_Y_OFFSET = 1;
    private static final int INVENTORY_TEXT_Y_OFFSET = 1;
    private static final int BUNDLE_OVERLAY_TEXT_X_OFFSET = 4;
    private static final int BUNDLE_OVERLAY_TEXT_Y_OFFSET = 4;
    private static boolean showPouchText = true;

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

    public static void onRenderGuiLayerPost(RenderGuiLayerEvent.Post event) {
        if (!VanillaGuiLayers.EXPERIENCE_LEVEL.equals(event.getName())) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null || minecraft.options.hideGui) {
            return;
        }

        PouchDisplayData displayData = PouchDisplayData.fromPlayerInventory(player);
        if (!displayData.hasPouches()) {
            return;
        }

        renderHudPouchCounter(
                event.getGuiGraphics(),
                minecraft,
                displayData.compactEmeraldAmount(),
                displayData.pouchCount(),
                player.getMainArm()
        );
    }

    public static void onScreenRenderPost(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof InventoryScreen inventoryScreen)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return;
        }

        PouchDisplayData displayData = PouchDisplayData.fromPlayerInventory(player);
        if (!displayData.hasPouches()) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        String counterText = displayData.compactEmeraldAmount();
        DisplayLayout layout = computeInventoryLayout(minecraft, inventoryScreen, counterText);
        boolean hoverIcon = isHovered(event.getMouseX(), event.getMouseY(), layout.iconX(), layout.iconY(), HUD_ICON_SIZE, HUD_ICON_SIZE);
        renderIconAndMaybeText(
                guiGraphics,
                minecraft,
                counterText,
                displayData.pouchCount(),
                layout,
                hoverIcon ? POUCH_HUD_HOVER_TEXTURE : POUCH_HUD_TEXTURE
        );

        boolean hoverText = showPouchText
                && isHovered(event.getMouseX(), event.getMouseY(), layout.textX(), layout.textY(), layout.textWidth(), minecraft.font.lineHeight);
        if (hoverIcon || hoverText) {
            guiGraphics.renderTooltip(minecraft.font, buildInventoryTooltip(displayData), java.util.Optional.empty(), event.getMouseX(), event.getMouseY());
        }
    }

    public static void onScreenMouseButtonPressedPre(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getButton() != InputConstants.MOUSE_BUTTON_LEFT) {
            return;
        }

        if (!(event.getScreen() instanceof InventoryScreen inventoryScreen)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return;
        }

        PouchDisplayData displayData = PouchDisplayData.fromPlayerInventory(player);
        if (!displayData.hasPouches()) {
            return;
        }

        DisplayLayout layout = computeInventoryLayout(minecraft, inventoryScreen, displayData.compactEmeraldAmount());
        if (!isHovered(event.getMouseX(), event.getMouseY(), layout.iconX(), layout.iconY(), HUD_ICON_SIZE, HUD_ICON_SIZE)) {
            return;
        }

        togglePouchTextVisibility(minecraft);
        event.setCanceled(true);
    }

    public static void onMouseButtonInputPre(InputEvent.MouseButton.Pre event) {
        if (event.getButton() != InputConstants.MOUSE_BUTTON_LEFT || event.getAction() != InputConstants.PRESS) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null || minecraft.options.hideGui) {
            return;
        }

        Player player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return;
        }

        PouchDisplayData displayData = PouchDisplayData.fromPlayerInventory(player);
        if (!displayData.hasPouches()) {
            return;
        }

        DisplayLayout layout = computeHudLayout(
                minecraft,
                minecraft.getWindow().getGuiScaledWidth(),
                minecraft.getWindow().getGuiScaledHeight(),
                displayData.compactEmeraldAmount(),
                player.getMainArm()
        );

        int mouseX = scaledMouseX(minecraft);
        int mouseY = scaledMouseY(minecraft);
        if (!isHovered(mouseX, mouseY, layout.iconX(), layout.iconY(), HUD_ICON_SIZE, HUD_ICON_SIZE)) {
            return;
        }

        togglePouchTextVisibility(minecraft);
        event.setCanceled(true);
    }

    private static float openedProperty(net.minecraft.world.item.ItemStack stack, net.minecraft.client.multiplayer.ClientLevel level, net.minecraft.world.entity.LivingEntity entity, int seed) {
        return PouchData.isOpenedVisualEnabled(stack) ? 1.0F : 0.0F;
    }

    private static void renderHudPouchCounter(
            GuiGraphics guiGraphics,
            Minecraft minecraft,
            String counterText,
            int bundleCount,
            HumanoidArm mainArm
    ) {
        DisplayLayout layout = computeHudLayout(minecraft, guiGraphics.guiWidth(), guiGraphics.guiHeight(), counterText, mainArm);
        renderIconAndMaybeText(guiGraphics, minecraft, counterText, bundleCount, layout, POUCH_HUD_TEXTURE);
    }

    private static void renderIconAndMaybeText(
            GuiGraphics guiGraphics,
            Minecraft minecraft,
            String text,
            int bundleCount,
            DisplayLayout layout,
            ResourceLocation texture
    ) {
        guiGraphics.blit(
                texture,
                layout.iconX(),
                layout.iconY(),
                0.0F,
                0.0F,
                HUD_ICON_SIZE,
                HUD_ICON_SIZE,
                HUD_ICON_SIZE,
                HUD_ICON_SIZE
        );

        if (showPouchText) {
            drawXpStyleText(guiGraphics, minecraft, text, layout.textX(), layout.textY());
        }

        if (EmeraldPouchClientConfig.showBundleCountOverlay()) {
            String bundleText = Integer.toString(bundleCount);
            int bundleTextX = layout.iconX() + (HUD_ICON_SIZE - minecraft.font.width(bundleText)) / 2 + BUNDLE_OVERLAY_TEXT_X_OFFSET;
            int bundleTextY = layout.iconY() + (HUD_ICON_SIZE - minecraft.font.lineHeight) / 2 + BUNDLE_OVERLAY_TEXT_Y_OFFSET;
            drawOutlinedText(guiGraphics, minecraft, bundleText, bundleTextX, bundleTextY, WHITE_TEXT_COLOR);
        }
    }

    private static void drawXpStyleText(
            GuiGraphics guiGraphics,
            Minecraft minecraft,
            String text,
            int x,
            int y
    ) {
        drawOutlinedText(guiGraphics, minecraft, text, x, y, XP_TEXT_COLOR);
    }

    private static void drawOutlinedText(
            GuiGraphics guiGraphics,
            Minecraft minecraft,
            String text,
            int x,
            int y,
            int color
    ) {
        guiGraphics.drawString(minecraft.font, text, x + 1, y, 0, false);
        guiGraphics.drawString(minecraft.font, text, x - 1, y, 0, false);
        guiGraphics.drawString(minecraft.font, text, x, y + 1, 0, false);
        guiGraphics.drawString(minecraft.font, text, x, y - 1, 0, false);
        guiGraphics.drawString(minecraft.font, text, x, y, color, false);
    }

    private static boolean isHovered(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static boolean isHovered(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static DisplayLayout computeInventoryLayout(Minecraft minecraft, InventoryScreen screen, String counterText) {
        int iconX = screen.getGuiLeft() + screen.getXSize() - INVENTORY_ICON_RIGHT_MARGIN - HUD_ICON_SIZE;
        int iconY = screen.getGuiTop() + INVENTORY_ICON_TOP_MARGIN;
        int textWidth = minecraft.font.width(counterText);
        int textX = iconX - TEXT_ICON_GAP - textWidth;
        int textY = iconY + (HUD_ICON_SIZE - minecraft.font.lineHeight) / 2 + INVENTORY_TEXT_Y_OFFSET;
        return new DisplayLayout(iconX, iconY, textX, textY, textWidth);
    }

    private static DisplayLayout computeHudLayout(
            Minecraft minecraft,
            int guiWidth,
            int guiHeight,
            String counterText,
            HumanoidArm mainArm
    ) {
        int centerX = guiWidth / 2;
        EmeraldPouchClientConfig.HudPosition hudPosition = EmeraldPouchClientConfig.hudPosition();
        boolean sideLeft = switch (hudPosition) {
            case POSITION_1 -> true;
            case POSITION_2 -> false;
            case POSITION_3 -> mainArm == HumanoidArm.LEFT;
        };

        int sideMargin = switch (hudPosition) {
            case POSITION_1 -> POSITION_1_SIDE_MARGIN;
            case POSITION_2 -> POSITION_2_SIDE_MARGIN;
            case POSITION_3 -> POSITION_3_SIDE_MARGIN;
        };

        int iconX = switch (hudPosition) {
            case POSITION_1 -> centerX - HOTBAR_HALF_WIDTH - POSITION_1_SIDE_MARGIN - HUD_ICON_SIZE;
            case POSITION_2 -> centerX + HOTBAR_HALF_WIDTH + POSITION_2_SIDE_MARGIN;
            case POSITION_3 -> sideLeft
                    ? centerX - HOTBAR_HALF_WIDTH - sideMargin - HUD_ICON_SIZE
                    : centerX + HOTBAR_HALF_WIDTH + sideMargin;
        };

        int iconY = switch (hudPosition) {
            case POSITION_1 -> guiHeight - POSITION_1_Y_FROM_BOTTOM;
            case POSITION_2 -> guiHeight - minecraft.gui.rightHeight + 10 + POSITION_2_Y_OFFSET;
            case POSITION_3 -> guiHeight - POSITION_3_Y_FROM_BOTTOM;
        };

        int textWidth = minecraft.font.width(counterText);
        int textX = sideLeft
                ? iconX - TEXT_ICON_GAP - textWidth
                : iconX + HUD_ICON_SIZE + TEXT_ICON_GAP;
        int textY = iconY + (HUD_ICON_SIZE - minecraft.font.lineHeight) / 2 + HUD_TEXT_Y_OFFSET;
        return new DisplayLayout(iconX, iconY, textX, textY, textWidth);
    }

    private static void togglePouchTextVisibility(Minecraft minecraft) {
        showPouchText = !showPouchText;
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private static int scaledMouseX(Minecraft minecraft) {
        double screenWidth = minecraft.getWindow().getScreenWidth();
        if (screenWidth <= 0.0D) {
            return 0;
        }
        return (int) (minecraft.mouseHandler.xpos() * minecraft.getWindow().getGuiScaledWidth() / screenWidth);
    }

    private static int scaledMouseY(Minecraft minecraft) {
        double screenHeight = minecraft.getWindow().getScreenHeight();
        if (screenHeight <= 0.0D) {
            return 0;
        }
        return (int) (minecraft.mouseHandler.ypos() * minecraft.getWindow().getGuiScaledHeight() / screenHeight);
    }

    private static List<Component> buildInventoryTooltip(PouchDisplayData displayData) {
        return List.of(
                Component.translatable("tooltip.emeraldpouch.inventory.emeralds", displayData.formattedEmeraldAmount()),
                Component.translatable(
                        "tooltip.emeraldpouch.inventory.blocks_equivalent",
                        displayData.formattedFullBlockEquivalent(),
                        displayData.emeraldRemainderAfterBlocks()
                ),
                Component.translatable("tooltip.emeraldpouch.inventory.pouch_count", displayData.pouchCount())
        );
    }

    private record DisplayLayout(int iconX, int iconY, int textX, int textY, int textWidth) {
    }
}
