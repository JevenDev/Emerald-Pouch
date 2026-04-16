package com.jvn.emeraldpouch.menu;

import com.jvn.emeraldpouch.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PouchMenu extends AbstractContainerMenu {
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_ROW_COUNT * PLAYER_INVENTORY_COLUMN_COUNT;

    private final int storageRows;

    public PouchMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, Math.max(1, extraData.readVarInt()));
    }

    public PouchMenu(int containerId, Inventory playerInventory, int storageRows) {
        super(ModMenus.POUCH_MENU.get(), containerId);
        this.storageRows = storageRows;
        addPlayerSlots(playerInventory, 8 + this.storageRows * 18 + 17);
    }

    public int storageRows() {
        return storageRows;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    private void addPlayerSlots(Inventory playerInventory, int topY) {
        int inventoryY = topY + 14;
        for (int row = 0; row < PLAYER_INVENTORY_ROW_COUNT; row++) {
            for (int column = 0; column < PLAYER_INVENTORY_COLUMN_COUNT; column++) {
                int slotIndex = column + row * PLAYER_INVENTORY_COLUMN_COUNT + HOTBAR_SLOT_COUNT;
                int x = 8 + column * 18;
                int y = inventoryY + row * 18;
                this.addSlot(new Slot(playerInventory, slotIndex, x, y));
            }
        }

        int hotbarY = inventoryY + PLAYER_INVENTORY_ROW_COUNT * 18 + 4;
        for (int slot = 0; slot < HOTBAR_SLOT_COUNT; slot++) {
            this.addSlot(new Slot(playerInventory, slot, 8 + slot * 18, hotbarY));
        }
    }
}
