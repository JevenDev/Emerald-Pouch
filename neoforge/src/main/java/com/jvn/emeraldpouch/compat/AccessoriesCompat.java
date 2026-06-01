package com.jvn.emeraldpouch.neoforge.compat;

import com.jvn.emeraldpouch.compat.ModCompat;
import com.jvn.emeraldpouch.registry.ModItems;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Optional;
import com.jvn.toucanlib.util.ToucanResourceLocations;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.EntityCapability;

public final class AccessoriesCompat {
    private static final String ACCESSORIES_CAPABILITY_CLASS_NAME = "io.wispforest.accessories.api.AccessoriesCapability";
    private static final String ACCESSORIES_BELT_SLOT_NAME = "belt";
    private static final EntityCapability<Object, Void> ACCESSORIES_CAPABILITY = createAccessoriesCapability();
    private static boolean equipSoundCallbacksRegistered;

    private AccessoriesCompat() {
    }

    public static int getBeltSlotCount(LivingEntity entity) {
        Container beltContainer = getBeltContainer(entity);
        return beltContainer == null ? 0 : beltContainer.getContainerSize();
    }

    public static ItemStack getBeltStackInSlot(LivingEntity entity, int slot) {
        Container beltContainer = getBeltContainer(entity);
        if (beltContainer == null || slot < 0 || slot >= beltContainer.getContainerSize()) {
            return ItemStack.EMPTY;
        }

        return beltContainer.getItem(slot);
    }

    public static void setBeltStackInSlot(LivingEntity entity, int slot, ItemStack stack) {
        Container beltContainer = getBeltContainer(entity);
        if (beltContainer == null || slot < 0 || slot >= beltContainer.getContainerSize()) {
            return;
        }

        beltContainer.setItem(slot, stack);
        beltContainer.setChanged();
    }

    public static boolean tryEquipFromHand(Player player, InteractionHand hand) {
        Object capability = getAccessoriesCapability(player);
        if (capability == null) {
            return false;
        }

        ItemStack handStack = player.getItemInHand(hand);
        if (handStack.isEmpty()) {
            return false;
        }

        try {
            Method attemptEquipMethod = capability.getClass().getMethod("attemptToEquipAccessory", ItemStack.class, boolean.class);
            ItemStack newHandStack = handStack.copy();
            Object pair = attemptEquipMethod.invoke(capability, newHandStack, true);
            if (pair == null) {
                return false;
            }

            Optional<ItemStack> swappedStack = extractPairSecondItemStackOptional(pair);
            if (swappedStack == null) {
                return false;
            }

            if (swappedStack.isPresent()) {
                ItemStack swapped = swappedStack.get();
                if (newHandStack.isEmpty()) {
                    newHandStack = swapped;
                } else if (ItemStack.isSameItemSameComponents(newHandStack, swapped)
                        && (newHandStack.getCount() + swapped.getCount()) <= newHandStack.getMaxStackSize()) {
                    newHandStack.grow(swapped.getCount());
                } else {
                    player.getInventory().placeItemBackInInventory(swapped);
                }
            }

            player.setItemInHand(hand, newHandStack);
            playManualEquipSoundIfNeeded(player);
            return true;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            return false;
        }
    }

    public static void registerEquipSoundCallbacks() {
        if (!ModCompat.isAccessoriesLoaded()) {
            return;
        }

        try {
            Class<?> accessoryClass = Class.forName("io.wispforest.accessories.api.core.Accessory");
            Class<?> accessoryRegistryClass = Class.forName("io.wispforest.accessories.api.core.AccessoryRegistry");
            Method register = accessoryRegistryClass.getMethod("register", Item.class, accessoryClass);

            InvocationHandler handler = (proxy, method, args) -> {
                if ("onEquip".equals(method.getName()) && args != null && args.length >= 2) {
                    Object slotReference = args[1];
                    LivingEntity entity = extractEntity(slotReference, "entity");
                    if (entity != null && !entity.level().isClientSide()) {
                        entity.playSound(SoundEvents.ARMOR_EQUIP_LEATHER.value(), 1.0F, 1.0F);
                    }
                    return null;
                }

                if (method.isDefault()) {
                    return invokeDefaultMethod(proxy, method, args);
                }

                if (method.getDeclaringClass() == Object.class) {
                    return method.invoke(proxy, args);
                }

                Class<?> returnType = method.getReturnType();
                if (returnType == boolean.class) {
                    return true;
                }
                if (returnType == int.class) {
                    return 0;
                }
                if (returnType == float.class) {
                    return 0.0F;
                }
                if (returnType == double.class) {
                    return 0.0D;
                }
                if (returnType == long.class) {
                    return 0L;
                }
                return null;
            };

            Object accessoryProxy = Proxy.newProxyInstance(
                    accessoryClass.getClassLoader(),
                    new Class<?>[] {accessoryClass},
                    handler
            );

            for (var pouchItem : ModItems.allPouchItems()) {
                register.invoke(null, pouchItem.get(), accessoryProxy);
            }
            equipSoundCallbacksRegistered = true;
        } catch (ReflectiveOperationException ignored) {
            equipSoundCallbacksRegistered = false;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static EntityCapability<Object, Void> createAccessoriesCapability() {
        if (!ModCompat.isAccessoriesLoaded()) {
            return null;
        }

        try {
            Class<?> capabilityClass = Class.forName(ACCESSORIES_CAPABILITY_CLASS_NAME);
            return (EntityCapability<Object, Void>) (EntityCapability) EntityCapability.createVoid(
                    ToucanResourceLocations.id("accessories", "capability"),
                    (Class) capabilityClass
            );
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    private static Container getBeltContainer(LivingEntity entity) {
        Object capability = getAccessoriesCapability(entity);
        if (capability == null) {
            return null;
        }

        try {
            Method getContainersMethod = capability.getClass().getMethod("getContainers");
            Object containers = getContainersMethod.invoke(capability);
            if (!(containers instanceof Map<?, ?> containerMap)) {
                return null;
            }

            Object beltContainer = containerMap.get(ACCESSORIES_BELT_SLOT_NAME);
            if (beltContainer == null) {
                return null;
            }

            Method getAccessoriesMethod = beltContainer.getClass().getMethod("getAccessories");
            Object accessoriesContainer = getAccessoriesMethod.invoke(beltContainer);
            return accessoriesContainer instanceof Container container ? container : null;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            return null;
        }
    }

    private static Object getAccessoriesCapability(LivingEntity entity) {
        if (!ModCompat.isAccessoriesLoaded() || ACCESSORIES_CAPABILITY == null) {
            return null;
        }

        return entity.getCapability(ACCESSORIES_CAPABILITY);
    }

    @SuppressWarnings("unchecked")
    private static Optional<ItemStack> extractPairSecondItemStackOptional(Object pair) {
        Object secondValue = invokeNoArg(pair, "second");
        if (secondValue == null) {
            secondValue = invokeNoArg(pair, "right");
        }

        if (!(secondValue instanceof Optional<?> optionalValue)) {
            return null;
        }

        if (optionalValue.isEmpty()) {
            return Optional.empty();
        }

        Object value = optionalValue.get();
        return value instanceof ItemStack stack ? Optional.of(stack) : null;
    }

    private static Object invokeNoArg(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            return null;
        }
    }

    private static void playManualEquipSoundIfNeeded(Player player) {
        if (!equipSoundCallbacksRegistered) {
            player.playSound(SoundEvents.ARMOR_EQUIP_LEATHER.value(), 1.0F, 1.0F);
        }
    }

    private static Object invokeDefaultMethod(Object proxy, Method method, Object[] args) throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Class<?> declaringClass = method.getDeclaringClass();
        return MethodHandles.privateLookupIn(declaringClass, lookup)
                .unreflectSpecial(method, declaringClass)
                .bindTo(proxy)
                .invokeWithArguments(args == null ? new Object[0] : args);
    }

    private static LivingEntity extractEntity(Object reference, String methodName) {
        if (reference == null) {
            return null;
        }

        try {
            Method entityMethod = reference.getClass().getMethod(methodName);
            Object entity = entityMethod.invoke(reference);
            return entity instanceof LivingEntity livingEntity ? livingEntity : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
