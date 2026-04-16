package com.jvn.emeraldpouch.item;

import com.jvn.emeraldpouch.pouch.PouchData;
import com.jvn.emeraldpouch.pouch.PouchMenuOpener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

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

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack heldStack = player.getItemInHand(usedHand);
        if (player instanceof ServerPlayer serverPlayer) {
            PouchMenuOpener.openFromHand(serverPlayer, usedHand);
        }

        return InteractionResultHolder.sidedSuccess(heldStack, level.isClientSide());
    }
}
