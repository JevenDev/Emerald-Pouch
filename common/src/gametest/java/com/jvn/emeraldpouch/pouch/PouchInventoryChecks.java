package com.jvn.emeraldpouch.pouch;

import com.jvn.emeraldpouch.client.PouchDisplayData;
import com.jvn.emeraldpouch.compat.ModCompat;
import com.jvn.emeraldpouch.compat.PouchSlotAccess;
import com.jvn.emeraldpouch.registry.ModItems;
import java.util.List;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

public final class PouchInventoryChecks {
    private PouchInventoryChecks() {
    }

    public static void pouchCounts(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        Inventory inventory = player.getInventory();
        ItemStack pouch = new ItemStack(ModItems.POUCH.get());
        PouchData.saveContents(pouch, List.of(new ItemStack(Items.EMERALD, 4)));
        PouchData.setAutoPickupEnabled(pouch, true);

        inventory.setItem(0, pouch);
        inventory.setItem(9, pouch);
        inventory.setItem(Inventory.SLOT_OFFHAND, pouch);
        checkCounts(helper, player, 1, 4);
        inventory.setItem(9, pouch.copy());
        inventory.setItem(Inventory.SLOT_OFFHAND, pouch.copy());
        checkCounts(helper, player, 3, 12);
        inventory.clearContent();

        int accessoriesSlots = PouchSlotAccess.getAccessoriesBeltSlotCount(player);
        int curiosSlots = PouchSlotAccess.getCuriosSlotCount(player);
        if (!ModCompat.hasSlotCompatLoaded()) {
            helper.succeed();
            return;
        }
        helper.assertTrue(accessoriesSlots + curiosSlots > 0, "loaded slot mod exposed no equipment slots");
        PouchStackReference equipped = accessoriesSlots > 0
                ? PouchStackReference.accessoriesBelt(0) : PouchStackReference.curios(0);
        PouchInventoryAccess.commitPouchStack(inventory, equipped, pouch);
        checkCounts(helper, player, 1, 4);
        helper.assertTrue(PouchInventoryAccess.findFirstPouch(inventory).orElseThrow().equals(equipped),
                "equipped pouch lost priority");
        helper.assertTrue(PouchInventoryAccess.extractEmeraldsForTrade(inventory, 5).isEmpty(),
                "trade spent emeralds counted twice through slot APIs");
        checkCounts(helper, player, 1, 4);

        ItemStack remainder = PouchInventoryAccess.insertIntoAutoPickupPouches(inventory, new ItemStack(Items.EMERALD, 2));
        helper.assertTrue(remainder.isEmpty(), "equipped pouch did not accept pickup");
        checkCounts(helper, player, 1, 6);

        ItemStack sameContents = PouchInventoryAccess.getPouchStack(inventory, equipped).copy();
        inventory.setItem(0, sameContents);
        inventory.setItem(Inventory.SLOT_OFFHAND, sameContents.copy());
        checkCounts(helper, player, 3, 18);
        ItemStack payment = PouchInventoryAccess.extractEmeraldsForTrade(inventory, 7);
        helper.assertTrue(payment.is(Items.EMERALD) && payment.getCount() == 7, "incorrect trade payment");
        checkCounts(helper, player, 3, 11);

        PouchInventoryAccess.commitPouchStack(inventory, equipped, ItemStack.EMPTY);
        checkCounts(helper, player, 2, 11);
        helper.succeed();
    }

    private static void checkCounts(GameTestHelper helper, Player player, int pouches, long emeralds) {
        helper.assertTrue(PouchInventoryAccess.getDeterministicPouchReferences(player.getInventory()).size() == pouches,
                "incorrect pouch reference count, expected " + pouches);
        PouchDisplayData display = PouchDisplayData.fromPlayerInventory(player);
        helper.assertTrue(display.pouchCount() == pouches, "incorrect HUD pouch count");
        helper.assertTrue(display.totalEmeraldEquivalent() == emeralds,
                "incorrect HUD balance, expected " + emeralds + " but got " + display.totalEmeraldEquivalent());
    }
}
