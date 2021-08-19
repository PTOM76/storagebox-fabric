package ml.pkom.storagebox;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.slot.Slot;

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
            ItemStack handItem = player.getMainHandStack();
            CompoundTag tag = handItem.getTag();
            if (tag == null) tag = new CompoundTag();
            tag.remove("countInBox");
            tag.remove("item");
            handItem.setTag(tag);
            return;
        }
        ItemStack handItem = player.getMainHandStack();
        CompoundTag tag = handItem.getTag();
        if (tag == null) tag = new CompoundTag();
        tag.putInt("countInBox", itemStack.getCount());
        tag.put("item", itemStack.toTag(new CompoundTag()));
        handItem.setTag(tag);
    }
}
