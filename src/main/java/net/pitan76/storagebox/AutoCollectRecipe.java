package net.pitan76.storagebox;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AutoCollectRecipe extends CustomRecipe {
    public AutoCollectRecipe() {

    }

    public static final MapCodec<AutoCollectRecipe> MAP_CODEC = MapCodec.unit(new AutoCollectRecipe());
    public static final StreamCodec<RegistryFriendlyByteBuf, AutoCollectRecipe> STREAM_CODEC = StreamCodec.unit(new AutoCollectRecipe());



    @Override
    public boolean matches(CraftingInput input, @NotNull Level world) {
        int count = 0;

        for (int i = 0; i < input.size(); ++i) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            ++count;
            if (!(stack.getItem() instanceof StorageBoxItem)) return false;
        }
        return count == 1;
    }

    @Override
    public @NotNull ItemStack assemble(CraftingInput input) {
        for (int i = 0; i < input.size(); ++i) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof StorageBoxItem)) continue;

            ItemStack crafted = stack.copy();
            StorageBoxItem.changeAutoCollect(crafted);
            return crafted;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return StorageBoxRecipeSerializer.CRAFTING_SPECIAL_AUTO_COLLECT_RECIPES;
    }

    @Override
    public @NotNull List<RecipeDisplay> display() {
        return super.display();
    }
}
