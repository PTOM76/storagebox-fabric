package net.pitan76.storagebox;

import net.fabricmc.api.ModInitializer;
import net.legacyfabric.fabric.api.registry.v1.RegistryHelper;
import net.legacyfabric.fabric.api.util.Identifier;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StorageBoxMod implements ModInitializer
{
    // Forge: https://www.curseforge.com/minecraft/mc-mods/storagebox-mod
    public static String MOD_NAME = "StorageBox";
    public static String MOD_ID = "storagebox";

    @Override
    public void onInitialize() {

        RegistryHelper.registerItem(StorageBoxItem.instance, id("storagebox"));
        StorageBoxScreenHandler.init();
        StorageBoxServer.init();
        //StorageBoxRecipeSerializer.init();

        ModConfig.init();
    }
    private static final Logger LOGGER = LogManager.getLogger();

    public static void log(Level level, String message){
        LOGGER.log(level, "[" + MOD_NAME + "] " + message);
    }

    public static Identifier id(String id) {
        return new Identifier(MOD_ID, id);
    }

    public static net.minecraft.util.Identifier id_mc(String id) {
        return new net.minecraft.util.Identifier(MOD_ID, id);
    }
}
