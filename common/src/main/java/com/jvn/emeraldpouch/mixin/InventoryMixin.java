package com.jvn.emeraldpouch.mixin;

import com.jvn.emeraldpouch.pouch.PouchDeathRetention;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public abstract class InventoryMixin {
    @Unique
    private final Map<Integer, ItemStack> emeraldpouch$retained = new HashMap<>();

    @Inject(method = "dropAll", at = @At("HEAD"))
    private void emeraldpouch$keepPouches(CallbackInfo ci) {
        Inventory inventory = (Inventory) (Object) this;
        if (inventory.player.level().isClientSide()) return;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (PouchDeathRetention.shouldKeep(stack)) {
                emeraldpouch$retained.put(slot, stack);
                inventory.setItem(slot, ItemStack.EMPTY);
            }
        }
    }

    @Inject(method = "dropAll", at = @At("RETURN"))
    private void emeraldpouch$returnPouches(CallbackInfo ci) {
        Inventory inventory = (Inventory) (Object) this;
        // keep retained stacks in player data so disconnecting before respawn is safe
        emeraldpouch$retained.forEach(inventory::setItem);
        emeraldpouch$retained.clear();
    }
}
