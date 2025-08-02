package net.pitan76.storagebox.mixin;

import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.entity.LivingEntity;
import net.pitan76.storagebox.ItemRendererHooks;
import net.pitan76.storagebox.StorageBoxItem;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class RenderStorageBoxMixin {
    @Shadow public abstract void method_12460(ItemStack itemStack, LivingEntity livingEntity, ModelTransformation.Mode mode, boolean bl);

    @Inject(method = "method_12456", at = @At("HEAD"), cancellable = true)
    private void renderGuiItemModel(ItemStack stack, int x, int y, BakedModel model, CallbackInfo ci) {
        if (ItemRendererHooks.onRenderItemModel((ItemRenderer) (Object) this, stack, x, y, model)) {
            ci.cancel();
        }
    }

    @Unique
    private static final ThreadLocal<ItemStack> RENDER_ITEM_OVERRIDING_FOR = new ThreadLocal<>();

    @Inject(method = "method_12460", at = @At("HEAD"), cancellable = true)
    private void method_12460(ItemStack stack, LivingEntity entity, ModelTransformation.Mode type, boolean bl, CallbackInfo ci) {
        if (RENDER_ITEM_OVERRIDING_FOR.get() == stack) return;
        if (!(stack.getItem() instanceof StorageBoxItem)) return;
        if (!StorageBoxItem.hasStackInStorageBox(stack)) return;
        ItemStack renderStack = StorageBoxItem.getStackInStorageBox(stack).copy();
        renderStack.setCount(1);

        RENDER_ITEM_OVERRIDING_FOR.set(stack);
        try {
            this.method_12460(renderStack, entity, type, bl);
        } finally {
            RENDER_ITEM_OVERRIDING_FOR.remove();
        }

        ci.cancel();
    }
}
