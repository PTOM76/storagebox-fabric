package ml.pkom.storagebox.mixin;

import ml.pkom.storagebox.ItemRendererHooks;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class RenderStorageBoxMixin {

    @Inject(method = "renderGuiItemModel", at = @At("HEAD"), cancellable = true)
    protected void render(ItemStack stack, int x, int y, BakedModel model, CallbackInfo ci) {
        if (ItemRendererHooks.onRenderItemModel((ItemRenderer) (Object) this, stack, x, y, model)) {
            ci.cancel();
        }
    }
}
