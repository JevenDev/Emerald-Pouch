package com.jvn.emeraldpouch.recipe;

import com.google.gson.JsonParser;
import com.jvn.emeraldpouch.pouch.PouchData;
import com.jvn.emeraldpouch.registry.ModItems;
import io.netty.buffer.Unpooled;
import java.io.InputStreamReader;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.player.Player;

public final class PouchUpgradeNbtChecks {
    private PouchUpgradeNbtChecks() { }

    public static void check(GameTestHelper helper) {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(com.jvn.emeraldpouch.PouchTestCapabilities.class);
        var serializer = new PouchUpgradeRecipe.Serializer();
        var id = new ResourceLocation("emeraldpouch", "large_pouch");
        PouchUpgradeRecipe recipe;
        try (var reader = new InputStreamReader(PouchUpgradeNbtChecks.class.getResourceAsStream("/data/emeraldpouch/recipes/large_pouch.json"))) {
            var json = JsonParser.parseReader(reader).getAsJsonObject();
            helper.assertTrue(json.get("type").getAsString().equals("emeraldpouch:pouch_upgrade"), "wrong recipe serializer");
            recipe = serializer.fromJson(id, json);
        } catch (Exception e) { throw new RuntimeException(e); }
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            serializer.toNetwork(buffer, recipe);
            recipe = serializer.fromNetwork(id, buffer);
        } finally { buffer.release(); }
        helper.assertTrue(recipe.getWidth() == 3 && recipe.getIngredients().size() == 9, "network lost shaped recipe");
        var menu = new AbstractContainerMenu(null, 0) {
            public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
            public boolean stillValid(Player player) { return true; }
        };
        CraftingContainer input = new TransientCraftingContainer(menu, 3, 3);
        for (int i = 0; i < 9; i++) input.setItem(i, new ItemStack(Items.LEATHER));
        ItemStack source = new ItemStack(ModItems.POUCH.get());
        source.getOrCreateTag().putString("another_mod", "retain me");
        source.setHoverName(net.minecraft.network.chat.Component.literal("Named fund"));
        PouchData.setAutoCompactEnabled(source, true);
        ItemStack namedEmeralds = new ItemStack(Items.EMERALD, 17);
        namedEmeralds.setHoverName(net.minecraft.network.chat.Component.literal("Keep nested metadata"));
        PouchData.saveContents(source, java.util.List.of(namedEmeralds));
        // Forge defers attachment until the first capability query.
        source.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER);
        CompoundTag serialized = source.save(new CompoundTag());
        helper.assertTrue(serialized.getCompound("ForgeCaps").contains("emeraldpouch:test_metadata"), "test capability was not attached: " + serialized);
        serialized.getCompound("ForgeCaps").getCompound("emeraldpouch:test_metadata").putInt("value", 12345);
        source = ItemStack.of(serialized);
        helper.assertTrue(source.save(new CompoundTag()).getCompound("ForgeCaps").getCompound("emeraldpouch:test_metadata").getInt("value") == 12345, "fixture capability did not deserialize: " + source.save(new CompoundTag()));
        input.setItem(4, source);
        // An uninitialized Forge capability retains lazy NBT; output NBT must not alias it.
        ItemStack lazyOutput = recipe.assemble(input, helper.getLevel().registryAccess());
        lazyOutput.save(new CompoundTag()).getCompound("ForgeCaps").getCompound("emeraldpouch:test_metadata").putInt("value", 7);
        helper.assertTrue(source.save(new CompoundTag()).getCompound("ForgeCaps")
                .getCompound("emeraldpouch:test_metadata").getInt("value") == 12345, "preview aliased source lazy capability NBT");
        ItemStack before = source.copy();
        for (int preview = 0; preview < 20; preview++) {
            helper.assertTrue(recipe.matches(input, helper.getLevel()), "valid recipe rejected");
            ItemStack output = recipe.assemble(input, helper.getLevel().registryAccess());
            helper.assertTrue(output.getTag().equals(before.getTag()), "NBT was lost or changed");
            output.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER);
            helper.assertTrue(output.save(new CompoundTag()).getCompound("ForgeCaps")
                    .getCompound("emeraldpouch:test_metadata").getInt("value") == 12345, "transferable capability metadata lost");
            output.getOrCreateTag().putString("another_mod", "changed preview");
            PouchData.saveContents(output, java.util.List.of());
            helper.assertTrue(ItemStack.matches(before, source), "preview mutated source NBT");
        }
        helper.assertTrue(recipe.getRemainingItems(input).stream().allMatch(ItemStack::isEmpty), "source returned as remainder");
        // Raw overflow and duplicate slots must not be hidden by ContainerHelper.
        ListTag entries = source.getTagElement(PouchData.POUCH_TAG).getCompound(PouchData.CONTENTS_TAG).getList("Items", 10);
        CompoundTag entry = entries.getCompound(0);
        entry.putByte("Slot", (byte)18);
        helper.assertFalse(recipe.matches(input, helper.getLevel()), "overflow accepted");
        helper.assertTrue(recipe.assemble(input, helper.getLevel().registryAccess()).isEmpty(), "overflow assembled");
        entry.putByte("Slot", (byte)17);
        helper.assertTrue(PouchData.getStoredEmeraldEquivalent(recipe.assemble(input, helper.getLevel().registryAccess())) == 17, "fitting overflow slot was lost");
        entry.putByte("Slot", (byte)0);
        entries.add(entry.copy());
        helper.assertFalse(recipe.matches(input, helper.getLevel()), "duplicate slot accepted");
        entries.remove(1);
        entry.putByte("Count", (byte)65);
        helper.assertFalse(recipe.matches(input, helper.getLevel()), "oversized emerald stack accepted");
        entry.putByte("Count", (byte)17);
        source.setCount(2);
        helper.assertFalse(recipe.matches(input, helper.getLevel()), "stacked pouches accepted");
        source.setCount(1);
        CompoundTag originalContents = source.getTagElement(PouchData.POUCH_TAG).getCompound(PouchData.CONTENTS_TAG).copy();
        source.getTagElement(PouchData.POUCH_TAG).putString(PouchData.CONTENTS_TAG, "invalid");
        helper.assertFalse(recipe.matches(input, helper.getLevel()), "malformed contents accepted");
        source.getTagElement(PouchData.POUCH_TAG).put(PouchData.CONTENTS_TAG, originalContents);
        for (int slot = 0; slot < 9; slot++) {
            ItemStack saved = input.getItem(slot);
            input.setItem(slot, new ItemStack(Items.DIRT));
            helper.assertFalse(recipe.matches(input, helper.getLevel()), "wrong ingredient accepted");
            helper.assertTrue(recipe.assemble(input, helper.getLevel().registryAccess()).isEmpty(), "invalid grid assembled");
            input.setItem(slot, saved);
        }
        input.setItem(4, new ItemStack(ModItems.LARGE_POUCH.get()));
        helper.assertFalse(recipe.matches(input, helper.getLevel()), "large pouch accepted as upgrade input");
        input.setItem(4, new ItemStack(ModItems.getColoredVariant(net.minecraft.world.item.DyeColor.RED, 9)));
        helper.assertFalse(recipe.matches(input, helper.getLevel()), "unsupported colored upgrade accepted");
        helper.succeed();
    }
}
