package net.pitan76.storagebox;

import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemModelManagerHooks {

    private static final ThreadLocal<ItemStack> OVERRIDING_FOR = new ThreadLocal<>();

    public static boolean update(ItemModelResolver itemModelManager, ItemStackRenderState renderState, ItemStack stack, ItemDisplayContext displayContext, Level world, ItemOwner context, int seed) {
        if (OVERRIDING_FOR.get() == stack) return false;
        if (!(stack.getItem() instanceof StorageBoxItem)) return false;

        if (world == null) return false;
        if (!StorageBoxItem.hasStackInStorageBox(stack)) return false;
        ItemStack renderStack = StorageBoxItem.getStackInStorageBox(stack).copy();
        if (renderStack.isEmpty()) return false;
        renderStack.setCount(1);

        try {
            itemModelManager.appendItemLayers(renderState, renderStack, displayContext, world, context, seed);
        } finally {
            OVERRIDING_FOR.remove();
        }
        return true;
    }
}
