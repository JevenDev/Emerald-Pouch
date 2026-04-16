package com.jvn.emeraldpouch.pouch;

import com.jvn.emeraldpouch.menu.PouchMenu;
import java.util.OptionalInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;

public final class PouchMenuOpener {
    private PouchMenuOpener() {
    }

    public static boolean openFromHand(ServerPlayer player, InteractionHand usedHand) {
        int slot = usedHand == InteractionHand.MAIN_HAND ? player.getInventory().selected : Inventory.SLOT_OFFHAND;
        return openFromInventorySlot(player, slot);
    }

    public static boolean openFromInventorySlot(ServerPlayer player, int inventorySlot) {
        ItemStack pouchStack = player.getInventory().getItem(inventorySlot);
        if (!PouchData.isPouchStack(pouchStack)) {
            return false;
        }

        int slotCount = PouchData.getSlotCount(pouchStack);
        OptionalInt menuId = player.openMenu(
                new SimpleMenuProvider(
                        (containerId, playerInventory, menuPlayer) -> new PouchMenu(containerId, playerInventory, inventorySlot, slotCount),
                        pouchStack.getHoverName()
                ),
                extraData -> {
                    extraData.writeVarInt(inventorySlot);
                    extraData.writeVarInt(slotCount);
                }
        );

        return menuId.isPresent();
    }
}
