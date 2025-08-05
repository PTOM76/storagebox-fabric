package net.pitan76.storagebox.mixin;

import net.minecraft.core.entity.EntityItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityItem.class)
public interface ItemEntityAccessor {
    @Accessor
    int getPickupDelay();

}
