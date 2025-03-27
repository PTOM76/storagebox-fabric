package net.pitan76.storagebox.mixin;

import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.pitan76.storagebox.ItemModelManagerHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemModelManager.class)
public class ItemModelManagerMixin {
    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void update(ItemRenderState renderState, ItemStack stack, ItemDisplayContext displayContext, World world, LivingEntity entity, int seed, CallbackInfo ci) {
        if (ItemModelManagerHooks.update((ItemModelManager) (Object) this, renderState, stack, displayContext, world, entity, seed)) {
            ci.cancel();
        }
    }
}
