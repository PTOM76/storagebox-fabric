package ml.pkom.storagebox;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
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
        if (stack.hasNbt()) return false;
        return true;
    }

    @Override
    public void setStack(ItemStack itemStack) {
        super.setStack(itemStack);
        if (itemStack.isEmpty()) {
            ItemStack handItem = player.getMainHandStack();
            NbtCompound tag = handItem.getNbt();
            if (tag == null) tag = new NbtCompound();
            tag.remove("countInBox");
            tag.remove("item");
            handItem.setNbt(tag);
            return;
        }
        ItemStack handItem = player.getMainHandStack();
        NbtCompound tag = handItem.getNbt();
        if (tag == null) tag = new NbtCompound();
        tag.putInt("countInBox", itemStack.getCount());
        tag.put("item", itemStack.writeNbt(new NbtCompound()));
        handItem.setNbt(tag);
    }
}
