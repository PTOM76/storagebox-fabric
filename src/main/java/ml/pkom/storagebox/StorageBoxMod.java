package ml.pkom.storagebox;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StorageBoxMod implements ModInitializer
{
    public static String MOD_NAME = "StorageBox";
    public static String MOD_ID = "storagebox";

    @Override
    public void onInitialize() {
        Registry.register(Registry.ITEM, id("storagebox"), StorageBoxItem.instance);
        log(Level.INFO, "init");
        StorageBoxServer.init();

    }

    private static Logger LOGGER = LogManager.getLogger();

    public static void log(Level level, String message){
        LOGGER.log(level, "[" + MOD_NAME + "] " + message);
    }

    public static Identifier id(String id) {
        return new Identifier(MOD_ID, id);
    }
}
