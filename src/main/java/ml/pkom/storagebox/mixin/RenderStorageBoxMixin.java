package ml.pkom.storagebox.mixin;

import ml.pkom.storagebox.ItemRendererHooks;
import ml.pkom.storagebox.StorageBoxItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ml.pkom.storagebox.StorageBoxItem.*;

@Mixin(ItemRenderer.class)
public abstract class RenderStorageBoxMixin {

    @Shadow public abstract void renderItem(ItemStack stack, ModelTransformation.Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model);

    @Inject(method = "renderGuiItemModel", at = @At("HEAD"), cancellable = true)
    protected void renderGuiItemModel(ItemStack stack, int x, int y, BakedModel model, CallbackInfo ci) {
        if (ItemRendererHooks.onRenderItemModel((ItemRenderer) (Object) this, stack, x, y, model)) {
            ci.cancel();
        }
    }

    @Unique
    private static final ThreadLocal<ItemStack> RENDER_ITEM_OVERRIDING_FOR = new ThreadLocal<>();

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V", at = @At("HEAD"), cancellable = true)
    protected void renderItem(ItemStack stack, ModelTransformation.Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
        if (RENDER_ITEM_OVERRIDING_FOR.get() == stack) {
            return;
        }

        if (stack.getItem() instanceof StorageBoxItem) {
            ClientWorld world = MinecraftClient.getInstance().world;
            if (world != null) {
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
    }
}
