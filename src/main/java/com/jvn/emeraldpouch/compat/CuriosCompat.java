package com.jvn.emeraldpouch.compat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;

public final class CuriosCompat {
    private static final String CURIOS_API_CLASS = "top.theillusivec4.curios.api.CuriosApi";

    private CuriosCompat() {
    }

    public static int getSlotCount(LivingEntity entity) {
        int slots = 0;
        for (IItemHandlerModifiable handler : getCurioStackHandlers(entity)) {
            slots += handler.getSlots();
        }
        return slots;
    }

    public static ItemStack getStackInSlot(LivingEntity entity, int slot) {
        int remainingSlot = slot;
        for (IItemHandlerModifiable handler : getCurioStackHandlers(entity)) {
            if (remainingSlot < handler.getSlots()) {
                return handler.getStackInSlot(remainingSlot);
            }
            remainingSlot -= handler.getSlots();
        }
        return ItemStack.EMPTY;
    }

    public static void setStackInSlot(LivingEntity entity, int slot, ItemStack stack) {
        int remainingSlot = slot;
        for (IItemHandlerModifiable handler : getCurioStackHandlers(entity)) {
            if (remainingSlot < handler.getSlots()) {
                handler.setStackInSlot(remainingSlot, stack);
                return;
            }
            remainingSlot -= handler.getSlots();
        }
    }

    public static boolean tryEquipFromHand(Player player, InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);
        if (heldStack.isEmpty()) {
            return false;
        }

        for (IItemHandlerModifiable handler : getCurioStackHandlers(player)) {
            if (tryInsertIntoFirstAvailableSlot(player, hand, heldStack, handler)) {
                return true;
            }
        }

        return false;
    }

    private static boolean tryInsertIntoFirstAvailableSlot(
            Player player,
            InteractionHand hand,
            ItemStack heldStack,
            IItemHandlerModifiable handler
    ) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack remainder = handler.insertItem(slot, heldStack.copy(), false);
            if (remainder.getCount() >= heldStack.getCount()) {
                continue;
            }

            player.setItemInHand(hand, remainder);
            return true;
        }

        return false;
    }

    private static List<IItemHandlerModifiable> getCurioStackHandlers(LivingEntity entity) {
        if (!ModCompat.isCuriosLoaded()) {
            return List.of();
        }

        try {
            Class<?> curiosApiClass = Class.forName(CURIOS_API_CLASS);
            Method getCuriosInventory = curiosApiClass.getMethod("getCuriosInventory", LivingEntity.class);
            Object lazyOptionalObject = getCuriosInventory.invoke(null, entity);
            if (!(lazyOptionalObject instanceof LazyOptional<?> lazyOptional)) {
                return List.of();
            }

            Object curiosInventory = lazyOptional.resolve().orElse(null);
            if (curiosInventory == null) {
                return List.of();
            }

            Method getCurios = curiosInventory.getClass().getMethod("getCurios");
            Object curiosObject = getCurios.invoke(curiosInventory);
            if (!(curiosObject instanceof Map<?, ?> curiosMap)) {
                return List.of();
            }

            List<Map.Entry<?, ?>> entries = new ArrayList<>(curiosMap.entrySet());
            entries.sort(Comparator.comparing(entry -> String.valueOf(entry.getKey())));

            List<IItemHandlerModifiable> handlers = new ArrayList<>();
            for (Map.Entry<?, ?> entry : entries) {
                Object stacksHandler = entry.getValue();
                Method getStacks = stacksHandler.getClass().getMethod("getStacks");
                Object stackHandler = getStacks.invoke(stacksHandler);
                if (stackHandler instanceof IItemHandlerModifiable handler) {
                    handlers.add(handler);
                }
            }

            return handlers;
        } catch (ReflectiveOperationException exception) {
            return List.of();
        }
    }
}
