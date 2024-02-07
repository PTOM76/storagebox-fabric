package ml.pkom.storagebox;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import static ml.pkom.storagebox.StorageBoxItem.*;

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
            removeItemDataAsInt(storageBoxStack, KEY_SIZE);
            removeItemDataAsInt(storageBoxStack, KEY_ITEM_DATA);
            removeItemDataAsInt(storageBoxStack, KEY_ITEM_ID);
            removeItemDataAsInt(storageBoxStack, KEY_AUTO);
            return;
        }
        ItemStack storageBoxStack = player.getMainHandStack();
        setItemStack(storageBoxStack, itemStack.copy());
        setItemStackSize(storageBoxStack, itemStack.getCount());
    }

    @Override
    public ItemStack takeStack(int amount) {
        ItemStack storageBoxStack = player.getMainHandStack();
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
