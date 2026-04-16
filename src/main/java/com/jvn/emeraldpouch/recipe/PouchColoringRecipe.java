package com.jvn.emeraldpouch.recipe;

import com.jvn.emeraldpouch.item.PouchItem;
import com.jvn.emeraldpouch.registry.ModItems;
import com.jvn.emeraldpouch.registry.ModRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.Tags;

public class PouchColoringRecipe extends CustomRecipe {
    public PouchColoringRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int pouchCount = 0;
        int dyeCount = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() instanceof PouchItem) {
                pouchCount++;
            } else if (stack.is(Tags.Items.DYES)) {
                dyeCount++;
            } else {
                return false;
            }

            if (pouchCount > 1 || dyeCount > 1) {
                return false;
            }
        }

        return pouchCount == 1 && dyeCount == 1;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack pouchStack = ItemStack.EMPTY;
        DyeColor dyeColor = null;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() instanceof PouchItem) {
                if (!pouchStack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                pouchStack = stack;
            } else if (stack.is(Tags.Items.DYES)) {
                if (dyeColor != null) {
                    return ItemStack.EMPTY;
                }
                dyeColor = DyeColor.getColor(stack);
            } else {
                return ItemStack.EMPTY;
            }
        }

        if (pouchStack.isEmpty() || dyeColor == null || !(pouchStack.getItem() instanceof PouchItem pouchItem)) {
            return ItemStack.EMPTY;
        }

        Item targetItem = ModItems.getColoredVariant(dyeColor, pouchItem.slotCount());
        if (targetItem == Items.AIR) {
            return ItemStack.EMPTY;
        }

        return pouchStack.transmuteCopy(targetItem, 1);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.POUCH_COLORING.get();
    }
}
