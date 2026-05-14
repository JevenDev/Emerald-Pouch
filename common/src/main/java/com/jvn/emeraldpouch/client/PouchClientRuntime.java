package com.jvn.emeraldpouch.client;

import com.jvn.emeraldpouch.EmeraldPouchMod;
import com.jvn.emeraldpouch.network.MerchantTradeClickPayload;
import com.jvn.emeraldpouch.pouch.PouchData;
import com.jvn.toucanlib.util.ToucanResourceLocations;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import java.lang.reflect.Field;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;

public final class PouchClientRuntime {
    private static final ResourceLocation EMERALD_POUCH_HUD_TEXTURE = id("textures/gui/hud/emerald_pouch.png");
    private static final ResourceLocation BLACK_POUCH_HUD_TEXTURE = id("textures/gui/hud/black_emerald_pouch.png");
    private static final ResourceLocation BLUE_POUCH_HUD_TEXTURE = id("textures/gui/hud/blue_emerald_pouch.png");
    private static final ResourceLocation BROWN_POUCH_HUD_TEXTURE = id("textures/gui/hud/brown_emerald_pouch.png");
    private static final ResourceLocation CYAN_POUCH_HUD_TEXTURE = id("textures/gui/hud/cyan_emerald_pouch.png");
    private static final ResourceLocation GRAY_POUCH_HUD_TEXTURE = id("textures/gui/hud/gray_emerald_pouch.png");
    private static final ResourceLocation GREEN_POUCH_HUD_TEXTURE = id("textures/gui/hud/green_emerald_pouch.png");
    private static final ResourceLocation LIGHT_BLUE_POUCH_HUD_TEXTURE = id("textures/gui/hud/light_blue_emerald_pouch.png");
    private static final ResourceLocation LIGHT_GRAY_POUCH_HUD_TEXTURE = id("textures/gui/hud/light_gray_emerald_pouch.png");
    private static final ResourceLocation LIME_POUCH_HUD_TEXTURE = id("textures/gui/hud/lime_emerald_pouch.png");
    private static final ResourceLocation MAGENTA_POUCH_HUD_TEXTURE = id("textures/gui/hud/magenta_emerald_pouch.png");
    private static final ResourceLocation ORANGE_POUCH_HUD_TEXTURE = id("textures/gui/hud/orange_emerald_pouch.png");
    private static final ResourceLocation PINK_POUCH_HUD_TEXTURE = id("textures/gui/hud/pink_emerald_pouch.png");
    private static final ResourceLocation PURPLE_POUCH_HUD_TEXTURE = id("textures/gui/hud/purple_emerald_pouch.png");
    private static final ResourceLocation RED_POUCH_HUD_TEXTURE = id("textures/gui/hud/red_emerald_pouch.png");
    private static final ResourceLocation WHITE_POUCH_HUD_TEXTURE = id("textures/gui/hud/white_emerald_pouch.png");
    private static final ResourceLocation YELLOW_POUCH_HUD_TEXTURE = id("textures/gui/hud/yellow_emerald_pouch.png");
    private static final ResourceLocation POUCH_HUD_HOVER_TEXTURE = id("textures/gui/hud/emerald_pouch_hover.png");
    private static final int HUD_ICON_SIZE = 16;
    private static final int XP_TEXT_COLOR = 8453920;
    private static final int WHITE_TEXT_COLOR = 16777215;
    private static final int TEXT_ICON_GAP = 1;
    private static final int HOTBAR_HALF_WIDTH = 91;
    private static final int POSITION_1_SIDE_MARGIN = 1;
    private static final int POSITION_2_SIDE_MARGIN = 1;
    private static final int POSITION_3_SIDE_MARGIN = 1;
    private static final int POSITION_1_Y_FROM_BOTTOM = 47;
    private static final int POSITION_2_Y_OFFSET = -3;
    private static final int POSITION_3_Y_FROM_BOTTOM = 19;
    private static final int INVENTORY_ICON_RIGHT_MARGIN = 6;
    private static final int INVENTORY_ICON_TOP_MARGIN = 62;
    private static final int HUD_TEXT_Y_OFFSET = 2;
    private static final int INVENTORY_TEXT_Y_OFFSET = 2;
    private static final int BUNDLE_OVERLAY_TEXT_X_OFFSET = 4;
    private static final int BUNDLE_OVERLAY_TEXT_Y_OFFSET = 4;
    private static final int MERCHANT_TRADE_BUTTON_X = 5;
    private static final int MERCHANT_TRADE_BUTTON_Y = 18;
    private static final int MERCHANT_TRADE_BUTTON_WIDTH = 88;
    private static final int MERCHANT_TRADE_BUTTON_HEIGHT = 20;
    private static final int MERCHANT_VISIBLE_TRADE_ROWS = 7;
    private static final int MERCHANT_RESULT_SLOT_X = 220;
    private static final int MERCHANT_RESULT_SLOT_Y = 37;
    private static final int SLOT_SIZE = 16;
    private static final Field ABSTRACT_CONTAINER_LEFT_POS_FIELD = findField(AbstractContainerScreen.class, "leftPos");
    private static final Field ABSTRACT_CONTAINER_TOP_POS_FIELD = findField(AbstractContainerScreen.class, "topPos");
    private static final Field ABSTRACT_CONTAINER_IMAGE_WIDTH_FIELD = findField(AbstractContainerScreen.class, "imageWidth");
    private static final Field MERCHANT_TRADE_CONTAINER_FIELD = findField(MerchantMenu.class, "tradeContainer");
    private static final Field MERCHANT_SCROLL_OFFSET_FIELD = findField(MerchantScreen.class, "scrollOff");
    private static final Field MERCHANT_SELECTION_HINT_FIELD = findField(MerchantContainer.class, "selectionHint");
    private static boolean showPouchText = true;
    private static boolean merchantResultClicked;
    private static boolean shiftClickedMerchantResult;

    private PouchClientRuntime() {
    }

    public static void onClientTick(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null) {
            return;
        }

        while (ModKeyMappings.OPEN_FIRST_POUCH.consumeClick()) {
            PouchClientNetworking.sendOpenFirstPouch();
        }
    }

    public static void onRenderHud(Minecraft minecraft, GuiGraphics guiGraphics) {
        Player player = minecraft.player;
        if (player == null || minecraft.level == null || minecraft.options.hideGui) {
            return;
        }

        PouchDisplayData displayData = PouchDisplayData.fromPlayerInventory(player);
        if (!displayData.hasPouches()) {
            return;
        }

        renderHudPouchCounter(
                guiGraphics,
                minecraft,
                displayData.compactEmeraldAmount(),
                displayData.pouchCount(),
                player.getMainArm()
        );
    }

    public static void onScreenRenderPost(Minecraft minecraft, Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!(screen instanceof AbstractContainerScreen<?> containerScreen)
                || !(containerScreen instanceof InventoryScreen || containerScreen instanceof MerchantScreen)) {
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

        String counterText = displayData.compactEmeraldAmount();
        DisplayLayout layout = computeInventoryLayout(minecraft, containerScreen, counterText);
        boolean hoverIcon = isHovered(mouseX, mouseY, layout.iconX(), layout.iconY(), HUD_ICON_SIZE, HUD_ICON_SIZE);
        renderIconAndMaybeText(guiGraphics, minecraft, counterText, displayData.pouchCount(), layout, hudIconTexture(), hoverIcon);

        boolean hoverText = showPouchText
                && isHovered(mouseX, mouseY, layout.textX(), layout.textY(), layout.textWidth(), minecraft.font.lineHeight);
        if (hoverIcon || hoverText) {
            guiGraphics.renderTooltip(minecraft.font, buildInventoryTooltip(displayData), java.util.Optional.empty(), mouseX, mouseY);
        }
    }

    public static boolean onScreenMouseButtonPressedPre(Minecraft minecraft, Screen screen, double mouseX, double mouseY, int button) {
        merchantResultClicked = false;
        shiftClickedMerchantResult = false;

        if (button != InputConstants.MOUSE_BUTTON_LEFT) {
            return true;
        }

        if (screen instanceof MerchantScreen merchantScreen) {
            int rx = (int) mouseX - screenLeft(merchantScreen);
            int ry = (int) mouseY - screenTop(merchantScreen);
            if (rx >= MERCHANT_RESULT_SLOT_X && rx < MERCHANT_RESULT_SLOT_X + SLOT_SIZE
                    && ry >= MERCHANT_RESULT_SLOT_Y && ry < MERCHANT_RESULT_SLOT_Y + SLOT_SIZE
                    && merchantScreen.getMenu().getSlot(2).hasItem()) {
                merchantResultClicked = true;
                shiftClickedMerchantResult = Screen.hasShiftDown();
            }
        }

        if (!(screen instanceof AbstractContainerScreen<?> containerScreen)
                || !(containerScreen instanceof InventoryScreen || containerScreen instanceof MerchantScreen)) {
            return true;
        }

        Player player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return true;
        }

        PouchDisplayData displayData = PouchDisplayData.fromPlayerInventory(player);
        if (!displayData.hasPouches()) {
            return true;
        }

        DisplayLayout layout = computeInventoryLayout(minecraft, containerScreen, displayData.compactEmeraldAmount());
        if (!isHovered(mouseX, mouseY, layout.iconX(), layout.iconY(), HUD_ICON_SIZE, HUD_ICON_SIZE)) {
            return true;
        }

        togglePouchTextVisibility(minecraft);
        return false;
    }

    public static void onScreenMouseButtonPressedPost(
            Minecraft minecraft,
            Screen screen,
            double mouseX,
            double mouseY,
            int button,
            boolean clickHandled
    ) {
        if (button != InputConstants.MOUSE_BUTTON_LEFT || !clickHandled) {
            return;
        }

        if (!(screen instanceof MerchantScreen merchantScreen)) {
            return;
        }

        if (merchantResultClicked) {
            merchantResultClicked = false;
            int selectionHint = getMerchantSelectionHint(merchantScreen);
            if (selectionHint < 0 || selectionHint >= merchantScreen.getMenu().getOffers().size()) {
                shiftClickedMerchantResult = false;
                return;
            }

            PouchClientNetworking.sendMerchantTradeClick(
                    shiftClickedMerchantResult
                            ? MerchantTradeClickPayload.ClickType.SHIFT_RESULT
                            : MerchantTradeClickPayload.ClickType.RESULT,
                    selectionHint
            );
            shiftClickedMerchantResult = false;
            return;
        }

        int relativeX = (int) mouseX - screenLeft(merchantScreen);
        int relativeY = (int) mouseY - screenTop(merchantScreen);
        if (relativeX < MERCHANT_TRADE_BUTTON_X || relativeX >= MERCHANT_TRADE_BUTTON_X + MERCHANT_TRADE_BUTTON_WIDTH) {
            return;
        }

        int listHeight = MERCHANT_VISIBLE_TRADE_ROWS * MERCHANT_TRADE_BUTTON_HEIGHT;
        if (relativeY < MERCHANT_TRADE_BUTTON_Y || relativeY >= MERCHANT_TRADE_BUTTON_Y + listHeight) {
            return;
        }

        int row = (relativeY - MERCHANT_TRADE_BUTTON_Y) / MERCHANT_TRADE_BUTTON_HEIGHT;
        int offerIndex = row + getMerchantScrollOffset(merchantScreen);
        if (offerIndex < 0 || offerIndex >= merchantScreen.getMenu().getOffers().size()) {
            return;
        }

        PouchClientNetworking.sendMerchantTradeClick(MerchantTradeClickPayload.ClickType.SELECTION, offerIndex);
    }

    public static boolean onMouseButtonInputPre(Minecraft minecraft, int button, int action) {
        if (button != InputConstants.MOUSE_BUTTON_LEFT || action != InputConstants.PRESS) {
            return false;
        }

        if (minecraft.screen != null || minecraft.options.hideGui) {
            return false;
        }

        Player player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return false;
        }

        PouchDisplayData displayData = PouchDisplayData.fromPlayerInventory(player);
        if (!displayData.hasPouches()) {
            return false;
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
            return false;
        }

        togglePouchTextVisibility(minecraft);
        return true;
    }

    public static float openedProperty(ItemStack stack, ClientLevel level, LivingEntity entity, int seed) {
        return PouchData.isOpenedVisualEnabled(stack) ? 1.0F : 0.0F;
    }

    private static void renderHudPouchCounter(
            GuiGraphics guiGraphics,
            Minecraft minecraft,
            String counterText,
            int pouchCount,
            HumanoidArm mainArm
    ) {
        DisplayLayout layout = computeHudLayout(minecraft, guiGraphics.guiWidth(), guiGraphics.guiHeight(), counterText, mainArm);
        int mouseX = scaledMouseX(minecraft);
        int mouseY = scaledMouseY(minecraft);
        boolean hoverIcon = isHovered(mouseX, mouseY, layout.iconX(), layout.iconY(), HUD_ICON_SIZE, HUD_ICON_SIZE);
        renderIconAndMaybeText(guiGraphics, minecraft, counterText, pouchCount, layout, hudIconTexture(), hoverIcon);
    }

    private static void renderIconAndMaybeText(
            GuiGraphics guiGraphics,
            Minecraft minecraft,
            String text,
            int pouchCount,
            DisplayLayout layout,
            ResourceLocation baseTexture,
            boolean hoverIcon
    ) {
        guiGraphics.blit(baseTexture, layout.iconX(), layout.iconY(), 0.0F, 0.0F, HUD_ICON_SIZE, HUD_ICON_SIZE, HUD_ICON_SIZE, HUD_ICON_SIZE);

        if (hoverIcon) {
            drawHoverOverlay(guiGraphics, layout.iconX(), layout.iconY());
        }

        if (showPouchText) {
            drawXpStyleText(guiGraphics, minecraft, text, layout.textX(), layout.textY());
        }

        if (PouchClientSettings.showBundleCountOverlay()) {
            String bundleText = Integer.toString(pouchCount);
            int bundleTextX = layout.iconX() + (HUD_ICON_SIZE - minecraft.font.width(bundleText)) / 2 + BUNDLE_OVERLAY_TEXT_X_OFFSET;
            int bundleTextY = layout.iconY() + (HUD_ICON_SIZE - minecraft.font.lineHeight) / 2 + BUNDLE_OVERLAY_TEXT_Y_OFFSET;
            drawOutlinedText(guiGraphics, minecraft, bundleText, bundleTextX, bundleTextY, WHITE_TEXT_COLOR);
        }
    }

    private static void drawXpStyleText(GuiGraphics guiGraphics, Minecraft minecraft, String text, int x, int y) {
        drawOutlinedText(guiGraphics, minecraft, text, x, y, XP_TEXT_COLOR);
    }

    private static void drawOutlinedText(GuiGraphics guiGraphics, Minecraft minecraft, String text, int x, int y, int color) {
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

    private static void drawHoverOverlay(GuiGraphics guiGraphics, int iconX, int iconY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F);
        guiGraphics.blit(POUCH_HUD_HOVER_TEXTURE, iconX, iconY, 0.0F, 0.0F, HUD_ICON_SIZE, HUD_ICON_SIZE, HUD_ICON_SIZE, HUD_ICON_SIZE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private static ResourceLocation hudIconTexture() {
        return switch (PouchClientSettings.hudIconColor()) {
            case EMERALD -> EMERALD_POUCH_HUD_TEXTURE;
            case BLACK -> BLACK_POUCH_HUD_TEXTURE;
            case BLUE -> BLUE_POUCH_HUD_TEXTURE;
            case BROWN -> BROWN_POUCH_HUD_TEXTURE;
            case CYAN -> CYAN_POUCH_HUD_TEXTURE;
            case GRAY -> GRAY_POUCH_HUD_TEXTURE;
            case GREEN -> GREEN_POUCH_HUD_TEXTURE;
            case LIGHT_BLUE -> LIGHT_BLUE_POUCH_HUD_TEXTURE;
            case LIGHT_GRAY -> LIGHT_GRAY_POUCH_HUD_TEXTURE;
            case LIME -> LIME_POUCH_HUD_TEXTURE;
            case MAGENTA -> MAGENTA_POUCH_HUD_TEXTURE;
            case ORANGE -> ORANGE_POUCH_HUD_TEXTURE;
            case PINK -> PINK_POUCH_HUD_TEXTURE;
            case PURPLE -> PURPLE_POUCH_HUD_TEXTURE;
            case RED -> RED_POUCH_HUD_TEXTURE;
            case WHITE -> WHITE_POUCH_HUD_TEXTURE;
            case YELLOW -> YELLOW_POUCH_HUD_TEXTURE;
        };
    }

    private static DisplayLayout computeInventoryLayout(Minecraft minecraft, AbstractContainerScreen<?> screen, String counterText) {
        int iconX = screenLeft(screen) + screenImageWidth(screen) - INVENTORY_ICON_RIGHT_MARGIN - HUD_ICON_SIZE;
        int iconY = screenTop(screen) + INVENTORY_ICON_TOP_MARGIN;
        int textWidth = minecraft.font.width(counterText);
        int textX = iconX - TEXT_ICON_GAP - textWidth;
        int textY = iconY + (HUD_ICON_SIZE - minecraft.font.lineHeight) / 2 + INVENTORY_TEXT_Y_OFFSET;
        return new DisplayLayout(iconX, iconY, textX, textY, textWidth);
    }

    private static DisplayLayout computeHudLayout(Minecraft minecraft, int guiWidth, int guiHeight, String counterText, HumanoidArm mainArm) {
        int centerX = guiWidth / 2;
        PouchClientSettings.HudPosition hudPosition = PouchClientSettings.hudPosition();
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
            case POSITION_2 -> guiHeight - guiRightHeight(minecraft) + 10 + POSITION_2_Y_OFFSET;
            case POSITION_3 -> guiHeight - POSITION_3_Y_FROM_BOTTOM;
        };

        int textWidth = minecraft.font.width(counterText);
        int textX = sideLeft ? iconX - TEXT_ICON_GAP - textWidth : iconX + HUD_ICON_SIZE + TEXT_ICON_GAP;
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

    private static int getMerchantScrollOffset(MerchantScreen screen) {
        try {
            return MERCHANT_SCROLL_OFFSET_FIELD.getInt(screen);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to read merchant screen scroll offset", exception);
        }
    }

    private static int getMerchantSelectionHint(MerchantScreen screen) {
        try {
            MerchantContainer tradeContainer = (MerchantContainer) MERCHANT_TRADE_CONTAINER_FIELD.get(screen.getMenu());
            return MERCHANT_SELECTION_HINT_FIELD.getInt(tradeContainer);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to read merchant selection hint", exception);
        }
    }

    private static int screenLeft(AbstractContainerScreen<?> screen) {
        try {
            return ABSTRACT_CONTAINER_LEFT_POS_FIELD.getInt(screen);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to read container screen left position", exception);
        }
    }

    private static int screenTop(AbstractContainerScreen<?> screen) {
        try {
            return ABSTRACT_CONTAINER_TOP_POS_FIELD.getInt(screen);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to read container screen top position", exception);
        }
    }

    private static int screenImageWidth(AbstractContainerScreen<?> screen) {
        try {
            return ABSTRACT_CONTAINER_IMAGE_WIDTH_FIELD.getInt(screen);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to read container screen width", exception);
        }
    }

    private static int guiRightHeight(Minecraft minecraft) {
        return readIntField(minecraft.gui, "rightHeight", "Unable to read HUD right height");
    }

    private static Field findField(Class<?> type, String fieldName) {
        try {
            Field field = type.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to resolve field " + type.getSimpleName() + "." + fieldName, exception);
        }
    }

    private static int readIntField(Object target, String fieldName, String errorMessage) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getInt(target);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(errorMessage, exception);
        }
    }

    private static ResourceLocation id(String path) {
        return ToucanResourceLocations.id(EmeraldPouchMod.MOD_ID, path);
    }

    private record DisplayLayout(int iconX, int iconY, int textX, int textY, int textWidth) {
    }
}