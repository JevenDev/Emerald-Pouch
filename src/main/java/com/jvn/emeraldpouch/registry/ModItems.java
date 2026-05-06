package com.jvn.emeraldpouch.registry;

import com.jvn.emeraldpouch.EmeraldPouchMod;
import com.jvn.emeraldpouch.item.PouchItem;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, EmeraldPouchMod.MOD_ID);
    private static final List<RegistryObject<Item>> ALL_POUCH_ITEMS = new ArrayList<>();
    private static final Map<DyeColor, RegistryObject<Item>> COLORED_POUCHES = new EnumMap<>(DyeColor.class);
    private static final Map<DyeColor, RegistryObject<Item>> COLORED_LARGE_POUCHES = new EnumMap<>(DyeColor.class);

    public static final RegistryObject<Item> POUCH = registerPouch("pouch", 9);
    public static final RegistryObject<Item> LARGE_POUCH = registerPouch("large_pouch", 18);

    static {
        for (DyeColor color : DyeColor.values()) {
            COLORED_POUCHES.put(color, registerPouch(color.getName() + "_pouch", 9));
            COLORED_LARGE_POUCHES.put(color, registerPouch(color.getName() + "_large_pouch", 18));
        }
    }

    private ModItems() {
    }

    public static List<RegistryObject<Item>> allPouchItems() {
        return List.copyOf(ALL_POUCH_ITEMS);
    }

    public static Item getColoredVariant(DyeColor color, int slotCount) {
        RegistryObject<Item> item = slotCount >= 18 ? COLORED_LARGE_POUCHES.get(color) : COLORED_POUCHES.get(color);
        return item == null ? Items.AIR : item.get();
    }

    private static RegistryObject<Item> registerPouch(String name, int slotCount) {
        RegistryObject<Item> item = ITEMS.register(
                name,
                () -> new PouchItem(slotCount, new Item.Properties().stacksTo(1))
        );
        ALL_POUCH_ITEMS.add(item);
        return item;
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
