package com.jvn.emeraldpouch.registry;

import com.jvn.emeraldpouch.EmeraldPouchMod;
import com.jvn.emeraldpouch.recipe.PouchColoringRecipe;
import com.jvn.emeraldpouch.recipe.PouchUpgradeRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRecipeSerializers {
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, EmeraldPouchMod.MOD_ID);

    public static final RegistryObject<RecipeSerializer<PouchColoringRecipe>> POUCH_COLORING =
            RECIPE_SERIALIZERS.register(
                    "crafting_special_pouch_coloring",
                    () -> new SimpleCraftingRecipeSerializer<>(PouchColoringRecipe::new)
            );

    public static final RegistryObject<RecipeSerializer<PouchUpgradeRecipe>> POUCH_UPGRADE =
            RECIPE_SERIALIZERS.register("pouch_upgrade", PouchUpgradeRecipe.Serializer::new);

    private ModRecipeSerializers() {
    }

    public static void register(IEventBus modEventBus) {
        RECIPE_SERIALIZERS.register(modEventBus);
    }
}
