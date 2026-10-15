package com.jvn.emeraldpouch.recipe;

import com.jvn.emeraldpouch.client.PouchHudVisibility;
import com.jvn.emeraldpouch.pouch.PouchData;
import com.jvn.emeraldpouch.registry.ModItems;
import com.jvn.emeraldpouch.registry.ModRecipeSerializers;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

/** Real server menus and registered mod items; included only with -PpouchGameTests. */
public final class PouchCraftingChecks {
    private PouchCraftingChecks() {
    }

    private static ItemStack pouch(int value) {
        ItemStack stack = new ItemStack(ModItems.POUCH.get());
        List<ItemStack> contents = new ArrayList<>();
        int blocks = value / 9;
        while (blocks > 0) {
            int count = Math.min(64, blocks);
            contents.add(new ItemStack(Items.EMERALD_BLOCK, count));
            blocks -= count;
        }
        if (value % 9 != 0) contents.add(new ItemStack(Items.EMERALD, value % 9));
        PouchData.saveContents(stack, contents);
        stack.setHoverName(Component.literal("Saved trading fund"));
        PouchData.setAutoPickupEnabled(stack, true);
        return stack;
    }

    public static void crafting(GameTestHelper helper, boolean shift) {
        var player = new net.minecraftforge.common.util.FakePlayer(helper.getLevel(),
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "PouchTest"));
        var menu = new CraftingMenu(1, player.getInventory(),
                ContainerLevelAccess.create(helper.getLevel(), helper.absolutePos(BlockPos.ZERO)));
        player.containerMenu = menu;
        int[] balances = {0, 1, 8, 9, 63, 64, 65, 511, 4096, 4608, 4609, 4616, 5175, 5184};
        for (int i = 1; i <= 9; i++) {
            if (i != 5) menu.getSlot(i).set(new ItemStack(Items.LEATHER, balances.length));
        }
        int expectedTotal = 0;
        for (int value : balances) {
            ItemStack source = pouch(value);
            ItemStack before = source.copy();
            menu.getSlot(5).set(source);
            for (int preview = 0; preview < 10; preview++) {
                menu.slotsChanged(menu.getSlot(1).container);
                helper.assertTrue(ItemStack.matches(before, source), "preview mutated input");
                checkOutput(helper, menu.getSlot(0).getItem(), value);
            }
            menu.clicked(0, 0, shift ? ClickType.QUICK_MOVE : ClickType.PICKUP, player);
            helper.assertTrue(menu.getSlot(5).getItem().isEmpty(), "input pouch not consumed");
            helper.assertTrue(menu.getSlot(0).getItem().isEmpty(), "stale output after consumption");
            if (shift) {
                expectedTotal += value;
                int total = 0;
                for (ItemStack stack : player.getInventory().items) {
                    if (stack.is(ModItems.LARGE_POUCH.get())) total += PouchData.getStoredEmeraldEquivalent(stack);
                }
                helper.assertTrue(total == expectedTotal, "shift-click lost or duplicated contents");
            } else {
                checkOutput(helper, menu.getCarried(), value);
                menu.setCarried(ItemStack.EMPTY);
            }
            // Repeated take without another input cannot create value.
            menu.clicked(0, 0, shift ? ClickType.QUICK_MOVE : ClickType.PICKUP, player);
            helper.assertTrue(menu.getCarried().isEmpty(), "empty grid yielded another output");
        }
        for (int i = 1; i <= 9; i++) helper.assertTrue(menu.getSlot(i).getItem().isEmpty(), "incorrect ingredient consumption");
        if (shift) helper.assertTrue(player.getInventory().items.stream().filter(s -> s.is(ModItems.LARGE_POUCH.get())).count() == balances.length,
                "incorrect number of upgraded pouches");
        helper.succeed();
    }

    public static void fullInventory(GameTestHelper helper) {
        var player = new net.minecraftforge.common.util.FakePlayer(helper.getLevel(),
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "PouchTest"));
        for (int i = 0; i < player.getInventory().items.size(); i++) player.getInventory().items.set(i, new ItemStack(Items.DIRT, 64));
        var menu = new CraftingMenu(1, player.getInventory(),
                ContainerLevelAccess.create(helper.getLevel(), helper.absolutePos(BlockPos.ZERO)));
        player.containerMenu = menu;
        for (int i = 1; i <= 9; i++) menu.getSlot(i).set(i == 5 ? pouch(5184) : new ItemStack(Items.LEATHER));
        ItemStack before = menu.getSlot(5).getItem().copy();
        menu.clicked(0, 0, ClickType.QUICK_MOVE, player);
        helper.assertTrue(ItemStack.matches(before, menu.getSlot(5).getItem()), "blocked shift-click consumed source");
        checkOutput(helper, menu.getSlot(0).getItem(), 5184);
        player.getInventory().items.set(0, ItemStack.EMPTY);
        menu.clicked(0, 0, ClickType.QUICK_MOVE, player);
        checkOutput(helper, player.getInventory().items.get(0), 5184);
        helper.assertTrue(menu.getSlot(5).getItem().isEmpty(), "successful shift-click retained source");
        helper.succeed();
    }

    public static void hudModes(GameTestHelper helper) {
        for (GameType mode : GameType.values()) {
            var player = new net.minecraftforge.common.util.FakePlayer(helper.getLevel(),
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "PouchTest"));
            player.setGameMode(mode);
            helper.assertTrue(PouchHudVisibility.isVisible(player.isSpectator(), false) == (mode != GameType.SPECTATOR),
                    "HUD visibility changed for " + mode.getName());
            helper.assertFalse(PouchHudVisibility.isVisible(player.isSpectator(), true), "hidden GUI showed pouch HUD");
        }
        // Resolve the registered serializer in the loaded mod, rather than a fixture registry.
        helper.assertTrue(ModRecipeSerializers.POUCH_UPGRADE.get() instanceof PouchUpgradeRecipe.Serializer, "upgrade serializer not registered");
        helper.succeed();
    }

    private static void checkOutput(GameTestHelper helper, ItemStack result, int value) {
        helper.assertTrue(result.is(ModItems.LARGE_POUCH.get()) && result.getCount() == 1, "wrong upgraded output");
        helper.assertTrue(PouchData.getStoredEmeraldEquivalent(result) == value, "wrong upgraded balance " + value);
        helper.assertTrue(result.getHoverName().getString().equals("Saved trading fund"), "custom name lost");
        helper.assertTrue(PouchData.isAutoPickupEnabled(result), "pickup toggle lost");
    }
}
