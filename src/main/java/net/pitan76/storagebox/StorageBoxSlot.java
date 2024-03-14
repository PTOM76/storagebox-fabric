package net.pitan76.storagebox;

import net.minecraft.container.Slot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class StorageBoxSlot extends Slot {

    private PlayerEntity player;

    public StorageBoxSlot(Inventory inventory, int index, int x, int y, PlayerEntity player) {
        super(inventory, index, x, y);
        this.player = player;

    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return StorageBoxItem.canInsertStack(stack);
    }

    @Override
    public void setStack(ItemStack itemStack) {
        super.setStack(itemStack);
        if (itemStack.isEmpty()) {
            ItemStack storageBoxStack = player.getMainHandStack();
            StorageBoxItem.removeItemDataAsInt(storageBoxStack, StorageBoxItem.KEY_SIZE);
            StorageBoxItem.removeItemDataAsInt(storageBoxStack, StorageBoxItem.KEY_ITEM_DATA);
            StorageBoxItem.removeItemDataAsInt(storageBoxStack, StorageBoxItem.KEY_ITEM_ID);
            StorageBoxItem.removeItemDataAsInt(storageBoxStack, StorageBoxItem.KEY_AUTO);
            return;
        }
        ItemStack storageBoxStack = player.getMainHandStack();
        StorageBoxItem.setItemStack(storageBoxStack, itemStack.copy());
        StorageBoxItem.setItemStackSize(storageBoxStack, itemStack.getCount());
    }

    @Override
    public ItemStack takeStack(int amount) {
        ItemStack storageBoxStack = player.getMainHandStack();
        if (!(storageBoxStack.getItem() instanceof StorageBoxItem)) return super.takeStack(amount);
        if (amount == getStack().getCount()) {
            StorageBoxItem.removeItemDataAsInt(storageBoxStack, StorageBoxItem.KEY_SIZE);
            StorageBoxItem.removeItemDataAsInt(storageBoxStack, StorageBoxItem.KEY_ITEM_DATA);
            StorageBoxItem.removeItemDataAsInt(storageBoxStack, StorageBoxItem.KEY_ITEM_ID);
            StorageBoxItem.removeItemDataAsInt(storageBoxStack, StorageBoxItem.KEY_AUTO);
        } else {
            StorageBoxItem.setItemDataAsInt(storageBoxStack, StorageBoxItem.KEY_SIZE, StorageBoxItem.getItemDataAsInt(storageBoxStack, StorageBoxItem.KEY_SIZE) - amount);
        }
        return super.takeStack(amount);
    }
}
