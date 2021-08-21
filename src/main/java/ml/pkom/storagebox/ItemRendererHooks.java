package ml.pkom.storagebox;

import ml.pkom.storagebox.mixin.ItemRendererAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class ItemRendererHooks {

    private static final ThreadLocal<ItemStack> OVERRIDING_FOR = new ThreadLocal<>();

    public static boolean onRenderItemModel(ItemRenderer renderer, ItemStack stack, int x, int y,
                                            BakedModel model) {
        if (OVERRIDING_FOR.get() == stack) {
            return false;
        }

        if (stack.getItem() instanceof StorageBoxItem) {
            ClientWorld world = MinecraftClient.getInstance().world;
            if (world != null && stack.hasNbt()) {
                NbtCompound tag = stack.getNbt();
                if (!tag.contains("item")) return false;
                ItemStack renderStack = ItemStack.fromNbt(tag.getCompound("item")).copy();
                if (!renderStack.isEmpty()) {
                    renderStack.setCount(1);
                    BakedModel realModel = MinecraftClient.getInstance().getItemRenderer().getModels()
                            .getModel(renderStack);
                    OVERRIDING_FOR.set(stack);
                    try {
                        ((ItemRendererAccessor) renderer).invokeRenderGuiItemModel(stack, x, y, realModel);
                    } finally {
                        OVERRIDING_FOR.remove();
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
