package net.pitan76.storagebox;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import static net.pitan76.storagebox.StorageBoxItem.*;

public class StorageBoxSlot extends Slot {

    private final StorageBoxScreenHandler handler;

    public StorageBoxSlot(StorageBoxScreenHandler handler, Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        this.handler = handler;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return StorageBoxItem.canInsertStack(stack);
    }

    @Override
    public void setStack(ItemStack itemStack) {
        super.setStack(itemStack);
        if (itemStack.isEmpty()) {
            ItemStack storageBoxStack = handler.getHandStack();
            removeItemDataAsInt(storageBoxStack, KEY_SIZE);
            removeItemDataAsInt(storageBoxStack, KEY_ITEM_DATA);
            removeItemDataAsInt(storageBoxStack, KEY_ITEM_ID);
            removeItemDataAsInt(storageBoxStack, KEY_AUTO);
            return;
        }
        ItemStack storageBoxStack = handler.getHandStack();
        setItemStack(storageBoxStack, itemStack.copy());
        setItemStackSize(storageBoxStack, itemStack.getCount());
    }

    @Override
    public ItemStack takeStack(int amount) {
        ItemStack storageBoxStack = handler.getHandStack();
        if (!(storageBoxStack.getItem() instanceof StorageBoxItem)) return super.takeStack(amount);
        if (amount == getStack().getCount()) {
            removeItemDataAsInt(storageBoxStack, KEY_SIZE);
            removeItemDataAsInt(storageBoxStack, KEY_ITEM_DATA);
            removeItemDataAsInt(storageBoxStack, KEY_ITEM_ID);
            removeItemDataAsInt(storageBoxStack, KEY_AUTO);
        } else {
            setItemDataAsInt(storageBoxStack, KEY_SIZE, getItemDataAsInt(storageBoxStack, KEY_SIZE) - amount);
        }
        return super.takeStack(amount);
    }
}
