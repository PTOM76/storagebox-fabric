package ml.pkom.storagebox;

import net.fabricmc.api.ModInitializer;
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
        Registry.register(Registry.ITEM, id("storagebox"), StorageBoxItem.instance);
        StorageBoxServer.init();

        ModConfig.init();
    }
    private static Logger LOGGER = LogManager.getLogger();

    public static void log(Level level, String message){
        LOGGER.log(level, "[" + MOD_NAME + "] " + message);
    }

    public static Identifier id(String id) {
        return new Identifier(MOD_ID, id);
    }
}
