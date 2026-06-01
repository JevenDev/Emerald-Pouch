package com.jvn.emeraldpouch.neoforge.compat;

import com.jvn.emeraldpouch.compat.ModCompat;
import com.jvn.emeraldpouch.registry.ModItems;
import com.jvn.toucanlib.util.ToucanResourceLocations;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public final class CuriosCompat {
    private static final EntityCapability<IItemHandler, Void> CURIOS_INVENTORY = EntityCapability.createVoid(
            ToucanResourceLocations.id("curios", "item_handler"),
            IItemHandler.class
    );
    private static boolean equipSoundCallbacksRegistered;

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
                    playManualEquipSoundIfNeeded(player);
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
            playManualEquipSoundIfNeeded(player);
            return true;
        }

        return false;
    }

    public static void registerEquipSoundCallbacks() {
        if (!ModCompat.isCuriosLoaded()) {
            return;
        }

        try {
            Class<?> curiosApiClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");
            Class<?> curioItemClass = Class.forName("top.theillusivec4.curios.api.type.capability.ICurioItem");
            Method registerCurio = curiosApiClass.getMethod("registerCurio", Item.class, curioItemClass);

            InvocationHandler handler = (proxy, method, args) -> {
                if ("onEquip".equals(method.getName()) && args != null && args.length >= 1) {
                    Object slotContext = args[0];
                    LivingEntity entity = extractEntity(slotContext, "entity");
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

            Object curioProxy = Proxy.newProxyInstance(
                    curioItemClass.getClassLoader(),
                    new Class<?>[] {curioItemClass},
                    handler
            );

            for (var pouchItem : ModItems.allPouchItems()) {
                registerCurio.invoke(null, pouchItem.get(), curioProxy);
            }
            equipSoundCallbacksRegistered = true;
        } catch (ReflectiveOperationException ignored) {
            equipSoundCallbacksRegistered = false;
        }
    }

    private static IItemHandler getHandler(LivingEntity entity) {
        if (!ModCompat.isCuriosLoaded()) {
            return null;
        }
        return entity.getCapability(CURIOS_INVENTORY);
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
