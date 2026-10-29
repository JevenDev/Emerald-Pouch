package com.jvn.emeraldpouch.compat;

import com.jvn.emeraldpouch.pouch.PouchDeathRetention;
import com.jvn.emeraldpouch.registry.ModItems;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class PouchDeathCompat {
    private PouchDeathCompat() {
    }

    public static void register() {
        if (ModCompat.isTrinketsLoaded()) {
            registerDropEvent("dev.emi.trinkets.api.event.TrinketDropCallback", true);
        }
        if (ModCompat.isAccessoriesLoaded()) {
            registerDropEvent("io.wispforest.accessories.api.events.OnDropCallback", false);
        }
        if (ModCompat.isCuriosLoaded()) {
            registerCurios();
        }
    }

    private static void registerDropEvent(String className, boolean trinkets) {
        try {
            Class<?> callbackType = Class.forName(className);
            var eventField = callbackType.getField("EVENT");
            Object listener = Proxy.newProxyInstance(callbackType.getClassLoader(), new Class<?>[] {callbackType},
                    (proxy, method, args) -> {
                        if (method.getDeclaringClass() == Object.class) return objectMethod(proxy, method, args);
                        Object entity = trinkets ? args[3] : args[2].getClass().getMethod("entity").invoke(args[2]);
                        if (entity instanceof Player && PouchDeathRetention.shouldKeep((ItemStack) args[1])) {
                            return method.getReturnType().getField("KEEP").get(null);
                        }
                        return args[0];
                    });
            eventField.getType().getMethod("register", Object.class).invoke(eventField.get(null), listener);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not register pouch death retention with " + className, exception);
        }
    }

    private static void registerCurios() {
        try {
            Class<?> itemType = Class.forName("top.theillusivec4.curios.api.type.capability.ICurioItem");
            Object curio = Proxy.newProxyInstance(itemType.getClassLoader(), new Class<?>[] {itemType},
                    (proxy, method, args) -> {
                        if (method.getDeclaringClass() == Object.class) return objectMethod(proxy, method, args);
                        if (method.getName().equals("getDropRule")
                                && args[0].getClass().getMethod("entity").invoke(args[0]) instanceof Player
                                && PouchDeathRetention.shouldKeep((ItemStack) args[args.length - 1])) {
                            return method.getReturnType().getField("ALWAYS_KEEP").get(null);
                        }
                        return InvocationHandler.invokeDefault(proxy, method, args);
                    });
            Method register = Class.forName("top.theillusivec4.curios.api.CuriosApi")
                    .getMethod("registerCurio", Item.class, itemType);
            for (var item : ModItems.allPouchItems()) {
                register.invoke(null, item.get(), curio);
            }
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not register pouch death retention with Curios", exception);
        }
    }

    private static Object objectMethod(Object proxy, Method method, Object[] args) {
        return switch (method.getName()) {
            case "equals" -> proxy == args[0];
            case "hashCode" -> System.identityHashCode(proxy);
            case "toString" -> "Emerald pouch death retention";
            default -> throw new IllegalStateException("Unexpected proxy method " + method);
        };
    }
}
