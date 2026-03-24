package net.pitan76.storagebox;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import static net.pitan76.storagebox.StorageBoxItem.*;

public class StorageBoxSlot extends Slot {

//    private Player player;
    private final StorageBoxScreenHandler handler;

    public StorageBoxSlot(StorageBoxScreenHandler handler, Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);
//        this.player = player;
        this.handler = handler;

    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return StorageBoxItem.canInsertStack(stack);
    }

    @Override
    public void set(ItemStack itemStack) {
        super.set(itemStack);
        if (itemStack.isEmpty()) {
            ItemStack storageBoxStack = handler.getHandStack();
            removeComponent(storageBoxStack, DataComponentTypes.ITEM_COUNT);
            removeComponent(storageBoxStack, DataComponentTypes.ITEM_DATA);
            removeComponent(storageBoxStack, DataComponentTypes.AUTO_COLLECT);
            return;
        }
        ItemStack storageBoxStack = handler.getHandStack();
        setItemStack(storageBoxStack, itemStack.copy());
        setItemStackSize(storageBoxStack, itemStack.getCount());
    }

    @Override
    public ItemStack remove(int amount) {
        ItemStack storageBoxStack = handler.getHandStack();
        if (!(storageBoxStack.getItem() instanceof StorageBoxItem)) return super.remove(amount);
        if (amount == getItem().getCount()) {
            removeComponent(storageBoxStack, DataComponentTypes.ITEM_COUNT);
            removeComponent(storageBoxStack, DataComponentTypes.ITEM_DATA);
            removeComponent(storageBoxStack, DataComponentTypes.AUTO_COLLECT);
        } else {
            setComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT, getComponentAsInt(storageBoxStack, DataComponentTypes.ITEM_COUNT) - amount);
        }
        return super.remove(amount);
    }
}
