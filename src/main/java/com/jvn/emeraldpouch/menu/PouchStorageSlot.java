package com.jvn.emeraldpouch.menu;

import com.jvn.emeraldpouch.pouch.PouchData;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PouchStorageSlot extends Slot {
    public PouchStorageSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return PouchData.isAllowedContent(stack);
    }
}
