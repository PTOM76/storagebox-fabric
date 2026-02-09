package net.pitan76.storagebox.api;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.pitan76.storagebox.StorageBoxItem;

import static net.pitan76.storagebox.StorageBoxItem.removeItemDataAsInt;

public class StorageBoxUtil {
    /**
     * Get the item stack in the storage box
     * @param storageBoxStack The storage box stack
     * @return The item stack
     */
    public static ItemStack getStackInStorageBox(ItemStack storageBoxStack) {
        return StorageBoxItem.getStackInStorageBox(storageBoxStack);
    }

    /**
     * Check if the storage box has an item stack
     * @param storageBoxStack The storage box stack
     * @return Whether the storage box has an item stack
     */
    public static boolean hasStackInStorageBox(ItemStack storageBoxStack) {
        return StorageBoxItem.hasStackInStorageBox(storageBoxStack);
    }

    /**
     * Set the item stack in the storage box
     * @param storageBoxStack The storage box stack
     * @param itemStack The item stack to set
     */
    public static void setStackInStorageBox(ItemStack storageBoxStack, ItemStack itemStack) {
        StorageBoxItem.setItemStack(storageBoxStack, itemStack);
    }

    /**
     * Get the amount of items in the storage box
     * @param storageBoxStack The storage box stack
     * @return The amount of items
     */
    public static int getAmountInStorageBox(ItemStack storageBoxStack) {
        return StorageBoxItem.getItemDataAsInt(storageBoxStack, StorageBoxItem.KEY_SIZE);
    }

    /**
     * Set the amount of items in the storage box
     * @param storageBoxStack The storage box stack
     * @param amount The amount of items
     */
    public static void setAmountOnlyInStorageBox(ItemStack storageBoxStack, int amount) {
        StorageBoxItem.setItemDataAsInt(storageBoxStack, StorageBoxItem.KEY_SIZE, amount);
    }

    /**
     * Set the amount of items in the storage box with clearing data if amount is zero or less
     * @param storageBoxStack The storage box stack
     * @param amount The amount of items
     */
    public static void setAmountInStorageBox(ItemStack storageBoxStack, int amount) {
        if (amount <= 0) {
            clearItemData(storageBoxStack);
            return;
        }

        setAmountOnlyInStorageBox(storageBoxStack, amount);
    }

    /**
     * Get the auto collect status of the storage box
     * @param storageBoxStack The storage box stack
     * @return The auto collect status
     */
    public static boolean isAutoCollect(ItemStack storageBoxStack) {
        return StorageBoxItem.isAutoCollect(storageBoxStack);
    }

    /**
     * Set the auto collect status of the storage box
     * @param storageBoxStack The storage box stack
     */
    public static void changeAutoCollect(ItemStack storageBoxStack) {
        StorageBoxItem.changeAutoCollect(storageBoxStack);
    }

    /**
     * Handle keyboard events
     * @param event The event
     * @param player The player
     * @param itemStack The item stack
     */
    public static void keyboardEvent(int event, ServerPlayerEntity player, ItemStack itemStack) {
        StorageBoxItem.keyboardEvent(event, player, itemStack);
    }

    /**
     * is insertable
     * @param stack The stack
     * @return Whether the stack is insertable
     */
    public static boolean canInsertStack(ItemStack stack) {
        return StorageBoxItem.canInsertStack(stack);
    }

    /**
     * Remove item data from the storage box
     * @param storageBoxStack The storage box stack
     */
    public static void clearItemData(ItemStack storageBoxStack) {
        removeItemDataAsInt(storageBoxStack, StorageBoxItem.KEY_SIZE);
        removeItemDataAsInt(storageBoxStack, StorageBoxItem.KEY_ITEM_DATA);
        removeItemDataAsInt(storageBoxStack, StorageBoxItem.KEY_ITEM_ID);
        removeItemDataAsInt(storageBoxStack, StorageBoxItem.KEY_AUTO);
    }
}
