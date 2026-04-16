package com.jvn.emeraldpouch.item;

import net.minecraft.world.item.Item;

public class PouchItem extends Item {
    private final int slotCount;

    public PouchItem(int slotCount, Properties properties) {
        super(properties);
        this.slotCount = slotCount;
    }

    public int slotCount() {
        return slotCount;
    }
}
