package net.pitan76.storagebox.mixin;

import net.minecraft.core.entity.EntityItem;
import net.minecraft.core.entity.player.Player;
import net.pitan76.storagebox.ModConfig;
import net.pitan76.storagebox.StorageBoxItem;
import net.minecraft.core.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityItem.class)
public class ItemPickupMixin {

    @Unique
    private static boolean process(ItemStack stack, ItemStack pickupStack) {
        // ストレージボックス
        if (stack.getItem() instanceof StorageBoxItem) {
            if (!StorageBoxItem.isAutoCollect(stack)) return false;
            ItemStack stackInNbt = StorageBoxItem.getStackInStorageBox(stack);
            if (stackInNbt == null) return false;
            if (stackInNbt.getItem().equals(pickupStack.getItem())) {
                if (!StorageBoxItem.canInsertStack(pickupStack, stack)) return false;
                StorageBoxItem.setItemStackSize(stack, StorageBoxItem.getItemDataAsInt(stack, StorageBoxItem.KEY_SIZE) + pickupStack.stackSize);
                return true;
            }
        }
        
        return false;
    }

    @Inject(method = "playerTouch", at = @At(value = "HEAD"), cancellable = true)
    private void onPickup(Player player, CallbackInfo ci) {
        EntityItem itemEntity = (EntityItem) (Object) this;

        Boolean supportEnderChest = ModConfig.getBoolean("SupportEnderChest");
        if (supportEnderChest == null) supportEnderChest = true;

        if (!itemEntity.world.isClientSide) {
            ItemStack itemStack = itemEntity.item;
            int count = itemStack.stackSize;
            if (((ItemEntityAccessor)itemEntity).getPickupDelay() == 0) {

                boolean insertedBox = false;
                // インベントリ
                for (ItemStack inStack : player.inventory.mainInventory) {
                    if (inStack == null) continue;
                    if (process(inStack, itemStack)) {
                        insertedBox = true;
                        itemStack = null;
                        break;
                    }
                }

                if (insertedBox) {
                    if (itemStack == null || itemStack.stackSize == 0) {
                        itemEntity.remove();
                    }

                    ci.cancel();
                }
            }
        }
    }
}