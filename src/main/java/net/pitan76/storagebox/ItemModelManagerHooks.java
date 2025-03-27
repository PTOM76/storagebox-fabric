package net.pitan76.storagebox;

import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemModelManagerHooks {

    private static final ThreadLocal<ItemStack> OVERRIDING_FOR = new ThreadLocal<>();

    public static boolean update(ItemModelManager itemModelManager, ItemRenderState renderState, ItemStack stack, ItemDisplayContext displayContext, World world, LivingEntity entity, int seed) {
        if (OVERRIDING_FOR.get() == stack) return false;
        if (!(stack.getItem() instanceof StorageBoxItem)) return false;

        if (world == null) return false;
        if (!StorageBoxItem.hasStackInStorageBox(stack)) return false;
        ItemStack renderStack = StorageBoxItem.getStackInStorageBox(stack).copy();
        if (renderStack.isEmpty()) return false;
        renderStack.setCount(1);

        try {
            itemModelManager.update(renderState, renderStack, displayContext, world, entity, seed);
        } finally {
            OVERRIDING_FOR.remove();
        }
        return true;
    }
}
