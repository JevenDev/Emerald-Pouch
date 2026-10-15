package com.jvn.emeraldpouch.recipe;

import com.google.gson.JsonObject;
import com.jvn.emeraldpouch.item.PouchItem;
import com.jvn.emeraldpouch.pouch.PouchData;
import com.jvn.emeraldpouch.registry.ModRecipeSerializers;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

/** Keep vanilla shaped matching/consumption, copying the input pouch's complete NBT. */
public final class PouchUpgradeRecipe extends ShapedRecipe {
    private final ShapedRecipe base;

    public PouchUpgradeRecipe(ShapedRecipe base) {
        super(base.getId(), base.getGroup(), base.category(), base.getWidth(), base.getHeight(),
                base.getIngredients(), base.getResultItem(RegistryAccess.EMPTY), base.showNotification());
        this.base = base;
    }

    @Override
    public boolean matches(CraftingContainer input, Level level) {
        return base.matches(input, level) && !upgradeSource(input).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer input, RegistryAccess registries) {
        if (!base.matches(input, null)) {
            return ItemStack.EMPTY;
        }
        ItemStack source = upgradeSource(input);
        if (source.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack template = base.assemble(input, registries);
        // Use Forge's full stack serialization so transferable capabilities survive too.
        CompoundTag serialized = source.save(new CompoundTag()).copy();
        serialized.putString("id", BuiltInRegistries.ITEM.getKey(template.getItem()).toString());
        serialized.putByte("Count", (byte)1);
        ItemStack result = ItemStack.of(serialized);
        if (template.hasTag()) {
            CompoundTag tag = template.getTag().copy();
            if (result.hasTag()) {
                tag.merge(result.getTag());
            }
            result.setTag(tag);
        }
        return result;
    }

    private ItemStack upgradeSource(CraftingContainer input) {
        ItemStack source = ItemStack.EMPTY;
        for (int i = 0; i < input.getContainerSize(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof PouchItem) {
                if (!source.isEmpty() || stack.getCount() != 1) {
                    return ItemStack.EMPTY;
                }
                source = stack;
            }
        }
        ItemStack target = base.getResultItem(RegistryAccess.EMPTY);
        if (!(source.getItem() instanceof PouchItem from)
                || !(target.getItem() instanceof PouchItem to)
                || target.getCount() != 1 || to.slotCount() <= from.slotCount()) {
            return ItemStack.EMPTY;
        }

        // Inspect raw serialized slots: ContainerHelper.loadAllItems skips overflow
        // and overwrites duplicate slot entries, which would hide invalid contents.
        if (source.hasTag() && source.getTag().contains(PouchData.POUCH_TAG)
                && !source.getTag().contains(PouchData.POUCH_TAG, Tag.TAG_COMPOUND)) {
            return ItemStack.EMPTY;
        }
        CompoundTag pouch = source.getTagElement(PouchData.POUCH_TAG);
        if (pouch != null && pouch.contains(PouchData.CONTENTS_TAG)) {
            if (!pouch.contains(PouchData.CONTENTS_TAG, Tag.TAG_COMPOUND)) {
                return ItemStack.EMPTY;
            }
            CompoundTag contents = pouch.getCompound(PouchData.CONTENTS_TAG);
            if (contents.contains("Items") && !contents.contains("Items", Tag.TAG_LIST)) {
                return ItemStack.EMPTY;
            }
            if (contents.get("Items") instanceof ListTag list && !list.isEmpty()
                    && list.getElementType() != Tag.TAG_COMPOUND) {
                return ItemStack.EMPTY;
            }
            ListTag entries = contents.getList("Items", Tag.TAG_COMPOUND);
            Set<Integer> slots = new HashSet<>();
            for (int i = 0; i < entries.size(); i++) {
                CompoundTag entry = entries.getCompound(i);
                int slot = entry.getByte("Slot") & 255;
                ItemStack stack = ItemStack.of(entry);
                if (!entry.contains("Slot", Tag.TAG_BYTE) || slot >= to.slotCount() || !slots.add(slot)
                        || stack.isEmpty() || !PouchData.isAllowedContent(stack)
                        || stack.getCount() > stack.getMaxStackSize()) {
                    return ItemStack.EMPTY;
                }
            }
        }
        return source;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.POUCH_UPGRADE.get();
    }

    public static final class Serializer implements RecipeSerializer<PouchUpgradeRecipe> {
        private final ShapedRecipe.Serializer shaped = new ShapedRecipe.Serializer();

        @Override
        public PouchUpgradeRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new PouchUpgradeRecipe(shaped.fromJson(id, json));
        }

        @Override
        public PouchUpgradeRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            return new PouchUpgradeRecipe(shaped.fromNetwork(id, buffer));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, PouchUpgradeRecipe recipe) {
            shaped.toNetwork(buffer, recipe.base);
        }
    }
}
