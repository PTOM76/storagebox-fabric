package net.pitan76.storagebox;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import static net.pitan76.storagebox.StorageBoxItem.*;

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
            removeComponent(storageBoxStack, DataComponentTypes.ITEM_COUNT);
            removeComponent(storageBoxStack, DataComponentTypes.ITEM_DATA);
            removeComponent(storageBoxStack, DataComponentTypes.AUTO_COLLECT);
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
            removeComponent(storageBoxStack, DataComponentTypes.ITEM_COUNT);
            removeComponent(storageBoxStack, DataComponentTypes.ITEM_DATA);
            removeComponent(storageBoxStack, DataComponentTypes.AUTO_COLLECT);
        } else {
            setComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT, getComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT) - amount);
        }
        return super.takeStack(amount);
    }
}
