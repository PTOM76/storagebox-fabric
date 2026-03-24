package net.pitan76.storagebox;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StorageBoxMod implements ModInitializer
{
    // Forge: https://www.curseforge.com/minecraft/mc-mods/storagebox-mod
    public static String MOD_NAME = "StorageBox";
    public static String MOD_ID = "storagebox";

    public static ResourceKey<Item> STORAGE_BOX_KEY = ResourceKey.create(Registries.ITEM, id("storagebox"));

    @Override
    public void onInitialize() {
        DataComponentTypes.init();

        Registry.register(BuiltInRegistries.ITEM, STORAGE_BOX_KEY, StorageBoxItem.instance);
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
        return Identifier.fromNamespaceAndPath(MOD_ID, id);
    }

    public static <T> ResourceKey<T> key(ResourceKey<Registry<T>> registry, String id) {
        return ResourceKey.create(registry, id(id));
    }
}
