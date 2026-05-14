package com.jvn.emeraldpouch.menu;

import com.jvn.emeraldpouch.pouch.PouchData;
import com.jvn.emeraldpouch.pouch.PouchItemContainer;
import com.jvn.emeraldpouch.pouch.PouchInventoryAccess;
import com.jvn.emeraldpouch.pouch.PouchStackReference;
import com.jvn.emeraldpouch.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PouchMenu extends AbstractContainerMenu {
    public static final int BUTTON_TOGGLE_AUTO_COMPACT = 0;
    public static final int BUTTON_TOGGLE_AUTO_PICKUP = 1;

    private static final int HOTBAR_SLOT_COUNT = 9;

    private final Inventory playerInventory;
    private final PouchItemContainer pouchContainer;
    private final PouchStackReference pouchReference;
    private final int lockedInventorySlot;
    private final int storageSlotCount;
    private final int storageRows;
    private final ContainerData toggleData;

    public PouchMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(
                containerId,
                playerInventory,
                PouchStackReference.Type.fromNetworkId(extraData.readVarInt()),
                extraData.readVarInt(),
                extraData.readVarInt()
        );
    }

    public PouchMenu(
            int containerId,
            Inventory playerInventory,
            PouchStackReference.Type pouchSourceType,
            int pouchSourceSlot,
            int storageSlotCount
    ) {
        super(ModMenus.POUCH_MENU.get(), containerId);
        this.playerInventory = playerInventory;
        this.pouchReference = new PouchStackReference(pouchSourceType, pouchSourceSlot);
        this.lockedInventorySlot = pouchSourceType == PouchStackReference.Type.INVENTORY ? pouchSourceSlot : -1;
        this.storageSlotCount = Math.max(0, storageSlotCount);
        this.storageRows = Math.max(1, (this.storageSlotCount + 8) / 9);

        ItemStack pouchStack = getCurrentPouchStack();
        this.pouchContainer = new PouchItemContainer(pouchStack, this.storageSlotCount);
        this.pouchContainer.setChangeListener(this::onPouchContainerChanged);

        this.toggleData = new SimpleContainerData(2);
        addDataSlots(this.toggleData);
        syncToggleDataFromStack();

        addPouchSlots();
        addPlayerSlots();
    }

    public int storageRows() {
        return storageRows;
    }

    public int storageSlotCount() {
        return storageSlotCount;
    }

    public boolean isAutoCompactEnabled() {
        return toggleData.get(0) != 0;
    }

    public boolean isAutoPickupEnabled() {
        return toggleData.get(1) != 0;
    }

    @Override
    public boolean stillValid(Player player) {
        if (!player.isAlive()) {
            return false;
        }

        ItemStack currentStack = getCurrentPouchStack();
        ItemStack backingStack = this.pouchContainer.getPouchStack();
        return !currentStack.isEmpty()
                && ItemStack.isSameItemSameComponents(currentStack, backingStack)
                && PouchData.getSlotCount(currentStack) == this.storageSlotCount;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        ItemStack pouchStack = getCurrentPouchStack();
        if (!PouchData.isPouchStack(pouchStack)) {
            return false;
        }

        if (id == BUTTON_TOGGLE_AUTO_COMPACT) {
            PouchData.toggleAutoCompact(pouchStack);
            PouchInventoryAccess.commitPouchStack(this.playerInventory, this.pouchReference, pouchStack);
            syncToggleDataFromStack();
            return true;
        }

        if (id == BUTTON_TOGGLE_AUTO_PICKUP) {
            PouchData.toggleAutoPickup(pouchStack);
            PouchInventoryAccess.commitPouchStack(this.playerInventory, this.pouchReference, pouchStack);
            syncToggleDataFromStack();
            return true;
        }

        return false;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (clickType == ClickType.SWAP && button == this.lockedInventorySlot) {
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public void removed(Player player) {
        ItemStack pouchStack = getCurrentPouchStack();
        if (PouchData.isPouchStack(pouchStack)) {
            PouchData.setOpenedVisualEnabled(pouchStack, false);
            PouchInventoryAccess.commitPouchStack(this.playerInventory, this.pouchReference, pouchStack);
        }
        super.removed(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= this.slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack slotStack = slot.getItem();
        ItemStack originalCopy = slotStack.copy();
        if (index < this.storageSlotCount) {
            if (!moveItemStackTo(slotStack, this.storageSlotCount, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!PouchData.isAllowedContent(slotStack)) {
                return ItemStack.EMPTY;
            }

            if (!moveItemStackTo(slotStack, 0, this.storageSlotCount, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (slotStack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (slotStack.getCount() == originalCopy.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, slotStack);
        return originalCopy;
    }

    private void addPouchSlots() {
        for (int row = 0; row < storageRows; row++) {
            for (int column = 0; column < 9; column++) {
                int slot = column + row * 9;
                if (slot >= storageSlotCount) {
                    return;
                }

                addSlot(new PouchStorageSlot(pouchContainer, slot, 8 + column * 18, 18 + row * 18));
            }
        }
    }

    private void addPlayerSlots() {
        int verticalOffset = (this.storageRows - 4) * 18;

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int slot = column + row * 9 + HOTBAR_SLOT_COUNT;
                int x = 8 + column * 18;
                int y = 103 + row * 18 + verticalOffset;
                addSlot(new LockedPlayerInventorySlot(playerInventory, slot, x, y, lockedInventorySlot));
            }
        }

        for (int slot = 0; slot < HOTBAR_SLOT_COUNT; slot++) {
            int x = 8 + slot * 18;
            int y = 161 + verticalOffset;
            addSlot(new LockedPlayerInventorySlot(playerInventory, slot, x, y, lockedInventorySlot));
        }
    }

    private ItemStack getCurrentPouchStack() {
        return PouchInventoryAccess.getPouchStack(this.playerInventory, this.pouchReference);
    }

    private void syncToggleDataFromStack() {
        ItemStack pouchStack = getCurrentPouchStack();
        this.toggleData.set(0, PouchData.isAutoCompactEnabled(pouchStack) ? 1 : 0);
        this.toggleData.set(1, PouchData.isAutoPickupEnabled(pouchStack) ? 1 : 0);
    }

    private void onPouchContainerChanged() {
        PouchInventoryAccess.commitPouchStack(this.playerInventory, this.pouchReference, this.pouchContainer.getPouchStack());
        this.slotsChanged(this.pouchContainer);
    }
}
