package com.jvn.emeraldpouch.registry;

import com.jvn.emeraldpouch.EmeraldPouchMod;
import com.jvn.emeraldpouch.recipe.PouchColoringRecipe;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

public final class ModRecipeSerializers {
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(EmeraldPouchMod.MOD_ID, Registries.RECIPE_SERIALIZER);

    public static final RegistrySupplier<RecipeSerializer<PouchColoringRecipe>> POUCH_COLORING =
            RECIPE_SERIALIZERS.register(
                    "crafting_special_pouch_coloring",
                    () -> new SimpleCraftingRecipeSerializer<>(PouchColoringRecipe::new)
            );

    private ModRecipeSerializers() {
    }

        public static void register() {
                RECIPE_SERIALIZERS.register();
    }
}
