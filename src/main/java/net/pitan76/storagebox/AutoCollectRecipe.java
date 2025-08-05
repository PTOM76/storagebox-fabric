package net.pitan76.storagebox;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.core.item.ItemStack;
import net.minecraft.recipe.ShapelessRecipeType;
import net.minecraft.world.World;

import java.util.Collections;

public class AutoCollectRecipe extends ShapelessRecipeType {
    public AutoCollectRecipe(ItemStack stack) {
        super(stack, Collections.singletonList(stack));
    }

    @Override
    public boolean matches(CraftingInventory inventory, World world) {
        int count = 0;

        for (int i = 0; i < inventory.getInvSize(); ++i) {
            ItemStack stack = inventory.getInvStack(i);
            if (stack == null) continue;
            ++count;
            if (!(stack.getItem() instanceof StorageBoxItem)) return false;
        }
        return count == 1;
    }

    @Override
    public ItemStack getResult(CraftingInventory inventory) {
        for (int i = 0; i < inventory.getInvSize(); ++i) {
            ItemStack stack = inventory.getInvStack(i);
            if (stack == null) continue;
            if (!(stack.getItem() instanceof StorageBoxItem)) continue;

            ItemStack crafted = stack.copy();
            StorageBoxItem.changeAutoCollect(crafted);
            return crafted;
        }

        return null;
    }
}
