package ml.pkom.storagebox;

import org.apache.logging.log4j.Level;

import java.util.HashMap;
import java.util.Map;

public class StorageBoxUtil {
    // Forge版Storage Boxより
    // Map<(old_id, old_damage), id>
    public static Map<OldItemId, String> convertIdMap = new HashMap<>() {
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
}
