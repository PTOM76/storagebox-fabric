package net.pitan76.storagebox;

import net.pitan76.storagebox.mixin.ItemRendererAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;

import static net.pitan76.storagebox.StorageBoxItem.*;

public class ItemRendererHooks {

    private static final ThreadLocal<ItemStack> OVERRIDING_FOR = new ThreadLocal<>();

    public static boolean onRenderItemModel(ItemRenderer renderer, MatrixStack matrices, ItemStack stack, int x, int y, BakedModel model) {
        if (OVERRIDING_FOR.get() == stack) return false;
        if (!(stack.getItem() instanceof StorageBoxItem)) return false;
        ClientWorld world = MinecraftClient.getInstance().world;

        if (world == null) return false;
        if (!hasStackInStorageBox(stack)) return false;
        ItemStack renderStack = getStackInStorageBox(stack).copy();

        if (renderStack.isEmpty()) return false;

        renderStack.setCount(1);
        BakedModel realModel = MinecraftClient.getInstance().getItemRenderer().getModels()
                .getModel(renderStack);
        OVERRIDING_FOR.set(stack);
        try {
            ((ItemRendererAccessor) renderer).invokeRenderGuiItemModel(matrices, renderStack, x, y, realModel);
        } finally {
            OVERRIDING_FOR.remove();
        }
        return true;
    }
}
