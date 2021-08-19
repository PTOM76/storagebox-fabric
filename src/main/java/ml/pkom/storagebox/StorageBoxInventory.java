package ml.pkom.storagebox;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public class StorageBoxInventory extends SimpleInventory {

    public StorageBoxInventory() {
        super(1);
    }

    @Override
    public void onClose(PlayerEntity player) {

    }
}
