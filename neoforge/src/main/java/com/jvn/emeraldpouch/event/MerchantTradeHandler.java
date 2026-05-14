package com.jvn.emeraldpouch.event;

import com.jvn.emeraldpouch.pouch.PouchInventoryAccess;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent;

public final class MerchantTradeHandler {
    private static final Field TRADE_CONTAINER_FIELD = findField(MerchantMenu.class, "tradeContainer");
    private static final Field SELECTION_HINT_FIELD = findField(MerchantContainer.class, "selectionHint");
    private static final Map<UUID, TradeSession> ACTIVE_TRADE_SESSIONS = new HashMap<>();

    private MerchantTradeHandler() {
    }

    public static void onContainerOpened(PlayerContainerEvent.Open event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !(event.getContainer() instanceof MerchantMenu merchantMenu)) {
            return;
        }

        ACTIVE_TRADE_SESSIONS.put(player.getUUID(), new TradeSession(merchantMenu.containerId));
    }

    public static void onContainerClosed(PlayerContainerEvent.Close event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !(event.getContainer() instanceof MerchantMenu merchantMenu)) {
            return;
        }

        TradeSession session = ACTIVE_TRADE_SESSIONS.remove(player.getUUID());
        if (session == null || session.containerId() != merchantMenu.containerId) {
            return;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        serverPlayer.server.execute(() -> {
            depositRemainingEmeralds(serverPlayer.getInventory(), session.pouchEmeraldBalance());
            syncPlayerInventory(serverPlayer);
        });
    }

    public static void onTradeWithVillager(TradeWithVillagerEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !(player.containerMenu instanceof MerchantMenu merchantMenu)) {
            return;
        }

        TradeSession session = ACTIVE_TRADE_SESSIONS.get(player.getUUID());
        if (session == null || session.containerId() != merchantMenu.containerId) {
            return;
        }

        int emeraldCost = emeraldCost(event.getMerchantOffer().getCostA()) + emeraldCost(event.getMerchantOffer().getCostB());
        session.consumePouchEmeralds(emeraldCost);

        if (session.isShiftResultRefillPending() && player instanceof ServerPlayer serverPlayer) {
            session.setShiftResultRefillPending(false);
            refillCurrentSelection(serverPlayer, merchantMenu);
        }
    }

    public static void onTradeSelectionClick(ServerPlayer player) {
        if (!(player.containerMenu instanceof MerchantMenu merchantMenu)) {
            return;
        }

        TradeSession session = ACTIVE_TRADE_SESSIONS.compute(
                player.getUUID(),
                (playerId, existingSession) -> {
                    if (existingSession != null && existingSession.containerId() == merchantMenu.containerId) {
                        return existingSession;
                    }
                    return new TradeSession(merchantMenu.containerId);
                }
        );

        MerchantContainer tradeContainer = getTradeContainer(merchantMenu);
        int selectionHint = getSelectionHint(tradeContainer);
        if (selectionHint < 0 || selectionHint >= merchantMenu.getOffers().size()) {
            return;
        }

        MerchantOffer offer = merchantMenu.getOffers().get(selectionHint);
        if (offer.isOutOfStock()) {
            return;
        }

        int pouchEmeraldsMoved = 0;
        pouchEmeraldsMoved += fillPaymentSlot(player, tradeContainer, 0, offer.getCostA());
        pouchEmeraldsMoved += fillPaymentSlot(player, tradeContainer, 1, offer.getCostB());
        if (pouchEmeraldsMoved > 0) {
            session.addPouchEmeralds(pouchEmeraldsMoved);
            merchantMenu.broadcastChanges();
        }
    }

    public static void onShiftTradeResultClick(ServerPlayer player) {
        if (!(player.containerMenu instanceof MerchantMenu merchantMenu)) {
            return;
        }

        TradeSession session = ACTIVE_TRADE_SESSIONS.compute(
                player.getUUID(),
                (playerId, existingSession) -> {
                    if (existingSession != null && existingSession.containerId() == merchantMenu.containerId) {
                        return existingSession;
                    }
                    return new TradeSession(merchantMenu.containerId);
                }
        );
        session.setShiftResultRefillPending(true);

        // Best-effort immediate refill in case this packet arrives after the trade click packet.
        refillCurrentSelection(player, merchantMenu);
    }

    private static void refillCurrentSelection(ServerPlayer player, MerchantMenu merchantMenu) {
        int selectionHint = getSelectionHint(getTradeContainer(merchantMenu));
        if (selectionHint < 0 || selectionHint >= merchantMenu.getOffers().size()) {
            return;
        }

        merchantMenu.tryMoveItems(selectionHint);
        onTradeSelectionClick(player);
        merchantMenu.broadcastChanges();
    }

    private static void depositRemainingEmeralds(Inventory inventory, int pouchEmeraldBalance) {
        if (pouchEmeraldBalance <= 0) {
            return;
        }

        ItemStack extracted = PouchInventoryAccess.extractMatching(inventory, new ItemStack(Items.EMERALD), pouchEmeraldBalance);
        if (extracted.isEmpty()) {
            return;
        }

        ItemStack remainder = PouchInventoryAccess.insertIntoPouches(inventory, extracted);
        if (!remainder.isEmpty()) {
            inventory.placeItemBackInInventory(remainder);
        }
    }

    private static void syncPlayerInventory(ServerPlayer player) {
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastFullState();
        player.inventoryMenu.sendAllDataToRemote();
    }

    private static int emeraldCost(ItemStack stack) {
        return stack.is(Items.EMERALD) ? stack.getCount() : 0;
    }

    private static int fillPaymentSlot(Player player, MerchantContainer tradeContainer, int slotIndex, ItemStack cost) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return 0;
        }

        if (cost.isEmpty() || !cost.is(Items.EMERALD)) {
            return 0;
        }

        ItemStack current = tradeContainer.getItem(slotIndex);
        if (!current.isEmpty() && !ItemStack.isSameItemSameComponents(current, cost)) {
            return 0;
        }

        int missing = cost.getCount() - current.getCount();
        if (missing <= 0) {
            return 0;
        }

        ItemStack extracted = PouchInventoryAccess.extractEmeraldsForTrade(serverPlayer.getInventory(), missing);
        if (extracted.isEmpty()) {
            return 0;
        }

        ItemStack updated = current.isEmpty() ? cost.copyWithCount(extracted.getCount()) : current.copy();
        if (!current.isEmpty()) {
            updated.grow(extracted.getCount());
        }
        tradeContainer.setItem(slotIndex, updated);
        return extracted.getCount();
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

    private static final class TradeSession {
        private final int containerId;
        private int pouchEmeraldBalance;
        private boolean shiftResultRefillPending;

        private TradeSession(int containerId) {
            this.containerId = containerId;
        }

        public int containerId() {
            return this.containerId;
        }

        public int pouchEmeraldBalance() {
            return this.pouchEmeraldBalance;
        }

        public void addPouchEmeralds(int amount) {
            if (amount > 0) {
                this.pouchEmeraldBalance += amount;
            }
        }

        public void consumePouchEmeralds(int amount) {
            if (amount > 0) {
                this.pouchEmeraldBalance = Math.max(0, this.pouchEmeraldBalance - amount);
            }
        }

        public boolean isShiftResultRefillPending() {
            return this.shiftResultRefillPending;
        }

        public void setShiftResultRefillPending(boolean shiftResultRefillPending) {
            this.shiftResultRefillPending = shiftResultRefillPending;
        }
    }
}
