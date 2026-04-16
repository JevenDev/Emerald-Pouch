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
        return openFromReference(player, PouchStackReference.inventory(inventorySlot));
    }

    public static boolean openFromReference(ServerPlayer player, PouchStackReference reference) {
        Inventory inventory = player.getInventory();
        ItemStack pouchStack = PouchInventoryAccess.getPouchStack(inventory, reference);
        if (!PouchData.isPouchStack(pouchStack)) {
            return false;
        }

        PouchInventoryAccess.clearOpenedVisualFlags(inventory);
        PouchData.setOpenedVisualEnabled(pouchStack, true);
        PouchInventoryAccess.commitPouchStack(inventory, reference, pouchStack);

        int slotCount = PouchData.getSlotCount(pouchStack);
        OptionalInt menuId = player.openMenu(
                new SimpleMenuProvider(
                        (containerId, playerInventory, menuPlayer) -> new PouchMenu(
                                containerId,
                                playerInventory,
                                reference.type(),
                                reference.slot(),
                                slotCount
                        ),
                        pouchStack.getHoverName()
                ),
                extraData -> {
                    extraData.writeVarInt(reference.type().networkId());
                    extraData.writeVarInt(reference.slot());
                    extraData.writeVarInt(slotCount);
                }
        );

        if (menuId.isEmpty()) {
            PouchData.setOpenedVisualEnabled(pouchStack, false);
            PouchInventoryAccess.commitPouchStack(inventory, reference, pouchStack);
        }

        return menuId.isPresent();
    }
}
