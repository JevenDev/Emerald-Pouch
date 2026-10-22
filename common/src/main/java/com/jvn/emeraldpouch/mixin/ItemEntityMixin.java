package com.jvn.emeraldpouch.mixin;

import com.jvn.emeraldpouch.pouch.PouchData;
import com.jvn.emeraldpouch.pouch.PouchInventoryAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Unique
    private int emeraldPouch$pickedCount;

    @Redirect(
            method = "playerTouch",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z")
    )
    private boolean emeraldPouch$addToInventory(Inventory inventory, ItemStack stack) {
        int originalCount = stack.getCount();
        boolean insertedIntoPouch = false;
        if (PouchData.isAllowedContent(stack)) {
            ItemStack remainder = PouchInventoryAccess.insertIntoAutoPickupPouches(inventory, stack);
            insertedIntoPouch = remainder.getCount() < originalCount;
            stack.setCount(remainder.getCount());
        }
        boolean insertedIntoInventory = inventory.add(stack);
        this.emeraldPouch$pickedCount = insertedIntoPouch ? originalCount - stack.getCount() : -1;
        return insertedIntoPouch || insertedIntoInventory;
    }

    @ModifyArg(
            method = "playerTouch",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;take(Lnet/minecraft/world/entity/Entity;I)V"),
            index = 1
    )
    private int emeraldPouch$pickupAnimationCount(int originalCount) {
        return this.emeraldPouch$pickedCount >= 0 ? this.emeraldPouch$pickedCount : originalCount;
    }

    @ModifyArg(
            method = "playerTouch",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;awardStat(Lnet/minecraft/stats/Stat;I)V"),
            index = 1
    )
    private int emeraldPouch$pickupStatCount(int originalCount) {
        return this.emeraldPouch$pickedCount >= 0 ? this.emeraldPouch$pickedCount : originalCount;
    }
}
