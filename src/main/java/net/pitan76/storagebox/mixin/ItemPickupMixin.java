package net.pitan76.storagebox.mixin;

import net.minecraft.block.Blocks;
import net.pitan76.storagebox.ModConfig;
import net.pitan76.storagebox.StorageBoxItem;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.pitan76.storagebox.StorageBoxUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ItemEntity.class)
public class ItemPickupMixin {

    @Unique
    private static boolean process(ItemStack stack, ItemStack pickupStack) {
        // ストレージボックス
        if (stack.getItem() instanceof StorageBoxItem) {
            if (!StorageBoxItem.isAutoCollect(stack)) return false;
            ItemStack stackInNbt = StorageBoxItem.getStackInStorageBox(stack);
            if (stackInNbt == null) return false;
            if (stackInNbt.getItem() == pickupStack.getItem()) {
                if (!StorageBoxItem.canInsertStack(pickupStack, stack)) return false;
                StorageBoxItem.setItemStackSize(stack, StorageBoxItem.getItemDataAsInt(stack, StorageBoxItem.KEY_SIZE) + pickupStack.count);
                return true;
            }
        }

        Boolean supportSimpleBackpack = ModConfig.getBoolean("SupportSimpleBackpack");
        if (supportSimpleBackpack == null) supportSimpleBackpack = true;
        // SimpleBackpackのサポート
        if (supportSimpleBackpack && Item.REGISTRY.getIdentifier(stack.getItem()).equals(new Identifier("simple_backpack", "backpack"))) {
            NbtCompound nbt = stack.getNbt();
            if (nbt.contains("backpack")) {
                NbtCompound backpackNbt = nbt.getCompound("backpack");
                List<ItemStack> items = new ArrayList<>(54);
                StorageBoxUtil.readNbt(backpackNbt, items);

                int i;
                for (i = 0; i < items.size(); i++) {
                    ItemStack inStack = items.get(i);
                    if (process(inStack, pickupStack)) {
                        // バックパック内のストレージボックスのNBTを更新
                        items.set(i, inStack);
                        StorageBoxUtil.writeNbt(backpackNbt, items);
                        nbt.put("backpack", backpackNbt);
                        stack.setNbt(nbt);
                        return true;
                    }
                }
            }
        }
        
        return false;
    }

    @Inject(method = "onPlayerCollision", at = @At(value = "HEAD"), cancellable = true)
    private void onPickup(PlayerEntity player, CallbackInfo ci) {
        ItemEntity itemEntity = (ItemEntity) (Object) this;

        Boolean supportEnderChest = ModConfig.getBoolean("SupportEnderChest");
        if (supportEnderChest == null) supportEnderChest = true;

        if (!itemEntity.world.isClient) {
            ItemStack itemStack = itemEntity.getItemStack();
            int count = itemStack.count;
            if (((ItemEntityAccessor)itemEntity).getPickupDelay() == 0 && (((ItemEntityAccessor)itemEntity).getOwner() == null || ((ItemEntityAccessor)itemEntity).getOwner().equals(player.getCustomName()))) {

                boolean insertedBox = false;
                boolean checkedEnderChest = false;
                // インベントリ
                for (ItemStack inStack : player.inventory.main) {
                    // エンダーチェストが含まれていたらエンダーチェストもループ処理
                    if (supportEnderChest && inStack.getItem() == BlockItem.fromBlock(Blocks.ENDERCHEST) && !checkedEnderChest) {
                        for (int i = 0; i < player.getEnderChestInventory().getInvSize(); i++) {
                            ItemStack enderChestStack = player.getEnderChestInventory().getInvStack(i);
                            if (enderChestStack.hasNbt()) {
                                if (process(enderChestStack, itemStack)) {
                                    insertedBox = true;
                                    itemStack = null;
                                    checkedEnderChest = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (inStack.hasNbt()) {
                        if (process(inStack, itemStack)) {
                            insertedBox = true;
                            itemStack = null;
                            break;
                        }
                    }
                }

                if (insertedBox) {
                    player.sendPickup(itemEntity, count);
                    if (itemStack == null || itemStack.count == 0) {
                        itemEntity.remove();
                    }

                    ci.cancel();
                }
            }
        }
    }
}