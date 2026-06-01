package com.jvn.emeraldpouch.fabric.compat;

import com.jvn.emeraldpouch.compat.ModCompat;
import com.jvn.emeraldpouch.registry.ModItems;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import java.util.Map;
import java.util.Optional;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class TrinketsCompat {
    private static final String BELT_GROUP = "legs";
    private static final String BELT_SLOT = "belt";
    private static final Trinket POUCH_TRINKET_SOUND = new Trinket() {
        @Override
        public void onEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
            if (!entity.level().isClientSide()) {
                entity.playSound(SoundEvents.ARMOR_EQUIP_LEATHER.value(), 1.0F, 1.0F);
            }
        }
    };

    private TrinketsCompat() {
    }

    public static void registerEquipSoundCallbacks() {
        if (!ModCompat.isTrinketsLoaded()) {
            return;
        }

        for (var pouchItem : ModItems.allPouchItems()) {
            TrinketsApi.registerTrinket(pouchItem.get(), POUCH_TRINKET_SOUND);
        }
    }

    public static int getBeltSlotCount(LivingEntity entity) {
        Container beltContainer = getBeltContainer(entity);
        return beltContainer == null ? 0 : beltContainer.getContainerSize();
    }

    public static ItemStack getBeltStackInSlot(LivingEntity entity, int slot) {
        Container beltContainer = getBeltContainer(entity);
        if (beltContainer == null || slot < 0 || slot >= beltContainer.getContainerSize()) {
            return ItemStack.EMPTY;
        }

        return beltContainer.getItem(slot);
    }

    public static void setBeltStackInSlot(LivingEntity entity, int slot, ItemStack stack) {
        Container beltContainer = getBeltContainer(entity);
        if (beltContainer == null || slot < 0 || slot >= beltContainer.getContainerSize()) {
            return;
        }

        beltContainer.setItem(slot, stack);
        if (beltContainer instanceof TrinketInventory beltInventory) {
            beltInventory.markUpdate();
        }
    }

    public static boolean tryEquipFromHand(Player player, InteractionHand hand) {
        Container beltContainer = getBeltContainer(player);
        if (beltContainer == null) {
            return false;
        }

        ItemStack handStack = player.getItemInHand(hand);
        if (handStack.isEmpty()) {
            return false;
        }

        for (int slot = 0; slot < beltContainer.getContainerSize(); slot++) {
            if (!beltContainer.getItem(slot).isEmpty() || !beltContainer.canPlaceItem(slot, handStack)) {
                continue;
            }

            beltContainer.setItem(slot, handStack.copy());
            if (beltContainer instanceof TrinketInventory beltInventory) {
                beltInventory.markUpdate();
            }
            player.setItemInHand(hand, ItemStack.EMPTY);
            player.playSound(SoundEvents.ARMOR_EQUIP_LEATHER.value(), 1.0F, 1.0F);
            return true;
        }

        if (beltContainer.getContainerSize() <= 0 || !beltContainer.canPlaceItem(0, handStack)) {
            return false;
        }

        ItemStack equippedStack = beltContainer.getItem(0);
        beltContainer.setItem(0, handStack.copy());
        if (beltContainer instanceof TrinketInventory beltInventory) {
            beltInventory.markUpdate();
        }
        player.setItemInHand(hand, equippedStack.copy());
        player.playSound(SoundEvents.ARMOR_EQUIP_LEATHER.value(), 1.0F, 1.0F);
        return true;
    }

    private static Container getBeltContainer(LivingEntity entity) {
        if (!ModCompat.isTrinketsLoaded()) {
            return null;
        }

        Optional<TrinketComponent> trinketComponent = TrinketsApi.getTrinketComponent(entity);
        if (trinketComponent.isEmpty()) {
            return null;
        }

        Map<String, TrinketInventory> groupInventory = trinketComponent.get().getInventory().get(BELT_GROUP);
        if (groupInventory == null) {
            return null;
        }

        return groupInventory.get(BELT_SLOT);
    }
}
