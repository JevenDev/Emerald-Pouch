package com.jvn.emeraldpouch.item;

import com.jvn.emeraldpouch.pouch.PouchData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PouchItem extends Item {
    private final int slotCount;

    public PouchItem(int slotCount, Properties properties) {
        super(properties);
        this.slotCount = slotCount;
    }

    public int slotCount() {
        return slotCount;
    }

    public boolean isAutoCompactEnabled(ItemStack stack) {
        return PouchData.isAutoCompactEnabled(stack);
    }

    public boolean isAutoPickupEnabled(ItemStack stack) {
        return PouchData.isAutoPickupEnabled(stack);
    }
}
