package net.pitan76.storagebox;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class StorageBoxScreenHandler extends AbstractContainerMenu {

    public static MenuType<StorageBoxScreenHandler> SCREEN_HANDLER_TYPE;

    public static void init() {
        SCREEN_HANDLER_TYPE = Registry.register(BuiltInRegistries.MENU, StorageBoxMod.id("storagebox"), new MenuType<>(StorageBoxScreenHandler::new, FeatureFlagSet.of()));
    }

    private final Container inventory;
    public final ItemStack handStack;

    public StorageBoxScreenHandler(int syncId, Inventory playerInventory, Player player) {
        this(syncId, playerInventory);
    }

    public StorageBoxScreenHandler(int syncId, Inventory playerInventory) {
        super(SCREEN_HANDLER_TYPE, syncId);
        inventory = new StorageBoxInventory();
        handStack = playerInventory.player.getActiveItem();
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
    public void clicked(int slotIndex, int button, ContainerInput actionType, Player player) {
        Inventory playerInventory = player.getInventory();
        int playerSlotIndex = slotIndex-1;
        // System.out.println(slotIndex-1);
        if ((playerSlotIndex >= 0 && playerSlotIndex < 36 || playerSlotIndex == 40) && playerInventory.getItem(playerSlotIndex) == handStack) {
            return;
        }
        super.clicked(slotIndex, button, actionType, player);
    }
    public ItemStack getHandStack() {
        return this.handStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return inventory.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.getContainerSize()) {
                if (!this.moveItemStackTo(originalStack, this.inventory.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(originalStack, 0, this.inventory.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return StorageBoxItem.canInsertStack(newStack) ? newStack : ItemStack.EMPTY;
    }
}
