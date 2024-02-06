package ml.pkom.storagebox;

import net.fabricmc.fabric.api.container.ContainerProviderRegistry;
import net.minecraft.container.Container;
import net.minecraft.container.Slot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class StorageBoxScreenHandler extends Container {

    public static void init() {
        ContainerProviderRegistry.INSTANCE.registerFactory(StorageBoxMod.id("storagebox"), ((syncId1, identifier, player, buf) -> new StorageBoxScreenHandler(syncId1, player.inventory, player)));
    }

    private final Inventory inventory;

    public StorageBoxScreenHandler(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        this(syncId, playerInventory);
    }

    public StorageBoxScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(null, syncId);
        inventory = new StorageBoxInventory();
        int m, l;

        addSlot(new StorageBoxSlot(inventory, 0, 12, 35, playerInventory.player));
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 9; ++l) {
                addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
            }
        }
        for (m = 0; m < 9; ++m) {
            addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUseInv(player);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.getInvSize()) {
                if (!this.insertItem(originalStack, this.inventory.getInvSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, this.inventory.getInvSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return StorageBoxSlot.canInsertStack(newStack) ? newStack : ItemStack.EMPTY;
    }

}
