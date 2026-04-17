package com.jvn.emeraldpouch.event;

import com.jvn.emeraldpouch.pouch.PouchInventoryAccess;
import java.lang.reflect.Field;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class MerchantTradeHandler {
    private static final Field TRADE_CONTAINER_FIELD = findField(MerchantMenu.class, "tradeContainer");
    private static final Field SELECTION_HINT_FIELD = findField(MerchantContainer.class, "selectionHint");

    private MerchantTradeHandler() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !(player.containerMenu instanceof MerchantMenu merchantMenu)) {
            return;
        }

        MerchantContainer tradeContainer = getTradeContainer(merchantMenu);
        int selectionHint = getSelectionHint(tradeContainer);
        if (selectionHint < 0 || selectionHint >= merchantMenu.getOffers().size()) {
            return;
        }

        MerchantOffer offer = merchantMenu.getOffers().get(selectionHint);
        if (offer.isOutOfStock()) {
            return;
        }

        boolean changed = false;
        changed |= fillPaymentSlot(player, tradeContainer, 0, offer.getCostA());
        changed |= fillPaymentSlot(player, tradeContainer, 1, offer.getCostB());
        if (changed) {
            merchantMenu.broadcastChanges();
        }
    }

    private static boolean fillPaymentSlot(Player player, MerchantContainer tradeContainer, int slotIndex, ItemStack cost) {
        if (cost.isEmpty() || !cost.is(Items.EMERALD)) {
            return false;
        }

        ItemStack current = tradeContainer.getItem(slotIndex);
        if (!current.isEmpty() && !ItemStack.isSameItemSameComponents(current, cost)) {
            return false;
        }

        int missing = cost.getCount() - current.getCount();
        if (missing <= 0) {
            return false;
        }

        ItemStack extracted = PouchInventoryAccess.extractEmeraldsForTrade(player.getInventory(), missing);
        if (extracted.isEmpty()) {
            return false;
        }

        ItemStack updated = current.isEmpty() ? cost.copyWithCount(extracted.getCount()) : current.copy();
        if (!current.isEmpty()) {
            updated.grow(extracted.getCount());
        }
        tradeContainer.setItem(slotIndex, updated);
        return true;
    }

    private static MerchantContainer getTradeContainer(MerchantMenu merchantMenu) {
        try {
            return (MerchantContainer) TRADE_CONTAINER_FIELD.get(merchantMenu);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to read merchant trade container", exception);
        }
    }

    private static int getSelectionHint(MerchantContainer tradeContainer) {
        try {
            return SELECTION_HINT_FIELD.getInt(tradeContainer);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to read merchant selection hint", exception);
        }
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
}
