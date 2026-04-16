package com.jvn.emeraldpouch.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public final class CuriosCompat {
    private static final EntityCapability<IItemHandler, Void> CURIOS_INVENTORY = EntityCapability.createVoid(
            ResourceLocation.fromNamespaceAndPath("curios", "item_handler"),
            IItemHandler.class
    );

    private CuriosCompat() {
    }

    public static int getSlotCount(LivingEntity entity) {
        IItemHandler handler = getHandler(entity);
        return handler == null ? 0 : handler.getSlots();
    }

    public static ItemStack getStackInSlot(LivingEntity entity, int slot) {
        IItemHandler handler = getHandler(entity);
        if (handler == null || slot < 0 || slot >= handler.getSlots()) {
            return ItemStack.EMPTY;
        }
        return handler.getStackInSlot(slot);
    }

    public static void setStackInSlot(LivingEntity entity, int slot, ItemStack stack) {
        IItemHandler handler = getHandler(entity);
        if (!(handler instanceof IItemHandlerModifiable modifiable) || slot < 0 || slot >= modifiable.getSlots()) {
            return;
        }
        modifiable.setStackInSlot(slot, stack);
    }

    private static IItemHandler getHandler(LivingEntity entity) {
        if (!ModCompat.isCuriosLoaded()) {
            return null;
        }
        return entity.getCapability(CURIOS_INVENTORY);
    }
}
