package net.pitan76.storagebox;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapelessRecipeType;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class AutoCollectRecipe extends ShapelessRecipeType {
    public AutoCollectRecipe(ItemStack stack) {
        super("", stack, DefaultedList.ofSize(1, Ingredient.method_14248(stack)));
    }

    @Override
    public boolean matches(CraftingInventory inventory, World world) {
        int count = 0;

        for (int i = 0; i < inventory.getInvSize(); ++i) {
            ItemStack stack = inventory.getInvStack(i);
            if (stack.isEmpty()) continue;
            ++count;
            if (!(stack.getItem() instanceof StorageBoxItem)) return false;
        }
        return count == 1;
    }

    @Override
    public ItemStack getResult(CraftingInventory inventory) {
        for (int i = 0; i < inventory.getInvSize(); ++i) {
            ItemStack stack = inventory.getInvStack(i);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof StorageBoxItem)) continue;

            ItemStack crafted = stack.copy();
            StorageBoxItem.changeAutoCollect(crafted);
            return crafted;
        }

        return null;
    }
}
