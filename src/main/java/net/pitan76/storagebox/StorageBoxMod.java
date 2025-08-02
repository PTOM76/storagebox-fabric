package net.pitan76.storagebox;

import net.fabricmc.api.ModInitializer;
import net.legacyfabric.fabric.api.registry.v2.RegistryHelper;
import net.legacyfabric.fabric.api.registry.v2.RegistryIds;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StorageBoxMod implements ModInitializer
{
    // Origin: https://www.curseforge.com/minecraft/mc-mods/storagebox-mod
    public static String MOD_NAME = "StorageBox";
    public static String MOD_ID = "storagebox";

    @Override
    public void onInitialize() {
        RegistryHelper.register(RegistryIds.ITEMS, lfid("storagebox"), StorageBoxItem.instance);
        StorageBoxScreenHandler.init();
        StorageBoxServer.init();
        StorageBoxRecipeSerializer.init();

        ModConfig.init();
    }

    private int getNextAvailableItemId() {
        int id = 0;
        while (Item.REGISTRY.getByRawId(id) != null && id < 32000) {
            id++;
        }
        return id;
    }
    private static final Logger LOGGER = LogManager.getLogger();

    public static void log(Level level, String message){
        LOGGER.log(level, "[" + MOD_NAME + "] " + message);
    }

    public static Identifier id(String id) {
        return new Identifier(MOD_ID, id);
    }

    public static net.legacyfabric.fabric.api.util.Identifier lfid(String id) {
        return new net.legacyfabric.fabric.api.util.Identifier(MOD_ID, id);
    }
}
