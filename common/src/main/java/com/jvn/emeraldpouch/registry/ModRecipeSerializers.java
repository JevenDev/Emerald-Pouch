package com.jvn.emeraldpouch.registry;

import com.jvn.emeraldpouch.EmeraldPouchMod;
import com.jvn.emeraldpouch.recipe.PouchColoringRecipe;
import com.jvn.emeraldpouch.recipe.PouchUpgradeRecipe;
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

    public static final RegistrySupplier<RecipeSerializer<PouchUpgradeRecipe>> POUCH_UPGRADE =
            RECIPE_SERIALIZERS.register("pouch_upgrade", PouchUpgradeRecipe.Serializer::new);

    private ModRecipeSerializers() {
    }

        public static void register() {
                RECIPE_SERIALIZERS.register();
    }
}
