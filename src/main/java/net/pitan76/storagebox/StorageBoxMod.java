package net.pitan76.storagebox;

import net.fabricmc.api.ModInitializer;
import net.legacyfabric.fabric.api.registry.v2.RegistryHelper;
import net.legacyfabric.fabric.api.registry.v2.RegistryIds;
import net.legacyfabric.fabric.impl.registry.RegistryHelperImplementation;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeDispatcher;
import net.minecraft.util.Identifier;
import net.pitan76.storagebox.mc.ExtendRegistryIds;
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

        RecipeDispatcher.method_14260(id("autocollectrecipes"), new AutoCollectRecipe(new ItemStack(StorageBoxItem.instance, 1, -1)));

        StorageBoxScreenHandler.init();
        StorageBoxServer.init();

        ModConfig.init();
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
