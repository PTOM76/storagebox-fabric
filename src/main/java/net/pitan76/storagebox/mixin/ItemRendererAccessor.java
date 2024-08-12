package net.pitan76.storagebox.mixin;

import net.minecraft.client.render.item.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemRenderer.class)
public interface ItemRendererAccessor {

    /*
    @Invoker("renderBakedItemModel")
    public void invokeRenderBakedItemModel(BakedModel model, ItemStack stack, int light, int overlay, MatrixStack matrices, VertexConsumer vertices);


     */
}
