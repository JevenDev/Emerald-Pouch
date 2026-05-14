package com.jvn.emeraldpouch.neoforge.event;

import com.jvn.emeraldpouch.pouch.PouchData;
import com.jvn.emeraldpouch.pouch.PouchInventoryAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

public final class AutoPickupHandler {
    private AutoPickupHandler() {
    }

    public static void onItemEntityPickupPost(ItemEntityPickupEvent.Post event) {
        if (event.getPlayer().level().isClientSide()) {
            return;
        }

        ItemStack originalStack = event.getOriginalStack();
        if (!PouchData.isAllowedContent(originalStack)) {
            return;
        }

        Inventory inventory = event.getPlayer().getInventory();
        if (!PouchInventoryAccess.hasAutoPickupPouch(inventory)) {
            return;
        }

        int pickedCount = originalStack.getCount() - event.getCurrentStack().getCount();
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
