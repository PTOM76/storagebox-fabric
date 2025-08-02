package net.pitan76.storagebox;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.slot.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class StorageBoxScreenHandler extends ScreenHandler {

    public static NamedScreenHandlerFactory FACTORY = new NamedScreenHandlerFactory() {
        @Override
        public ScreenHandler createScreenHandler(PlayerInventory inventory, PlayerEntity player) {
            return new StorageBoxScreenHandler(inventory, player);
        }

        @Override
        public String getId() {
            return StorageBoxMod.id("container").toString();
        }

        @Override
        public String getTranslationKey() {
            return "item.storagebox.storage";
        }

        @Override
        public boolean hasCustomName() {
            return false;
        }

        @Override
        public Text getName() {
            return new LiteralText("");
        }
    };

    public static void init() {
        //ContainerProviderRegistry.INSTANCE.registerFactory(StorageBoxMod.id("container"), FACTORY);
    }

    private final Inventory inventory;

    public StorageBoxScreenHandler(PlayerInventory playerInventory, PlayerEntity player) {
        this(playerInventory);
    }

    public StorageBoxScreenHandler(PlayerInventory playerInventory) {
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

        return StorageBoxItem.canInsertStack(newStack) ? newStack : ItemStack.EMPTY;
    }

}
