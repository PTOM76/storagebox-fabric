package net.pitan76.storagebox;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fabricmc.api.ModInitializer;
import net.legacyfabric.fabric.api.registry.v2.RegistryHelper;
import net.legacyfabric.fabric.api.registry.v2.RegistryIds;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeDispatcher;
import net.minecraft.recipe.ShapedRecipeType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class StorageBoxMod implements ModInitializer
{
    // Origin: https://www.curseforge.com/minecraft/mc-mods/storagebox-mod
    public static String MOD_NAME = "StorageBox";
    public static String MOD_ID = "storagebox";

    @Override
    public void onInitialize() {
        RegistryHelper.register(RegistryIds.ITEMS, lfid("storagebox"), StorageBoxItem.instance);

        RecipeDispatcher.method_14260(id("autocollectrecipes"), new AutoCollectRecipe(new ItemStack(StorageBoxItem.instance, 1, -1)));

        Gson gson = new Gson();
        JsonObject jsonObject;
        try {
            InputStream stream = getClass().getClassLoader().getResourceAsStream("assets/storagebox/recipes/storagebox.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));

            jsonObject = gson.fromJson(reader, JsonObject.class);
            reader.close();
        } catch (IOException | NullPointerException e) {
            jsonObject = gson.fromJson("{\n" +
                    "  \"type\": \"crafting_shaped\",\n" +
                    "  \"pattern\": [\n" +
                    "    \"XXX\",\n" +
                    "    \"X X\",\n" +
                    "    \"XXX\"\n" +
                    "  ],\n" +
                    "  \"key\": {\n" +
                    "    \"X\": {\n" +
                    "      \"item\": \"minecraft:chest\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"result\": {\n" +
                    "    \"item\": \"storagebox:storagebox\",\n" +
                    "    \"count\": 1\n" +
                    "  }\n" +
                    "}", JsonObject.class);
        }

        if (jsonObject != null)
            RecipeDispatcher.method_14260(id("storagebox"), ShapedRecipeType.load(jsonObject));

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
