package net.pitan76.storagebox;

import net.minecraft.item.ModelTransformationMode;
import net.pitan76.storagebox.mixin.ItemRendererAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;

public class ItemRendererHooks {

    private static final ThreadLocal<ItemStack> OVERRIDING_FOR = new ThreadLocal<>();

    public static boolean onRenderItemModel(ItemRenderer renderer, BakedModel model, ItemStack stack, int light, int overlay, MatrixStack matrices, VertexConsumer vertices) {
        if (OVERRIDING_FOR.get() == stack) return false;
        if (!(stack.getItem() instanceof StorageBoxItem)) return false;
        ClientWorld world = MinecraftClient.getInstance().world;

        if (world == null) return false;
        if (!StorageBoxItem.hasStackInStorageBox(stack)) return false;
        ItemStack renderStack = StorageBoxItem.getStackInStorageBox(stack).copy();

        if (renderStack.isEmpty()) return false;

        renderStack.setCount(1);
        BakedModel realModel = MinecraftClient.getInstance().getItemRenderer()
                .getModel(renderStack, MinecraftClient.getInstance().player, ModelTransformationMode.GUI);
        OVERRIDING_FOR.set(stack);
        try {
            ((ItemRendererAccessor) renderer).invokeRenderBakedItemModel(realModel, renderStack, light, overlay, matrices, vertices);
        } finally {
            OVERRIDING_FOR.remove();
        }
        return true;
    }
}
