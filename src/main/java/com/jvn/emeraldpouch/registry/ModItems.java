package com.jvn.emeraldpouch.registry;

import com.jvn.emeraldpouch.EmeraldPouchMod;
import com.jvn.emeraldpouch.item.PouchItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EmeraldPouchMod.MOD_ID);

    public static final DeferredItem<Item> POUCH = ITEMS.register(
            "pouch",
            () -> new PouchItem(9, new Item.Properties().stacksTo(1))
    );

    public static final DeferredItem<Item> LARGE_POUCH = ITEMS.register(
            "large_pouch",
            () -> new PouchItem(18, new Item.Properties().stacksTo(1))
    );

    private ModItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
