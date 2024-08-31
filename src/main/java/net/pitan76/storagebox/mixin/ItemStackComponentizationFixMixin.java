package net.pitan76.storagebox.mixin;

import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.fix.ItemStackComponentizationFix;
import net.pitan76.storagebox.StorageBoxMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStackComponentizationFix.class)
public class ItemStackComponentizationFixMixin {
    @Inject(method = "fixStack", at = @At("TAIL"))
    private static void storagebox$fixStack(ItemStackComponentizationFix.StackData data, Dynamic<?> dynamic, CallbackInfo ci) {
        if (data.itemEquals("storagebox:storagebox")) {
            data.moveToComponent("StorageSize", StorageBoxMod.id("size").toString());
            data.moveToComponent("StorageAuto", StorageBoxMod.id("auto").toString());
            data.moveToComponent("StorageItemData", StorageBoxMod.id("item_data").toString());
        }
    }
}
