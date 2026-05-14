package com.jvn.emeraldpouch.fabric.event;

import com.jvn.emeraldpouch.pouch.PouchData;
import com.jvn.emeraldpouch.pouch.PouchInventoryAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class FabricAutoPickupHandler {
    private FabricAutoPickupHandler() {
    }

    public static void onPickupItem(Player player, ItemEntity itemEntity, ItemStack originalStack) {
        if (player.level().isClientSide() || !PouchData.isAllowedContent(originalStack)) {
            return;
        }

        Inventory inventory = player.getInventory();
        if (!PouchInventoryAccess.hasAutoPickupPouch(inventory)) {
            return;
        }

        int pickedCount = Math.max(0, originalStack.getCount() - itemEntity.getItem().getCount());
        if (pickedCount <= 0) {
            return;
        }

        ItemStack extracted = PouchInventoryAccess.extractMatching(inventory, originalStack, pickedCount);
        if (extracted.isEmpty()) {
            return;
        }

        ItemStack remainder = PouchInventoryAccess.insertIntoAutoPickupPouches(inventory, extracted);
        if (!remainder.isEmpty()) {
            inventory.placeItemBackInInventory(remainder);
        }
    }
}