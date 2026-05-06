package com.jvn.emeraldpouch.compat;

import com.jvn.emeraldpouch.EmeraldPouchMod;
import com.jvn.emeraldpouch.pouch.PouchData;
import com.jvn.emeraldpouch.registry.ModItems;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModLoadingContext;

public final class ShulkerBoxTooltipCompat {
    private static final String SHULKER_TOOLTIP_API_CLASS = "com.misterpemodder.shulkerboxtooltip.api.ShulkerBoxTooltipApi";
    private static final String SHULKER_TOOLTIP_PLUGIN_CLASS = "com.misterpemodder.shulkerboxtooltip.api.forge.ShulkerBoxTooltipPlugin";
    private static final String COLOR_KEY_CLASS = "com.misterpemodder.shulkerboxtooltip.api.color.ColorKey";
    private static final String PREVIEW_PROVIDER_CLASS = "com.misterpemodder.shulkerboxtooltip.api.provider.PreviewProvider";
    private static final String PREVIEW_PROVIDER_REGISTRY_CLASS = "com.misterpemodder.shulkerboxtooltip.api.provider.PreviewProviderRegistry";
    private static final ResourceLocation PREVIEW_PROVIDER_ID =
            new ResourceLocation(EmeraldPouchMod.MOD_ID, "pouch_preview");
    private static boolean pluginRegistered;
    private static Object defaultColorKey;

    private ShulkerBoxTooltipCompat() {
    }

    public static void registerPluginExtension() {
        if (pluginRegistered || !ModCompat.isShulkerTooltipLoaded()) {
            return;
        }

        try {
            Class<?> apiClass = Class.forName(SHULKER_TOOLTIP_API_CLASS);
            Object apiImpl = Proxy.newProxyInstance(
                    apiClass.getClassLoader(),
                    new Class<?>[]{apiClass},
                    new TooltipApiHandler()
            );
            Supplier<Object> apiSupplier = () -> apiImpl;

            Class<?> pluginClass = Class.forName(SHULKER_TOOLTIP_PLUGIN_CLASS);
            Object plugin = pluginClass.getConstructor(Supplier.class).newInstance(apiSupplier);
            Method registerExtensionPoint = ModLoadingContext.class.getMethod(
                    "registerExtensionPoint",
                    Class.class,
                    Supplier.class
            );
            registerExtensionPoint.invoke(ModLoadingContext.get(), pluginClass, (Supplier<Object>) () -> plugin);
            pluginRegistered = true;
        } catch (ReflectiveOperationException exception) {
        }
    }

    private static final class TooltipApiHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return switch (method.getName()) {
                case "registerProviders" -> {
                    registerPreviewProvider(args[0]);
                    yield null;
                }
                case "registerColors" -> null;
                case "equals" -> proxy == args[0];
                case "hashCode" -> System.identityHashCode(proxy);
                case "toString" -> "EmeraldPouchShulkerBoxTooltipApi";
                default -> null;
            };
        }

        private static void registerPreviewProvider(Object registry) throws ReflectiveOperationException {
            Class<?> previewProviderClass = Class.forName(PREVIEW_PROVIDER_CLASS);
            Object provider = Proxy.newProxyInstance(
                    previewProviderClass.getClassLoader(),
                    new Class<?>[]{previewProviderClass},
                    new PouchPreviewProviderHandler()
            );
            Method register = Class.forName(PREVIEW_PROVIDER_REGISTRY_CLASS).getMethod(
                    "register",
                    ResourceLocation.class,
                    previewProviderClass,
                    Iterable.class
            );
            List<Item> pouchItems = ModItems.allPouchItems().stream()
                    .map(registryObject -> registryObject.get())
                    .toList();
            register.invoke(registry, PREVIEW_PROVIDER_ID, provider, pouchItems);
        }
    }

    private static final class PouchPreviewProviderHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return switch (method.getName()) {
                case "shouldDisplay" -> shouldDisplay(args[0]);
                case "getInventory" -> getInventory(args[0]);
                case "getInventoryMaxSize" -> getInventoryMaxSize(args[0]);
                case "getMaxRowSize" -> 9;
                case "isFullPreviewAvailable" -> true;
                case "showTooltipHints" -> false;
                case "getTooltipHintLangKey" -> "";
                case "getFullTooltipHintLangKey" -> "";
                case "getLockKeyTooltipHintLangKey" -> "";
                case "getWindowColorKey" -> getDefaultColorKey();
                case "getRenderer" -> null;
                case "addTooltip" -> List.of();
                case "onInventoryAccessStart" -> null;
                case "getTextureOverride" -> null;
                case "getPriority" -> 0;
                case "equals" -> proxy == args[0];
                case "hashCode" -> System.identityHashCode(proxy);
                case "toString" -> "EmeraldPouchPreviewProvider";
                default -> null;
            };
        }

        private static boolean shouldDisplay(Object previewContext) throws ReflectiveOperationException {
            return PouchData.isPouchStack(getContextStack(previewContext));
        }

        private static List<ItemStack> getInventory(Object previewContext) throws ReflectiveOperationException {
            ItemStack stack = getContextStack(previewContext);
            if (!PouchData.isPouchStack(stack)) {
                return List.of();
            }

            return List.copyOf(PouchData.loadContents(stack));
        }

        private static int getInventoryMaxSize(Object previewContext) throws ReflectiveOperationException {
            return PouchData.getSlotCount(getContextStack(previewContext));
        }

        private static ItemStack getContextStack(Object previewContext) throws ReflectiveOperationException {
            Method stackMethod = previewContext.getClass().getMethod("stack");
            return (ItemStack) stackMethod.invoke(previewContext);
        }

        private static Object getDefaultColorKey() {
            if (defaultColorKey != null) {
                return defaultColorKey;
            }

            try {
                defaultColorKey = Class.forName(COLOR_KEY_CLASS).getField("DEFAULT").get(null);
            } catch (ReflectiveOperationException exception) {
            }
            return defaultColorKey;
        }
    }
}
