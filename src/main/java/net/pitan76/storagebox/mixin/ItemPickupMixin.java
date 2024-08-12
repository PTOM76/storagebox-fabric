package net.pitan76.storagebox.mixin;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.pitan76.storagebox.ModConfig;
import net.pitan76.storagebox.StorageBoxItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
                StorageBoxItem.setItemStackSize(stack, StorageBoxItem.getItemDataAsInt(stack, StorageBoxItem.KEY_SIZE) + pickupStack.getCount());
                return true;
            }
        }

        /*
        Boolean supportSimpleBackpack = ModConfig.getBoolean("SupportSimpleBackpack");
        if (supportSimpleBackpack == null) supportSimpleBackpack = true;
        // SimpleBackpackのサポート
        if (supportSimpleBackpack && Registry.ITEM.getId(stack.getItem()).equals(new Identifier("simple_backpack", "backpack"))) {
            NbtCompound nbt = stack.getNbt();
            if (nbt.contains("backpack")) {
                NbtCompound backpackNbt = nbt.getCompound("backpack");
                DefaultedList<ItemStack> items = DefaultedList.ofSize(54, ItemStack.EMPTY);
                Inventories.fromTag(backpackNbt, items);

                int i;
                for (i = 0; i < items.size(); i++) {
                    ItemStack inStack = items.get(i);
                    if (process(inStack, pickupStack)) {
                        // バックパック内のストレージボックスのNBTを更新
                        items.set(i, inStack);
                        Inventories.toTag(backpackNbt, items);
                        nbt.put("backpack", backpackNbt);
                        stack.setNbt(nbt);
                        return true;
                    }
                }
            }
        }

         */

        /*
        Boolean supportShulkerBox = ModConfig.getBoolean("SupportShulkerBox");
        if (supportShulkerBox == null) supportShulkerBox = true;
        // シュルカーボックスのサポート
        if (supportShulkerBox && stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() instanceof ShulkerBoxBlock) {
            NbtCompound nbt = stack.getNbt();
            if (nbt.contains("BlockEntityTag")) {
                NbtCompound tileNbt = nbt.getCompound("BlockEntityTag");
                DefaultedList<ItemStack> items = DefaultedList.ofSize(27, ItemStack.EMPTY);
                Inventories.fromTag(tileNbt, items);

                int i;
                for (i = 0; i < items.size(); i++) {
                    ItemStack inStack = items.get(i);
                    if (process(inStack, pickupStack)) {
                        // シュルカーボックス内のストレージボックスのNBTを更新
                        items.set(i, inStack);
                        Inventories.toTag(tileNbt, items);
                        nbt.put("BlockEntityTag", tileNbt);
                        stack.setTag(nbt);
                        return true;
                    }
                }
            }
        }

         */

        return false;
    }

    @Inject(method = "onPlayerCollision", at = @At(value = "HEAD"), cancellable = true)
    private void onPickup(PlayerEntity player, CallbackInfo ci) {
        ItemEntity itemEntity = (ItemEntity) (Object) this;

        Boolean supportEnderChest = ModConfig.getBoolean("SupportEnderChest");
        if (supportEnderChest == null) supportEnderChest = true;

        if (!itemEntity.world.isClient) {
            ItemStack itemStack = itemEntity.getItemStack();
            Item item = itemStack.getItem();
            int count = itemStack.getCount();
            if (((ItemEntityAccessor)itemEntity).getPickupDelay() == 0 && (((ItemEntityAccessor)itemEntity).getOwner() == null || ((ItemEntityAccessor)itemEntity).getOwner().equals(player.getUuid()))) {

                boolean insertedBox = false;
                boolean checkedEnderChest = false;
                // インベントリ
                for (ItemStack inStack : player.inventory.field_15082) {
                    // エンダーチェストが含まれていたらエンダーチェストもループ処理
                    /*
                    if (supportEnderChest && inStack.getItem() == Items.ENDER_CHEST && !checkedEnderChest) {
                        for (int i = 0; i < player.getEnderChestInventory().getInvSize(); i++) {
                            ItemStack enderChestStack = player.getEnderChestInventory().getInvStack(i);
                            if (enderChestStack.hasTag()) {
                                if (process(enderChestStack, itemStack)) {
                                    insertedBox = true;
                                    itemStack = ItemStack.EMPTY;
                                    checkedEnderChest = true;
                                    break;
                                }
                            }
                        }
                    }

                     */
                    if (inStack.hasNbt()) {
                        if (process(inStack, itemStack)) {
                            insertedBox = true;
                            itemStack = ItemStack.EMPTY;
                            break;
                        }
                    }
                }

                if (!insertedBox) {
                    // オフハンド
                    if (player.getOffHandStack().hasNbt()) {
                        if (process(player.getOffHandStack(), itemStack)) {
                            insertedBox = true;
                            itemStack = ItemStack.EMPTY;
                        }
                    }
                }

                if (insertedBox) {
                    player.sendPickup(itemEntity, count);
                    if (itemStack.isEmpty()) {
                        itemEntity.remove();
                        itemStack.setCount(count);
                    }

                    player.incrementStat(Stats.picked(item));
                    ci.cancel();
                }
            }
        }
    }
}