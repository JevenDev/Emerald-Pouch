package com.jvn.emeraldpouch.recipe;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonParser;
import com.jvn.emeraldpouch.client.PouchHudVisibility;
import com.jvn.emeraldpouch.item.PouchItem;
import com.jvn.emeraldpouch.pouch.PouchData;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.GameType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PouchUpgradeRecipeTest {
    private static Item small;
    private static Item large;
    private static Item colored;
    private static PouchUpgradeRecipe recipe;
    private static RegistryAccess registries;

    @BeforeAll
    static void bootstrap() throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        // Isolated vanilla test JVM: reopen only the item registry for fixture registration.
        var frozen = net.minecraft.core.MappedRegistry.class.getDeclaredField("frozen");
        frozen.setAccessible(true);
        frozen.set(BuiltInRegistries.ITEM, false);
        var holders = net.minecraft.core.MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
        holders.setAccessible(true);
        holders.set(BuiltInRegistries.ITEM, new java.util.IdentityHashMap<>());
        small = register("pouch", 9);
        large = register("large_pouch", 18);
        colored = register("red_pouch", 9);
        BuiltInRegistries.ITEM.freeze();
        registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var json = JsonParser.parseString(Files.readString(Path.of(System.getProperty("pouch.projectDir"), "src/main/resources/data/emeraldpouch/recipe/large_pouch.json")));
        assertEquals("emeraldpouch:pouch_upgrade", json.getAsJsonObject().get("type").getAsString());
        recipe = new PouchUpgradeRecipe.Serializer().codec().codec()
                .parse(registries.createSerializationContext(JsonOps.INSTANCE), json).getOrThrow();
    }

    private static Item register(String name, int slots) {
        return Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("emeraldpouch", name),
                new PouchItem(slots, new Item.Properties().stacksTo(1)));
    }

    private static CraftingInput grid(ItemStack pouch) {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            items.add(i == 4 ? pouch : new ItemStack(Items.LEATHER, 64));
        }
        return CraftingInput.of(3, 3, items);
    }

    private static ItemStack pouch(int value) {
        ItemStack pouch = new ItemStack(small);
        List<ItemStack> contents = new ArrayList<>();
        int blocks = value / 9;
        while (blocks > 0) {
            int count = Math.min(64, blocks);
            contents.add(new ItemStack(Items.EMERALD_BLOCK, count));
            blocks -= count;
        }
        if (value % 9 > 0) {
            contents.add(new ItemStack(Items.EMERALD, value % 9));
        }
        PouchData.saveContents(pouch, contents);
        return pouch;
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 8, 9, 63, 64, 65, 511, 4096, 4608, 4609, 4616, 5175, 5184})
    void preservesBalanceAndPreviewIsPure(int value) {
        ItemStack source = pouch(value);
        ItemStack before = source.copy();
        CraftingInput input = grid(source);
        for (int i = 0; i < 20; i++) {
            assertTrue(recipe.matches(input, null));
            ItemStack result = recipe.assemble(input, registries);
            assertTrue(result.is(large));
            assertEquals(1, result.getCount());
            assertEquals(value, PouchData.getStoredEmeraldEquivalent(result));
            assertEquals(source.get(DataComponents.CONTAINER), result.get(DataComponents.CONTAINER));
            assertTrue(ItemStack.matches(before, source));
            // Mutating one preview must not alias the source or subsequent results.
            PouchData.saveContents(result, List.of());
            assertEquals(value, PouchData.getStoredEmeraldEquivalent(source));
        }
        assertTrue(recipe.getRemainingItems(input).stream().allMatch(ItemStack::isEmpty));
    }

    @Test
    void preservesNamesTogglesCustomDataAndContainedItemMetadata() {
        ItemStack source = pouch(81);
        source.set(DataComponents.CUSTOM_NAME, Component.literal("Trading fund"));
        CompoundTag unrelated = new CompoundTag();
        unrelated.putString("another_mod", "keep me");
        source.set(DataComponents.CUSTOM_DATA, CustomData.of(unrelated));
        PouchData.setAutoCompactEnabled(source, true);
        PouchData.setAutoPickupEnabled(source, true);
        PouchData.setOpenedVisualEnabled(source, true);
        ItemStack emerald = new ItemStack(Items.EMERALD, 17);
        emerald.set(DataComponents.CUSTOM_NAME, Component.literal("Named emeralds"));
        List<ItemStack> contents = new ArrayList<>();
        for (int i = 0; i < 9; i++) contents.add(ItemStack.EMPTY);
        contents.set(8, emerald);
        PouchData.saveContents(source, contents);
        ItemStack result = recipe.assemble(grid(source), registries);
        assertEquals(source.getComponentsPatch(), result.getComponentsPatch());
        assertEquals(17, PouchData.getStoredEmeraldEquivalent(result));
        assertTrue(ItemStack.matches(emerald, PouchData.loadContents(result).get(8)));
        assertTrue(PouchData.isAutoCompactEnabled(result));
        assertTrue(PouchData.isAutoPickupEnabled(result));
    }

    @Test
    void checksTheFullShapedRecipeAndCorrectPouch() {
        CraftingInput valid = grid(pouch(130));
        for (int slot = 0; slot < 9; slot++) {
            List<ItemStack> missing = new ArrayList<>(valid.items());
            missing.set(slot, ItemStack.EMPTY);
            assertRejected(CraftingInput.of(3, 3, missing));
            List<ItemStack> wrong = new ArrayList<>(valid.items());
            wrong.set(slot, new ItemStack(Items.DIRT));
            assertRejected(CraftingInput.of(3, 3, wrong));
        }
        assertRejected(grid(new ItemStack(large)));
        // The existing recipe only accepts the undyed regular pouch. Do not broaden it.
        assertRejected(grid(new ItemStack(colored)));
        List<ItemStack> moved = new ArrayList<>(valid.items());
        moved.set(0, valid.getItem(4));
        moved.set(4, new ItemStack(Items.LEATHER));
        assertRejected(CraftingInput.of(3, 3, moved));
        assertFalse(recipe.canCraftInDimensions(2, 2));
    }

    @Test
    void rejectsStackedPouchesInvalidContentsAndOverflowWithoutTruncation() {
        ItemStack stacked = pouch(81);
        stacked.setCount(2);
        assertRejected(grid(stacked));
        ItemStack invalid = new ItemStack(small);
        PouchData.saveContents(invalid, List.of(new ItemStack(Items.DIAMOND)));
        assertRejected(grid(invalid));
        PouchData.saveContents(invalid, List.of(new ItemStack(Items.EMERALD, 65)));
        assertRejected(grid(invalid));
        List<ItemStack> overflow = new ArrayList<>();
        for (int i = 0; i < 19; i++) overflow.add(new ItemStack(Items.EMERALD_BLOCK, 64));
        PouchData.saveContents(invalid, overflow);
        ItemStack before = invalid.copy();
        assertRejected(grid(invalid));
        assertTrue(ItemStack.matches(before, invalid));
        // An otherwise inaccessible slot that fits the larger pouch must be retained.
        overflow.removeLast();
        PouchData.saveContents(invalid, overflow);
        ItemStack result = recipe.assemble(grid(invalid), registries);
        assertEquals(18 * 64 * 9, PouchData.getStoredEmeraldEquivalent(result));
    }

    @Test
    void refusesNonUpgradesMultiplePouchesAndMultipleOutputs() {
        var pattern = ShapedRecipePattern.of(Map.of('P', Ingredient.of(small)), "P");
        var downgrade = new PouchUpgradeRecipe(new ShapedRecipe("", CraftingBookCategory.MISC, pattern, new ItemStack(small)));
        assertFalse(downgrade.matches(CraftingInput.of(1, 1, List.of(pouch(5))), null));
        var duplicates = new PouchUpgradeRecipe(new ShapedRecipe("", CraftingBookCategory.MISC, pattern, new ItemStack(large, 2)));
        assertTrue(duplicates.assemble(CraftingInput.of(1, 1, List.of(pouch(5))), registries).isEmpty());
        var two = ShapedRecipePattern.of(Map.of('P', Ingredient.of(small)), "PP");
        var merge = new PouchUpgradeRecipe(new ShapedRecipe("", CraftingBookCategory.MISC, two, new ItemStack(large)));
        assertFalse(merge.matches(CraftingInput.of(2, 1, List.of(pouch(5), pouch(8))), null));
    }

    @Test
    void jsonAndNetworkRoundTripsRetainDynamicAssemblyAndRecipeBookShape() {
        var serializer = new PouchUpgradeRecipe.Serializer();
        var ops = registries.createSerializationContext(JsonOps.INSTANCE);
        var json = serializer.codec().codec().encodeStart(ops, recipe).getOrThrow();
        var decoded = serializer.codec().codec().parse(ops, json).getOrThrow();
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
        try {
            serializer.streamCodec().encode(buffer, decoded);
            var networkRecipe = serializer.streamCodec().decode(buffer);
            assertTrue(networkRecipe instanceof ShapedRecipe);
            assertEquals(3, networkRecipe.getWidth());
            assertEquals(3, networkRecipe.getHeight());
            assertEquals(9, networkRecipe.getIngredients().size());
            assertEquals(large, networkRecipe.getResultItem(registries).getItem());
            assertEquals(5184, PouchData.getStoredEmeraldEquivalent(networkRecipe.assemble(grid(pouch(5184)), registries)));
            assertRejected(grid(new ItemStack(Items.DIRT)));
        } finally {
            buffer.release();
        }
    }

    @Test
    void repeatedCraftingRecomputesFromEachNewInput() {
        int totalInput = 0;
        int totalOutput = 0;
        for (int value : new int[]{0, 1, 5184, 64, 4096, 9}) {
            CraftingInput input = grid(pouch(value));
            recipe.assemble(input, registries); // discarded preview
            ItemStack taken = recipe.assemble(input, registries);
            totalInput += value;
            totalOutput += PouchData.getStoredEmeraldEquivalent(taken);
            // Model vanilla consumption; this is not a menu/shift-click integration test.
            input.items().forEach(stack -> stack.shrink(1));
            assertRejected(input);
        }
        assertEquals(totalInput, totalOutput);
    }

    @Test
    void hudIsHiddenOnlyForSpectatorOrHiddenGui() {
        for (GameType mode : GameType.values()) {
            assertEquals(mode != GameType.SPECTATOR, PouchHudVisibility.isVisible(mode == GameType.SPECTATOR, false), mode.getName());
            assertFalse(PouchHudVisibility.isVisible(mode == GameType.SPECTATOR, true));
        }
    }

    private static void assertRejected(CraftingInput input) {
        assertFalse(recipe.matches(input, null));
        assertTrue(recipe.assemble(input, registries).isEmpty());
    }
}
