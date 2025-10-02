package net.pitan76.storagebox;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class StorageBoxScreenHandler extends ScreenHandler {

    public static ScreenHandlerType<StorageBoxScreenHandler> SCREEN_HANDLER_TYPE;

    public static void init() {
        SCREEN_HANDLER_TYPE = Registry.register(Registries.SCREEN_HANDLER,  StorageBoxMod.id("storagebox"), new ScreenHandlerType<>(StorageBoxScreenHandler::new, FeatureSet.empty()));
    }

    private final Inventory inventory;
    public final ItemStack handStack;

    public StorageBoxScreenHandler(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        this(syncId, playerInventory);
    }

    public StorageBoxScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(SCREEN_HANDLER_TYPE, syncId);
        inventory = new StorageBoxInventory();
        handStack = playerInventory.getMainHandStack();
        int m, l;

        addSlot(new StorageBoxSlot(this, inventory, 0, 12, 35));
        for (m = 0; m < 9; ++m) {
            addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
        }
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 9; ++l) {
                addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
            }
        }
    }
    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        PlayerInventory playerInventory = player.getInventory();
        int playerSlotIndex = slotIndex-1;
        if ((playerSlotIndex >= 0 && playerSlotIndex < 36 || playerSlotIndex == 40) && playerInventory.getStack(playerSlotIndex) == handStack) {
            return;
        }
        super.onSlotClick(slotIndex, button, actionType, player);
    }
    public ItemStack getHandStack() {
        return this.handStack;
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

        return StorageBoxItem.canInsertStack(newStack) ? newStack : ItemStack.EMPTY;
    }

}
