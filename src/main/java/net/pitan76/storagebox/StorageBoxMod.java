package net.pitan76.storagebox;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
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
        Item.REGISTRY.add(getNextAvailableItemId(), id("storagebox"), StorageBoxItem.instance);
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
