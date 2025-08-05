package net.pitan76.storagebox;

import net.pitan76.storagebox.mixin.ItemRendererAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.core.item.ItemStack;

import static net.pitan76.storagebox.StorageBoxItem.*;

public class ItemRendererHooks {

    private static final ThreadLocal<ItemStack> OVERRIDING_FOR = new ThreadLocal<>();

    public static boolean onRenderItemModel(ItemRenderer renderer, ItemStack stack, int x, int y) {
        if (OVERRIDING_FOR.get() == stack) return false;
        if (!(stack.getItem() instanceof StorageBoxItem)) return false;
        ClientWorld world = MinecraftClient.getInstance().world;

        if (world == null) return false;
        if (!hasStackInStorageBox(stack)) return false;
        ItemStack renderStack = getStackInStorageBox(stack).copy();

        renderStack.stackSize = 1;
        OVERRIDING_FOR.set(stack);
        try {
            ((ItemRendererAccessor) renderer).invokeRenderGuiItemModel(renderStack, x, y);
        } finally {
            OVERRIDING_FOR.remove();
        }
        return true;
    }
}
