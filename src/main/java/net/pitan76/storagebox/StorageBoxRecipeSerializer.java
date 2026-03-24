package net.pitan76.storagebox;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class StorageBoxRecipeSerializer {
    public static RecipeSerializer<AutoCollectRecipe> CRAFTING_SPECIAL_AUTO_COLLECT_RECIPES = new RecipeSerializer<>(AutoCollectRecipe.MAP_CODEC, AutoCollectRecipe.STREAM_CODEC);

    public static void init() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, StorageBoxMod.key(Registries.RECIPE_SERIALIZER, "autocollectrecipes"), CRAFTING_SPECIAL_AUTO_COLLECT_RECIPES);
    }
}
