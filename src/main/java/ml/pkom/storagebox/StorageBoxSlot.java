package ml.pkom.storagebox;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class StorageBoxSlot extends Slot {

    private ItemStack exeStack;

    public StorageBoxSlot(Inventory inventory, int index, int x, int y, ItemStack stack) {
        super(inventory, index, x, y);
        this.exeStack = stack;

    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return canInsertStack(stack);
    }

    public static boolean canInsertStack(ItemStack stack) {
        if (stack.getItem() instanceof StorageBoxItem) return false;
        if (stack.isEnchantable()) return false;
        if (stack.isDamageable()) return false;
        if (StorageBoxItem.hasNBT(stack)) return false;
        return true;
    }

    @Override
    public void setStack(ItemStack itemStack) {
        super.setStack(itemStack);
        if (itemStack.isEmpty()) {
            ItemStack handItem = exeStack;
            if (StorageBoxItem.hasNBT(handItem)) {
                StorageBoxItem.getNBT(handItem).remove("countInBox");
                StorageBoxItem.getNBT(handItem).remove("item");
                StorageBoxItem.setNBT(handItem);
            }
            return;
        }
        ItemStack handItem = exeStack;
        StorageBoxItem.getNBT(handItem).putInt("countInBox", itemStack.getCount());
        StorageBoxItem.getNBT(handItem).put("item", itemStack.toTag(StorageBoxItem.newNBT()));
        StorageBoxItem.setNBT(handItem);
    }
}
