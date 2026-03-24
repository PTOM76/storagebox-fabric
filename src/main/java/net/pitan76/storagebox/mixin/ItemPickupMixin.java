package net.pitan76.storagebox.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.pitan76.storagebox.DataComponentTypes;
import net.pitan76.storagebox.ModConfig;
import net.pitan76.storagebox.StorageBoxItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.pitan76.storagebox.StorageBoxItem.*;

@Mixin(ItemEntity.class)
public class ItemPickupMixin {
    @Unique
    private static boolean process(ItemStack stack, ItemStack pickupStack) {
        // ストレージボックス
        if (stack.getItem() instanceof StorageBoxItem) {
            if (!StorageBoxItem.isAutoCollect(stack)) return false;
            ItemStack stackInNbt = getStackInStorageBox(stack);
            if (stackInNbt == null) return false;
            if (stackInNbt.getItem() == pickupStack.getItem()) {
                if (!StorageBoxItem.canInsertStack(pickupStack, stack)) return false;
                if (getComponentAsInt(stack, DataComponentTypes.ITEM_COUNT) + pickupStack.getCount() < 0) return false; // 2147483647を超える場合は回収しない
                setItemStackSize(stack, getComponentAsInt(stack, DataComponentTypes.ITEM_COUNT) + pickupStack.getCount());
                return true;
            }
        }

        Boolean supportSimpleBackpack = ModConfig.getBoolean("SupportSimpleBackpack");
        if (supportSimpleBackpack == null) supportSimpleBackpack = true;
        /*
        // SimpleBackpackのサポート
        if (supportSimpleBackpack && Registries.ITEM.getId(stack.getItem()).equals(new Identifier("simple_backpack", "backpack"))) {
            NbtCompound nbt = stack.getNbt();
            if (nbt.contains("backpack")) {
                NbtCompound backpackNbt = nbt.getCompound("backpack");
                DefaultedList<ItemStack> items = DefaultedList.ofSize(54, ItemStack.EMPTY);
                Inventories.readNbt(backpackNbt, items);

                int i;
                for (i = 0; i < items.size(); i++) {
                    ItemStack inStack = items.get(i);
                    if (process(inStack, pickupStack)) {
                        // バックパック内のストレージボックスのNBTを更新
                        items.set(i, inStack);
                        Inventories.writeNbt(backpackNbt, items);
                        nbt.put("backpack", backpackNbt);
                        stack.setNbt(nbt);
                        return true;
                    }
                }
            }
        }
        */

        Boolean supportShulkerBox = ModConfig.getBoolean("SupportShulkerBox");
        if (supportShulkerBox == null) supportShulkerBox = true;
        // シュルカーボックスのサポート
        if (supportShulkerBox && stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() instanceof ShulkerBoxBlock) {
            DataComponentMap components = stack.getComponents();

            if (components.has(DataComponents.CONTAINER)) {
                ItemContainerContents component = components.get(DataComponents.CONTAINER);
                NonNullList<ItemStack> items = NonNullList.withSize(ShulkerBoxBlockEntity.CONTAINER_SIZE, ItemStack.EMPTY);
                component.copyInto(items);

                int i;
                for (i = 0; i < items.size(); i++) {
                    ItemStack inStack = items.get(i);
                    if (process(inStack, pickupStack)) {
                        // シュルカーボックス内のストレージボックスのNBTを更新
                        items.set(i, inStack);
                        stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Inject(method = "playerTouch", at = @At(value = "HEAD"), cancellable = true)
    private void onPickup(Player player, CallbackInfo ci) {
        ItemEntity itemEntity = (ItemEntity) (Object) this;

        Boolean supportEnderChest = ModConfig.getBoolean("SupportEnderChest");
        if (supportEnderChest == null) supportEnderChest = true;

        if (!itemEntity.level().isClientSide()) {
            ItemStack itemStack = itemEntity.getItem();
            Item item = itemStack.getItem();
            int count = itemStack.getCount();
            if (!itemEntity.hasPickUpDelay() && (itemEntity.getOwner() == null || itemEntity.getOwner().equals(player))) {

                boolean insertedBox = false;
                boolean checkedEnderChest = false;
                // インベントリ
                for (ItemStack inStack : player.getInventory().getNonEquipmentItems()) {

                    // エンダーチェストが含まれていたらエンダーチェストもループ処理
                    if (supportEnderChest && inStack.getItem() == Items.ENDER_CHEST && !checkedEnderChest) {
                        for (ItemStack enderChestStack : player.getEnderChestInventory().getItems()) {
                            if (!enderChestStack.getComponents().isEmpty()) {
                                if (process(enderChestStack, itemStack)) {
                                    insertedBox = true;
                                    itemStack = ItemStack.EMPTY;
                                    checkedEnderChest = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (!inStack.getComponents().isEmpty()) {
                        if (process(inStack, itemStack)) {
                            insertedBox = true;
                            itemStack = ItemStack.EMPTY;
                            break;
                        }
                    }
                }

                if (!insertedBox) {
                    // オフハンド
                    if (!player.getOffhandItem().getComponents().isEmpty()) {
                        if (process(player.getOffhandItem(), itemStack)) {
                            insertedBox = true;
                            itemStack = ItemStack.EMPTY;
                        }
                    }
                }

                if (insertedBox) {
                    player.take(itemEntity, count);
                    if (itemStack.isEmpty()) {
                        itemEntity.discard();
                        itemStack.setCount(count);
                    }

                    player.awardStat(Stats.ITEM_PICKED_UP.get(item), count);
                    player.onItemPickup(itemEntity);
                    ci.cancel();
                }
            }
        }
    }
}