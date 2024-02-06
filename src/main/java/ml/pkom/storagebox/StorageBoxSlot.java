package ml.pkom.storagebox;

import net.minecraft.container.Slot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

import static ml.pkom.storagebox.StorageBoxItem.*;

public class StorageBoxSlot extends Slot {

    private PlayerEntity player;

    public StorageBoxSlot(Inventory inventory, int index, int x, int y, PlayerEntity player) {
        super(inventory, index, x, y);
        this.player = player;

    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return canInsertStack(stack);
    }

    public static boolean canInsertStack(ItemStack stack) {
        if (stack.getItem() == StorageBoxItem.instance) return false;
        if (stack.isEnchantable()) return false;
        if (stack.isDamageable()) return false;
        if (stack.hasTag()) return false;
        return true;
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
}
