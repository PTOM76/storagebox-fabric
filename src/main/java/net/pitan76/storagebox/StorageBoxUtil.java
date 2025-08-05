package net.pitan76.storagebox;

import net.minecraft.core.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtList;
import org.apache.logging.log4j.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StorageBoxUtil {
    // Forge版Storage Boxより
    // Map<(old_id, old_damage), id>
    public static Map<OldItemId, String> convertIdMap = new HashMap<OldItemId, String>() {
        {
            put(new OldItemId("minecraft:stone", 1), "minecraft:granite");
            put(new OldItemId("minecraft:stone", 2), "minecraft:smooth_granite");
            put(new OldItemId("minecraft:stone", 3), "minecraft:diorite");
            put(new OldItemId("minecraft:stone", 4), "minecraft:smooth_diorite");
            put(new OldItemId("minecraft:stone", 5), "minecraft:andesite");
            put(new OldItemId("minecraft:stone", 6), "minecraft:smooth_andesite");
            put(new OldItemId("minecraft:log", 1), "minecraft:spruce_log");
            put(new OldItemId("minecraft:log", 2), "minecraft:birch_log");
            put(new OldItemId("minecraft:log", 3), "minecraft:jungle_log");
            put(new OldItemId("minecraft:log2", 0), "minecraft:acacia_log");
            put(new OldItemId("minecraft:log2", 1), "minecraft:dark_oak_log");
        }
    };

    /**
     * 1.12以前のIDシステムを1.13以降のIDシステムへ以降
     */
    public static String oldItemIDtoNewItemID(String oldId, int oldDamage) {
        for (Map.Entry<OldItemId, String> entry : convertIdMap.entrySet()) {
            if (entry.getKey().id.equals(oldId) && entry.getKey().damage == oldDamage) {
                StorageBoxMod.log(Level.INFO, "oldItemIDtoNewItemID:old id(" + oldId + ":" + oldDamage + ") is read as id(" + entry.getValue() + ")");
                return entry.getValue();
            }
        }
        return oldId;
    }

    public static class OldItemId {
        public String id;
        public int damage;

        public OldItemId(String id, int damage) {
            this.id = id;
            this.damage = damage;
        }
    }

    public static void readNbt(CompoundTag tag, List<ItemStack> items) {
        NbtList nbtList = tag.getList("Items", 10); // 10 = CompoundTag
        items.clear();

        int size = nbtList.size();
        for (int i = 0; i < size; ++i) {
            CompoundTag itemNbt = nbtList.getCompound(i);
            int slot = itemNbt.getByte("Slot") & 255;

            if (slot < items.size()) {
                items.set(slot, ItemStack.fromNbt(itemNbt));
            }
        }
    }

    public static void writeNbt(CompoundTag tag, List<ItemStack> items) {
        NbtList nbtList = new NbtList();
        for (int i = 0; i < items.size(); ++i) {
            ItemStack itemStack = items.get(i);
            if (itemStack != null && itemStack.stackSize != 0) {
                CompoundTag itemNbt = new CompoundTag();
                itemNbt.putByte("Slot", (byte) i);
                itemStack.toNbt(itemNbt);
                nbtList.add(itemNbt);
            }
        }
        tag.put("Items", nbtList);
    }
}
