package com.jvn.emeraldpouch.compat;

import com.jvn.toucanlib.util.toucanResourceLocations;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public final class CuriosCompat {
    private static final EntityCapability<IItemHandler, Void> CURIOS_INVENTORY = EntityCapability.createVoid(
            toucanResourceLocations.id("curios", "item_handler"),
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

    public static boolean tryEquipFromHand(Player player, InteractionHand hand) {
        IItemHandler handler = getHandler(player);
        if (handler == null) {
            return false;
        }

        ItemStack handStack = player.getItemInHand(hand);
        if (handStack.isEmpty()) {
            return false;
        }

        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack simulatedRemainder = handler.insertItem(slot, handStack.copy(), true);
            if (simulatedRemainder.getCount() == handStack.getCount()) {
                continue;
            }

            ItemStack slotStack = handler.getStackInSlot(slot);
            if (slotStack.isEmpty()) {
                ItemStack remainder = handler.insertItem(slot, handStack.copy(), false);
                if (remainder.getCount() < handStack.getCount()) {
                    player.setItemInHand(hand, remainder);
                    return true;
                }
                continue;
            }

            if (!simulatedRemainder.isEmpty()) {
                continue;
            }

            ItemStack extracted = handler.extractItem(slot, slotStack.getCount(), false);
            if (extracted.isEmpty()) {
                continue;
            }

            ItemStack insertRemainder = handler.insertItem(slot, handStack.copy(), false);
            if (!insertRemainder.isEmpty()) {
                ItemStack rollback = handler.insertItem(slot, extracted, false);
                if (!rollback.isEmpty()) {
                    player.getInventory().placeItemBackInInventory(rollback);
                }
                continue;
            }

            player.setItemInHand(hand, extracted);
            return true;
        }

        return false;
    }

    private static IItemHandler getHandler(LivingEntity entity) {
        if (!ModCompat.isCuriosLoaded()) {
            return null;
        }
        return entity.getCapability(CURIOS_INVENTORY);
    }
}
