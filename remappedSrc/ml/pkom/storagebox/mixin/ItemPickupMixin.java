package ml.pkom.storagebox.mixin;

import ml.pkom.storagebox.StorageBoxItem;
import ml.pkom.storagebox.StorageBoxSlot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.stat.Stats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemPickupMixin {
    @Inject(method = "onPlayerCollision", at = @At(value = "HEAD"), cancellable = true)
    private void onPickup(PlayerEntity player, CallbackInfo ci) {
        ItemEntity itemEntity = (ItemEntity) (Object) this;
        if (!itemEntity.world.isClient) {
            ItemStack itemStack = itemEntity.getStack();
            Item item = itemStack.getItem();
            int count = itemStack.getCount();
            if (((ItemEntityAccessor)itemEntity).getPickupDelay() == 0 && (((ItemEntityAccessor)itemEntity).getOwner() == null || ((ItemEntityAccessor)itemEntity).getOwner().equals(player.getUuid()))) {
                boolean insertedBox = false;
                int maxSize = player.getInventory().main.size() - 1;
                for (int i = 0; i <= maxSize; i++) {
                    ItemStack itemStack2 = player.getInventory().getStack(i);
                    if (itemStack2.getItem() instanceof StorageBoxItem) if (itemStack2.hasNbt()) {
                        if (!StorageBoxItem.isAutoCollect(itemStack2)) continue;
                        NbtCompound tag = itemStack2.getNbt();
                        ItemStack stackInTag = ItemStack.fromNbt(tag.getCompound("item"));
                        if (stackInTag.getItem() == itemStack.getItem()) {
                            if (!StorageBoxSlot.canInsertStack(itemStack)) continue;
                            tag.putInt("countInBox", tag.getInt("countInBox") + itemStack.getCount());
                            itemStack2.setNbt(tag);
                            insertedBox = true;
                            itemStack = ItemStack.EMPTY;
                            break;
                        }
                    }
                }
                if (!insertedBox) {
                    ItemStack itemStack2 = player.getOffHandStack();
                    if (itemStack2.getItem() instanceof StorageBoxItem) if (itemStack2.hasNbt()) {
                        if (StorageBoxItem.isAutoCollect(itemStack2)) {
                            NbtCompound tag = itemStack2.getNbt();
                            ItemStack stackInTag = ItemStack.fromNbt(tag.getCompound("item"));
                            if (stackInTag.getItem() == itemStack.getItem()) {
                                if (StorageBoxSlot.canInsertStack(itemStack)) {
                                    tag.putInt("countInBox", tag.getInt("countInBox") + itemStack.getCount());
                                    itemStack2.setNbt(tag);
                                    insertedBox = true;
                                    itemStack = ItemStack.EMPTY;
                                }
                            }
                        }
                    }
                }
                if (insertedBox) {
                    player.sendPickup(itemEntity, count);
                    if (itemStack.isEmpty()) {
                        itemEntity.discard();
                        itemStack.setCount(count);
                    }

                    player.increaseStat(Stats.PICKED_UP.getOrCreateStat(item), count);
                    player.triggerItemPickedUpByEntityCriteria(itemEntity);
                    ci.cancel();
                }
            }

        }
    }
}