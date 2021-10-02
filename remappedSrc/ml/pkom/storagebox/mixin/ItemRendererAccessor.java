package ml.pkom.storagebox.mixin;

import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemRenderer.class)
public interface ItemRendererAccessor {

    @Invoker("renderGuiItemModel")
    public void invokeRenderGuiItemModel(ItemStack stack, int x, int y, BakedModel model);

}
