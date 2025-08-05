package net.pitan76.storagebox.mixin;

import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.entity.LivingEntity;
import net.pitan76.storagebox.ItemRendererHooks;
import net.pitan76.storagebox.StorageBoxItem;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.core.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class RenderStorageBoxMixin {

    @Shadow public abstract void renderItem(ItemStack stack, LivingEntity entity, ModelTransformation.Mode mode);

    @Shadow public abstract void renderItem(ItemStack stack, ModelTransformation.Mode mode);

    @Inject(method = "renderGuiItemModel", at = @At("HEAD"), cancellable = true)
    private void renderGuiItemModel(ItemStack stack, int x, int y, CallbackInfo ci) {
        if (ItemRendererHooks.onRenderItemModel((ItemRenderer) (Object) this, stack, x, y)) {
            ci.cancel();
        }
    }

    @Unique
    private static final ThreadLocal<ItemStack> RENDER_ITEM_OVERRIDING_FOR = new ThreadLocal<>();

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;)V", at = @At("HEAD"), cancellable = true)
    private void renderItem(ItemStack stack, LivingEntity entity, ModelTransformation.Mode mode, CallbackInfo ci) {
        if (RENDER_ITEM_OVERRIDING_FOR.get() == stack) return;
        if (!(stack.getItem() instanceof StorageBoxItem)) return;
        if (!StorageBoxItem.hasStackInStorageBox(stack)) return;
        ItemStack renderStack = StorageBoxItem.getStackInStorageBox(stack).copy();
        renderStack.stackSize = 1;

        RENDER_ITEM_OVERRIDING_FOR.set(stack);
        try {
            this.renderItem(renderStack, entity, mode);
        } finally {
            RENDER_ITEM_OVERRIDING_FOR.remove();
        }

        ci.cancel();
    }

    @Unique
    private static final ThreadLocal<ItemStack> RENDER_ITEM2_OVERRIDING_FOR = new ThreadLocal<>();

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;)V", at = @At("HEAD"), cancellable = true)
    private void renderItem(ItemStack stack, ModelTransformation.Mode mode, CallbackInfo ci) {
        if (RENDER_ITEM2_OVERRIDING_FOR.get() == stack) return;
        if (!(stack.getItem() instanceof StorageBoxItem)) return;
        if (!StorageBoxItem.hasStackInStorageBox(stack)) return;
        ItemStack renderStack = StorageBoxItem.getStackInStorageBox(stack).copy();
        renderStack.stackSize = 1;

        RENDER_ITEM2_OVERRIDING_FOR.set(stack);
        try {
            this.renderItem(renderStack, mode);
        } finally {
            RENDER_ITEM2_OVERRIDING_FOR.remove();
        }

        ci.cancel();
    }
}
