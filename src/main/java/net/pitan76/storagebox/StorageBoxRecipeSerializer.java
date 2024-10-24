package net.pitan76.storagebox;

import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;

public class StorageBoxRecipeSerializer {
    public static SpecialCraftingRecipe.SpecialRecipeSerializer<AutoCollectRecipe> CRAFTING_SPECIAL_AUTO_COLLECT_RECIPES = new SpecialCraftingRecipe.SpecialRecipeSerializer<>(AutoCollectRecipe::new);

    public static void init() {
        Registry.register(Registries.RECIPE_SERIALIZER, StorageBoxMod.key(RegistryKeys.RECIPE_SERIALIZER, "autocollectrecipes"), CRAFTING_SPECIAL_AUTO_COLLECT_RECIPES);
    }
}
