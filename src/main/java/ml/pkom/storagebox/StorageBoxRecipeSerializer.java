package ml.pkom.storagebox;

import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class StorageBoxRecipeSerializer {
    public static SpecialRecipeSerializer<AutoCollectRecipe> CRAFTING_SPECIAL_AUTO_COLLECT_RECIPES = new SpecialRecipeSerializer<>(AutoCollectRecipe::new);

    public static void init() {
        Registry.register(Registries.RECIPE_SERIALIZER, StorageBoxMod.id("autocollectrecipes"), CRAFTING_SPECIAL_AUTO_COLLECT_RECIPES);
    }
}
