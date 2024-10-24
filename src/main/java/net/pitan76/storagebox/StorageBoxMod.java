package net.pitan76.storagebox;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StorageBoxMod implements ModInitializer
{
    // Forge: https://www.curseforge.com/minecraft/mc-mods/storagebox-mod
    public static String MOD_NAME = "StorageBox";
    public static String MOD_ID = "storagebox";

    public static RegistryKey<Item> STORAGE_BOX_KEY = RegistryKey.of(RegistryKeys.ITEM, id("storagebox"));

    @Override
    public void onInitialize() {
        DataComponentTypes.init();

        Registry.register(Registries.ITEM, STORAGE_BOX_KEY, StorageBoxItem.instance);
        StorageBoxScreenHandler.init();
        StorageBoxServer.init();
        StorageBoxRecipeSerializer.init();

        ModConfig.init();
    }
    private static final Logger LOGGER = LogManager.getLogger();

    public static void log(Level level, String message){
        LOGGER.log(level, "[" + MOD_NAME + "] " + message);
    }

    public static Identifier id(String id) {
        return Identifier.of(MOD_ID, id);
    }

    public static <T> RegistryKey<T> key(RegistryKey<Registry<T>> registry, String id) {
        return RegistryKey.of(registry, id(id));
    }
}
