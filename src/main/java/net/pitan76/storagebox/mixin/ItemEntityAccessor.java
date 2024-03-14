package net.pitan76.storagebox.mixin;

import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(ItemEntity.class)
public interface ItemEntityAccessor {
    @Accessor
    int getPickupDelay();

    @Accessor
    UUID getOwner();

}
