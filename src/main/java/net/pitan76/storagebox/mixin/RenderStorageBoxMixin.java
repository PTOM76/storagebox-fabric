package net.pitan76.storagebox.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.world.World;
import net.pitan76.storagebox.StorageBoxItem;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.pitan76.storagebox.StorageBoxItem.getStackInStorageBox;
import static net.pitan76.storagebox.StorageBoxItem.hasStackInStorageBox;

@Mixin(ItemRenderer.class)
public abstract class RenderStorageBoxMixin {

    @Shadow public abstract void renderItem(@Nullable LivingEntity entity, ItemStack stack, ModelTransformationMode transformationMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, @Nullable World world, int light, int overlay, int seed);

    @Unique
    private static final ThreadLocal<ItemStack> RENDER_ITEM_OVERRIDING_FOR = new ThreadLocal<>();

    @Inject(method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;III)V", at = @At("HEAD"), cancellable = true)
    protected void renderItem(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, World world, int light, int overlay, int seed, CallbackInfo ci) {
        if (RENDER_ITEM_OVERRIDING_FOR.get() == stack) return;
        if (!(stack.getItem() instanceof StorageBoxItem)) return;

        if (world == null) return;
        if (!hasStackInStorageBox(stack)) return;
        ItemStack renderStack = getStackInStorageBox(stack).copy();
        renderStack.setCount(1);
//        BakedModel realModel = MinecraftClient.getInstance().getBakedModelManager()
//                .getModel(Registries.ITEM.getId(renderStack.getItem()));
        RENDER_ITEM_OVERRIDING_FOR.set(stack);
        try {
            this.renderItem(entity, renderStack, renderMode, leftHanded, matrices, vertexConsumers, world, light, overlay, seed);
        } finally {
            RENDER_ITEM_OVERRIDING_FOR.remove();
        }
        ci.cancel();
    }
}
