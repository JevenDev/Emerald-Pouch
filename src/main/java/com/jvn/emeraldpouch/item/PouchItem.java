package com.jvn.emeraldpouch.item;

import com.jvn.emeraldpouch.compat.ModCompat;
import com.jvn.emeraldpouch.pouch.PouchData;
import com.jvn.emeraldpouch.pouch.PouchMenuOpener;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.component.ItemContainerContents;

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

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        ItemStack slotStack = slot.getItem();
        if (!PouchData.isAllowedContent(slotStack)) {
            return false;
        }

        int requested = action == ClickAction.PRIMARY ? slotStack.getCount() : 1;
        ItemStack extracted = slot.safeTake(requested, requested, player);
        if (extracted.isEmpty()) {
            return false;
        }

        ItemStack remainder = PouchData.insertIntoPouch(stack, extracted, extracted.getCount());
        int inserted = extracted.getCount() - remainder.getCount();
        if (inserted <= 0) {
            ItemStack rollback = slot.safeInsert(extracted);
            if (!rollback.isEmpty()) {
                player.getInventory().placeItemBackInInventory(rollback);
            }
            return false;
        }

        if (!remainder.isEmpty()) {
            ItemStack leftover = slot.safeInsert(remainder);
            if (!leftover.isEmpty()) {
                player.getInventory().placeItemBackInInventory(leftover);
            }
        }

        return true;
    }

    @Override
    public boolean overrideOtherStackedOnMe(
            ItemStack stack,
            ItemStack other,
            Slot slot,
            ClickAction action,
            Player player,
            SlotAccess access
    ) {
        if (!PouchData.isAllowedContent(other)) {
            return false;
        }

        int requested = action == ClickAction.PRIMARY ? other.getCount() : 1;
        ItemStack remainder = PouchData.insertIntoPouch(stack, other, requested);
        int inserted = other.getCount() - remainder.getCount();
        if (inserted <= 0) {
            return false;
        }

        access.set(remainder);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        Component compactState = Component.translatable(
                isAutoCompactEnabled(stack)
                        ? "tooltip.emeraldpouch.state.on"
                        : "tooltip.emeraldpouch.state.off"
        );
        Component pickupState = Component.translatable(
                isAutoPickupEnabled(stack)
                        ? "tooltip.emeraldpouch.state.on"
                        : "tooltip.emeraldpouch.state.off"
        );

        if (tooltipFlag.hasShiftDown()) {
            tooltipComponents.add(Component.translatable(
                    "tooltip.emeraldpouch.stored_emeralds",
                    PouchData.getStoredEmeraldEquivalent(stack)
            ).withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.translatable("tooltip.emeraldpouch.auto_compact", compactState).withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.translatable("tooltip.emeraldpouch.auto_pickup", pickupState).withStyle(ChatFormatting.GRAY));
        } else if (!ModCompat.isShulkerTooltipLoaded()) {
            int shown = 0;
            int total = 0;
            ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
            for (ItemStack stored : contents.nonEmptyItems()) {
                total++;
                if (shown < 5) {
                    shown++;
                    tooltipComponents.add(Component.translatable("container.shulkerBox.itemCount", stored.getHoverName(), stored.getCount()));
                }
            }
            if (total - shown > 0) {
                tooltipComponents.add(Component.translatable("container.shulkerBox.more", total - shown).withStyle(ChatFormatting.ITALIC));
            }
        }
    }
}
