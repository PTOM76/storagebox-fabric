package net.pitan76.storagebox;

import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.util.registry.Registry;

public class StorageBoxRecipeSerializer {
    public static SpecialRecipeSerializer<AutoCollectRecipe> CRAFTING_SPECIAL_AUTO_COLLECT_RECIPES = new SpecialRecipeSerializer<>(AutoCollectRecipe::new);

    public static void init() {
        Registry.register(Registry.RECIPE_SERIALIZER, StorageBoxMod.id("autocollectrecipes"), CRAFTING_SPECIAL_AUTO_COLLECT_RECIPES);
    }
}
