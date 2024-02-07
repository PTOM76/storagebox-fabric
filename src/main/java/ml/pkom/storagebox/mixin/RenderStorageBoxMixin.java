package ml.pkom.storagebox.mixin;

import ml.pkom.storagebox.ItemRendererHooks;
import ml.pkom.storagebox.StorageBoxItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ml.pkom.storagebox.StorageBoxItem.getStackInStorageBox;
import static ml.pkom.storagebox.StorageBoxItem.hasStackInStorageBox;

@Mixin(ItemRenderer.class)
public abstract class RenderStorageBoxMixin {

    @Shadow public abstract void renderItem(ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model);

    @Inject(method = "renderBakedItemModel", at = @At("HEAD"), cancellable = true)
    protected void renderGuiItemModel(BakedModel model, ItemStack stack, int light, int overlay, MatrixStack matrices, VertexConsumer vertices, CallbackInfo ci) {
        if (ItemRendererHooks.onRenderItemModel((ItemRenderer) (Object) this, model, stack, light, overlay, matrices, vertices)) {
            ci.cancel();
        }
    }

    private static final ThreadLocal<ItemStack> RENDER_ITEM_OVERRIDING_FOR = new ThreadLocal<>();

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V", at = @At("HEAD"), cancellable = true)
    protected void renderItem(ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
        if (RENDER_ITEM_OVERRIDING_FOR.get() == stack) return;
        if (!(stack.getItem() instanceof StorageBoxItem)) return;
        ClientWorld world = MinecraftClient.getInstance().world;

        if (world == null) return;
        if (!hasStackInStorageBox(stack)) return;
        ItemStack renderStack = getStackInStorageBox(stack).copy();
        renderStack.setCount(1);
        BakedModel realModel = MinecraftClient.getInstance().getItemRenderer().getModels()
                .getModel(renderStack);
        RENDER_ITEM_OVERRIDING_FOR.set(stack);
        try {
            this.renderItem(stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, realModel);
        } finally {
            RENDER_ITEM_OVERRIDING_FOR.remove();
        }
        ci.cancel();
    }
}
