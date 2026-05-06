package com.jvn.emeraldpouch.event;

import com.jvn.emeraldpouch.pouch.PouchInventoryAccess;
import com.jvn.emeraldpouch.util.StackHelper;
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
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.TradeWithVillagerEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public final class MerchantTradeHandler {
    private static final String TRADE_CONTAINER_DEV_NAME = "tradeContainer";
    private static final String TRADE_CONTAINER_SRG_NAME = "f_40028_";
    private static final String SELECTION_HINT_DEV_NAME = "selectionHint";
    private static final String SELECTION_HINT_SRG_NAME = "f_40000_";
    private static final Field TRADE_CONTAINER_FIELD =
            findField(MerchantMenu.class, TRADE_CONTAINER_DEV_NAME, TRADE_CONTAINER_SRG_NAME);
    private static final Field SELECTION_HINT_FIELD =
            findField(MerchantContainer.class, SELECTION_HINT_DEV_NAME, SELECTION_HINT_SRG_NAME);
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

        depositRemainingEmeralds(serverPlayer, session.pouchEmeraldBalance());
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
        if (tradeContainer == null) {
            return;
        }

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

        refillCurrentSelection(player, merchantMenu);
    }

    private static void refillCurrentSelection(ServerPlayer player, MerchantMenu merchantMenu) {
        MerchantContainer tradeContainer = getTradeContainer(merchantMenu);
        if (tradeContainer == null) {
            return;
        }

        int selectionHint = getSelectionHint(tradeContainer);
        if (selectionHint < 0 || selectionHint >= merchantMenu.getOffers().size()) {
            return;
        }

        merchantMenu.tryMoveItems(selectionHint);
        onTradeSelectionClick(player);
        merchantMenu.broadcastChanges();
    }

    private static void depositRemainingEmeralds(ServerPlayer player, int pouchEmeraldBalance) {
        if (pouchEmeraldBalance <= 0) {
            return;
        }

        Inventory inventory = player.getInventory();
        ItemStack extracted = PouchInventoryAccess.extractMatching(inventory, new ItemStack(Items.EMERALD), pouchEmeraldBalance);
        if (extracted.isEmpty()) {
            return;
        }

        ItemStack remainder = PouchInventoryAccess.insertIntoPouches(inventory, extracted);
        if (!remainder.isEmpty()) {
            inventory.placeItemBackInInventory(remainder);
        }

        // This runs during merchant menu teardown, after vanilla returns payment slots to the
        // player inventory. Force a fresh inventory sync so the client does not keep a ghost stack.
        player.inventoryMenu.broadcastFullState();
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
        if (!current.isEmpty() && !StackHelper.sameItemData(current, cost)) {
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

        ItemStack updated = current.isEmpty() ? StackHelper.copyWithCount(cost, extracted.getCount()) : current.copy();
        if (!current.isEmpty()) {
            updated.grow(extracted.getCount());
        }
        tradeContainer.setItem(slotIndex, updated);
        return extracted.getCount();
    }

    private static MerchantContainer getTradeContainer(MerchantMenu merchantMenu) {
        if (TRADE_CONTAINER_FIELD == null) {
            return null;
        }

        try {
            return (MerchantContainer) TRADE_CONTAINER_FIELD.get(merchantMenu);
        } catch (IllegalAccessException exception) {
            return null;
        }
    }

    private static int getSelectionHint(MerchantContainer tradeContainer) {
        if (SELECTION_HINT_FIELD == null) {
            return -1;
        }

        try {
            return SELECTION_HINT_FIELD.getInt(tradeContainer);
        } catch (IllegalAccessException exception) {
            return -1;
        }
    }

    private static Field findField(Class<?> type, String... fieldNames) {
        for (String fieldName : fieldNames) {
            try {
                return ObfuscationReflectionHelper.findField(type, fieldName);
            } catch (RuntimeException ignored) {
                try {
                    Field field = type.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    return field;
                } catch (ReflectiveOperationException ignoredAgain) {
                    // Try the next candidate name.
                }
            }
        }

        return null;
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
