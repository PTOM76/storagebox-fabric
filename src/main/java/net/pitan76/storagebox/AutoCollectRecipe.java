package net.pitan76.storagebox;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

public class AutoCollectRecipe extends SpecialCraftingRecipe {
    public AutoCollectRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        int count = 0;

        for (int i = 0; i < input.getStacks().size(); ++i) {
            ItemStack stack = input.getStacks().get(i);
            if (stack.isEmpty()) continue;
            ++count;
            if (!(stack.getItem() instanceof StorageBoxItem)) return false;
        }
        return count == 1;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        for (int i = 0; i < input.getStacks().size(); ++i) {
            ItemStack stack = input.getStacks().get(i);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof StorageBoxItem)) continue;

            ItemStack crafted = stack.copy();
            StorageBoxItem.changeAutoCollect(crafted);
            return crafted;
        }

        return null;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return StorageBoxRecipeSerializer.CRAFTING_SPECIAL_AUTO_COLLECT_RECIPES;
    }
}
