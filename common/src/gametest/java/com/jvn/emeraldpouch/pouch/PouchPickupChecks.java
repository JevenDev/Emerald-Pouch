package com.jvn.emeraldpouch.pouch;

import com.jvn.emeraldpouch.compat.ModCompat;
import com.jvn.emeraldpouch.compat.PouchSlotAccess;
import com.jvn.emeraldpouch.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

public final class PouchPickupChecks {
    private PouchPickupChecks() {
    }

    public static void autoPickup(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        List<PouchStackReference> locations = new ArrayList<>();
        locations.add(PouchStackReference.inventory(0));
        locations.add(PouchStackReference.inventory(Inventory.SLOT_OFFHAND));
        int accessoriesSlots = PouchSlotAccess.getAccessoriesBeltSlotCount(player);
        int curiosSlots = PouchSlotAccess.getCuriosSlotCount(player);
        if (ModCompat.hasSlotCompatLoaded()) {
            helper.assertTrue(accessoriesSlots + curiosSlots > 0, "loaded slot mod exposed no equipment slots");
        }
        if (accessoriesSlots > 0) locations.add(PouchStackReference.accessoriesBelt(0));
        if (curiosSlots > 0) locations.add(PouchStackReference.curios(0));

        Inventory inventory = player.getInventory();
        for (PouchStackReference location : locations) {
            fillInventory(inventory);
            ItemStack pouch = new ItemStack(ModItems.POUCH.get());
            PouchData.setAutoPickupEnabled(pouch, true);
            PouchInventoryAccess.commitPouchStack(inventory, location, pouch);
            ItemEntity emeralds = dropped(helper, player, new ItemStack(Items.EMERALD, 16));
            emeralds.playerTouch(player);
            helper.assertTrue(emeralds.isRemoved(), "full inventory blocked emerald pickup at " + location);
            checkBalance(helper, inventory, location, 16);
            ItemEntity blocks = dropped(helper, player, new ItemStack(Items.EMERALD_BLOCK, 3));
            blocks.playerTouch(player);
            helper.assertTrue(blocks.isRemoved(), "full inventory blocked block pickup at " + location);
            checkBalance(helper, inventory, location, 43);

            pouch = PouchInventoryAccess.getPouchStack(inventory, location);
            PouchData.setAutoPickupEnabled(pouch, false);
            PouchInventoryAccess.commitPouchStack(inventory, location, pouch);
            ItemEntity disabled = dropped(helper, player, new ItemStack(Items.EMERALD, 16));
            disabled.playerTouch(player);
            checkRemainder(helper, disabled, 16);
            checkBalance(helper, inventory, location, 43);
            disabled.discard();
            PouchData.setAutoPickupEnabled(pouch, true);
            PouchInventoryAccess.commitPouchStack(inventory, location, pouch);

            ItemEntity protectedItem = dropped(helper, player, new ItemStack(Items.EMERALD, 16));
            protectedItem.setPickUpDelay(10);
            protectedItem.playerTouch(player);
            checkRemainder(helper, protectedItem, 16);
            protectedItem.setNoPickUpDelay();
            protectedItem.setTarget(UUID.randomUUID());
            protectedItem.playerTouch(player);
            checkRemainder(helper, protectedItem, 16);
            checkBalance(helper, inventory, location, 43);
            protectedItem.setTarget(player.getUUID());
            protectedItem.playerTouch(player);
            helper.assertTrue(protectedItem.isRemoved(), "owner could not pick up item");
            checkBalance(helper, inventory, location, 59);

            ItemEntity unrelated = dropped(helper, player, new ItemStack(Items.GOLD_INGOT, 64));
            unrelated.playerTouch(player);
            checkRemainder(helper, unrelated, 64);
            checkBalance(helper, inventory, location, 59);
            unrelated.discard();

            List<ItemStack> contents = new ArrayList<>();
            for (int slot = 0; slot < 8; slot++) contents.add(new ItemStack(Items.EMERALD_BLOCK, 64));
            contents.add(new ItemStack(Items.EMERALD, 63));
            PouchData.saveContents(pouch, contents);
            PouchInventoryAccess.commitPouchStack(inventory, location, pouch);
            int before = PouchData.getStoredEmeraldEquivalent(pouch);
            ItemEntity partial = dropped(helper, player, new ItemStack(Items.EMERALD, 64));
            int pickedBefore = player.getStats().getValue(Stats.ITEM_PICKED_UP, Items.EMERALD);
            partial.playerTouch(player);
            helper.assertTrue(player.getStats().getValue(Stats.ITEM_PICKED_UP, Items.EMERALD) == pickedBefore + 1,
                    "partial pickup credited the wrong emerald count");
            checkRemainder(helper, partial, 63);
            checkBalance(helper, inventory, location, before + 1);
            partial.playerTouch(player);
            checkRemainder(helper, partial, 63);
            checkBalance(helper, inventory, location, before + 1);
            inventory.setItem(1, ItemStack.EMPTY);
            partial.playerTouch(player);
            helper.assertTrue(partial.isRemoved(), "full pouch blocked normal inventory pickup");
            helper.assertTrue(inventory.getItem(1).is(Items.EMERALD) && inventory.getItem(1).getCount() == 63,
                    "partial pickup lost or duplicated emeralds");
            checkBalance(helper, inventory, location, before + 1);
            helper.assertTrue(player.getStats().getValue(Stats.ITEM_PICKED_UP, Items.EMERALD) == pickedBefore + 64,
                    "pickup statistics lost or duplicated emeralds");
            PouchInventoryAccess.commitPouchStack(inventory, location, ItemStack.EMPTY);
        }
        helper.succeed();
    }

    private static void fillInventory(Inventory inventory) {
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            inventory.setItem(slot, new ItemStack(Items.DIRT, 64));
        }
        inventory.setItem(Inventory.SLOT_OFFHAND, new ItemStack(Items.DIRT, 64));
    }

    private static ItemEntity dropped(GameTestHelper helper, ServerPlayer player, ItemStack stack) {
        ItemEntity entity = new ItemEntity(helper.getLevel(), player.getX(), player.getY(), player.getZ(), stack);
        entity.setNoPickUpDelay();
        helper.getLevel().addFreshEntity(entity);
        return entity;
    }

    private static void checkBalance(GameTestHelper helper, Inventory inventory, PouchStackReference location, int expected) {
        int actual = PouchData.getStoredEmeraldEquivalent(PouchInventoryAccess.getPouchStack(inventory, location));
        helper.assertTrue(actual == expected, "incorrect pouch balance at " + location + ": " + actual + " instead of " + expected);
    }

    private static void checkRemainder(GameTestHelper helper, ItemEntity entity, int count) {
        helper.assertTrue(!entity.isRemoved() && entity.getItem().getCount() == count, "incorrect ground item remainder");
    }
}
