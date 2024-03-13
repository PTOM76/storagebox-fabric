package net.pitan76.storagebox.api;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.pitan76.storagebox.StorageBoxItem;

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
    public static void setAmountInStorageBox(ItemStack storageBoxStack, int amount) {
        StorageBoxItem.setItemDataAsInt(storageBoxStack, StorageBoxItem.KEY_SIZE, amount);
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
}
