package com.jvn.emeraldpouch.recipe;

import com.jvn.emeraldpouch.item.PouchItem;
import com.jvn.emeraldpouch.pouch.PouchData;
import com.jvn.emeraldpouch.registry.ModRecipeSerializers;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;

/** A shaped upgrade with vanilla matching/consumption and an input-dependent result. */
public final class PouchUpgradeRecipe extends ShapedRecipe {
    private final ShapedRecipe base;

    public PouchUpgradeRecipe(ShapedRecipe base) {
        super(base.getGroup(), base.category(),
                new ShapedRecipePattern(base.getWidth(), base.getHeight(), base.getIngredients(), Optional.empty()),
                base.getResultItem(null), base.showNotification());
        // Retain the original pattern data for JSON encoding and recipe synchronization.
        this.base = base;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return base.matches(input, level) && !upgradeSource(input).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        if (!base.matches(input, null)) {
            return ItemStack.EMPTY;
        }
        ItemStack source = upgradeSource(input);
        if (source.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack result = base.assemble(input, registries);
        // Components are immutable values. Copy the entire patch, including container,
        // custom name and pouch toggles, without loading/resaving or compacting slots.
        result.applyComponents(source.getComponentsPatch());
        return result;
    }

    private ItemStack upgradeSource(CraftingInput input) {
        ItemStack source = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof PouchItem) {
                if (!source.isEmpty() || stack.getCount() != 1) {
                    return ItemStack.EMPTY;
                }
                source = stack;
            }
        }

        ItemStack target = base.getResultItem(null);
        if (!(source.getItem() instanceof PouchItem from)
                || !(target.getItem() instanceof PouchItem to)
                || target.getCount() != 1 || to.slotCount() <= from.slotCount()) {
            return ItemStack.EMPTY;
        }

        // Inspect the raw component: loadContents would hide slots beyond capacity.
        List<ItemStack> contents = source.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
                .stream().toList();
        if (contents.size() > to.slotCount()) {
            return ItemStack.EMPTY;
        }
        for (ItemStack stack : contents) {
            if (!stack.isEmpty() && (!PouchData.isAllowedContent(stack)
                    || stack.getCount() <= 0 || stack.getCount() > stack.getMaxStackSize())) {
                return ItemStack.EMPTY;
            }
        }
        return source;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.POUCH_UPGRADE.get();
    }

    public static final class Serializer implements RecipeSerializer<PouchUpgradeRecipe> {
        private static final MapCodec<PouchUpgradeRecipe> CODEC = ShapedRecipe.Serializer.CODEC
                .xmap(PouchUpgradeRecipe::new, recipe -> recipe.base);
        private static final StreamCodec<RegistryFriendlyByteBuf, PouchUpgradeRecipe> STREAM_CODEC =
                ShapedRecipe.Serializer.STREAM_CODEC.map(PouchUpgradeRecipe::new, recipe -> recipe.base);

        @Override
        public MapCodec<PouchUpgradeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, PouchUpgradeRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
