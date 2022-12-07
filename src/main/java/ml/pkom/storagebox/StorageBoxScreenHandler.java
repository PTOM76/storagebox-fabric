package ml.pkom.storagebox;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

public class StorageBoxScreenHandler extends ScreenHandler {

    public static ScreenHandlerType<StorageBoxScreenHandler> SCREEN_HANDLER_TYPE;

    public static void init() {
        SCREEN_HANDLER_TYPE = ScreenHandlerRegistry.registerSimple(StorageBoxMod.id("storagebox"), StorageBoxScreenHandler::new);
    }

    private final Inventory inventory;

    public StorageBoxScreenHandler(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        this(syncId, playerInventory);
    }

    protected StorageBoxScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(SCREEN_HANDLER_TYPE, syncId);
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
        return inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.size()) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
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
