package net.pitan76.storagebox;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class AutoCollectRecipe implements RecipeType {
    private final String group;

    public AutoCollectRecipe(String group) {
        this.group = group;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public String method_14253() {
        return group;
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

    @Environment(EnvType.CLIENT)
    public boolean method_14250(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public ItemStack getOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public DefaultedList<ItemStack> method_13670(CraftingInventory inventory) {
        return DefaultedList.ofSize(1, getResult(inventory));
    }
}
