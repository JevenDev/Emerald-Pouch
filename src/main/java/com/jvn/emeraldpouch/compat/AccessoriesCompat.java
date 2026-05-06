package com.jvn.emeraldpouch.compat;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class AccessoriesCompat {
    private static final String ACCESSORIES_CAPABILITY_CLASS = "io.wispforest.accessories.api.AccessoriesCapability";
    private static final String ACCESSORIES_CONTAINER_CLASS = "io.wispforest.accessories.api.AccessoriesContainer";
    private static final String BELT_SLOT_NAME = "belt";

    private AccessoriesCompat() {
    }

    public static int getBeltSlotCount(LivingEntity entity) {
        return getBeltContainer(entity).map(Container::getContainerSize).orElse(0);
    }

    public static ItemStack getBeltStackInSlot(LivingEntity entity, int slot) {
        return getBeltContainer(entity)
                .filter(container -> slot >= 0 && slot < container.getContainerSize())
                .map(container -> container.getItem(slot))
                .orElse(ItemStack.EMPTY);
    }

    public static void setBeltStackInSlot(LivingEntity entity, int slot, ItemStack stack) {
        getBeltContainer(entity)
                .filter(container -> slot >= 0 && slot < container.getContainerSize())
                .ifPresent(container -> container.setItem(slot, stack));
    }

    public static boolean tryEquipFromHand(Player player, InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);
        if (heldStack.isEmpty()) {
            return false;
        }

        return getBeltContainer(player)
                .map(container -> tryEquipIntoBelt(player, hand, heldStack, container))
                .orElse(false);
    }

    private static boolean tryEquipIntoBelt(Player player, InteractionHand hand, ItemStack heldStack, Container container) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            if (!container.getItem(slot).isEmpty()) {
                continue;
            }

            container.setItem(slot, heldStack.copy());
            player.setItemInHand(hand, ItemStack.EMPTY);
            return true;
        }

        return false;
    }

    private static Optional<Container> getBeltContainer(LivingEntity entity) {
        if (!ModCompat.isAccessoriesLoaded()) {
            return Optional.empty();
        }

        try {
            Class<?> capabilityClass = Class.forName(ACCESSORIES_CAPABILITY_CLASS);
            Method getOptionally = capabilityClass.getMethod("getOptionally", LivingEntity.class);
            Object capabilityOptional = getOptionally.invoke(null, entity);
            if (!(capabilityOptional instanceof Optional<?> optionalCapability) || optionalCapability.isEmpty()) {
                return Optional.empty();
            }

            Object capability = optionalCapability.get();
            Method getContainers = capabilityClass.getMethod("getContainers");
            Object containersObject = getContainers.invoke(capability);
            if (!(containersObject instanceof Map<?, ?> containers)) {
                return Optional.empty();
            }

            Object beltContainer = containers.get(BELT_SLOT_NAME);
            if (beltContainer == null) {
                return Optional.empty();
            }

            Method getAccessories = Class.forName(ACCESSORIES_CONTAINER_CLASS).getMethod("getAccessories");
            Object accessoriesContainer = getAccessories.invoke(beltContainer);
            if (!(accessoriesContainer instanceof Container container)) {
                return Optional.empty();
            }

            return Optional.of(container);
        } catch (ReflectiveOperationException exception) {
            return Optional.empty();
        }
    }
}
