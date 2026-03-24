package net.pitan76.storagebox.mixin;

import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.ItemStackComponentizationFix;
import net.pitan76.storagebox.StorageBoxMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStackComponentizationFix.class)
public class ItemStackComponentizationFixMixin {
    @Inject(method = "fixItemStack", at = @At("TAIL"))
    private static void storagebox$fixStack(ItemStackComponentizationFix.ItemStackData data, Dynamic<?> dynamic, CallbackInfo ci) {
        if (data.is("storagebox:storagebox")) {
            data.moveTagToComponent("StorageSize", StorageBoxMod.id("size").toString());
            data.moveTagToComponent("StorageAuto", StorageBoxMod.id("auto").toString());
            data.moveTagToComponent("StorageItemData", StorageBoxMod.id("item_data").toString());
        }
    }
}
