package com.jvn.emeraldpouch.event;

import com.jvn.emeraldpouch.pouch.PouchData;
import com.jvn.emeraldpouch.pouch.PouchInventoryAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

public final class AutoPickupHandler {
    private AutoPickupHandler() {
    }

    public static void onItemEntityPickup(EntityItemPickupEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        ItemEntity itemEntity = event.getItem();
        ItemStack entityStack = itemEntity.getItem();
        if (!PouchData.isAllowedContent(entityStack)) {
            return;
        }

        Inventory inventory = event.getEntity().getInventory();
        if (!PouchInventoryAccess.hasAutoPickupPouch(inventory)) {
            return;
        }

        ItemStack remainder = PouchInventoryAccess.insertIntoAutoPickupPouches(inventory, entityStack.copy());
        int inserted = entityStack.getCount() - remainder.getCount();
        if (inserted <= 0) {
            return;
        }

        if (remainder.isEmpty()) {
            itemEntity.discard();
            event.setCanceled(true);
        } else {
            itemEntity.setItem(remainder);
        }
    }
}
