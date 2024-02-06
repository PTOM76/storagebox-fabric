package ml.pkom.storagebox;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

public class ModConfig {
    public static File dir = FabricLoader.getInstance().getConfigDir().toFile();
    public static File file = new File(dir, "storagebox.json");

    private static Map<String, Object> configMap = new LinkedHashMap<>();

    public static void init() {
        setDefault();
        if (!load())
            save();

    }

    public static boolean save() {
        if (!dir.exists())
            dir.mkdirs();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(configMap);
        return fileWriteContents(file, json);
    }

    public static boolean load() {
        if (!dir.exists())
            dir.mkdirs();

        if (file.exists()) {
            String json = fileReadContents(file);
            Gson gson = new Gson();
            Type jsonMap = new TypeToken<LinkedHashMap<String, Object>>() {
            }.getType();
            configMap = gson.fromJson(json, jsonMap);
            return true;
        }

        return false;
    }

    public static void setDefault() {
        setBoolean("DefaultAutoCollect", true);
        setBoolean("SupportEnderChest", true);
        setBoolean("SupportShulkerBox", true);
        setBoolean("SupportSimpleBackpack", true);
    }

    public static Object get(String key) {
        return configMap.get(key);
    }

    public static void setBoolean(String key, boolean value) {
        configMap.put(key, value);
    }

    public static void setInt(String key, int value) {
        configMap.put(key, value);
    }

    public static void setString(String key, String value) {
        configMap.put(key, value);
    }

    public static Boolean getBoolean(String key) {
        Object value = get(key);
        if (value instanceof Integer) {
            Integer i = (Integer) value;
            return i != 0;
        }
        if (value instanceof Boolean)
            return (Boolean) value;
        if (value instanceof String)
            return Boolean.getBoolean((String) value);

        return null;
    }

    public static Integer getInt(String key) {
        Object value = get(key);
        if (value instanceof Integer)
            return (Integer) value;
        if (value instanceof String)
            return Integer.getInteger((String) value);

        return null;
    }

    public static String getString(String key) {
        Object value = get(key);
        if (value instanceof String)
            return (String) value;

        return value.toString();
    }

    public static Map<String, Object> getConfigMap() {
        return configMap;
    }

    /**
     * ファイルにデータを書き込みます。
     * 失敗した場合falseを返します。
     *
     * @param file   ファイル
     * @param contents データ
     */
    public static boolean fileWriteContents(File file, String contents) {
        try {
            PrintWriter writer = new PrintWriter(
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")));
            writer.println(contents);
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ファイルからデータを読み込みます。
     * 失敗した場合nullを返します。
     *
     * @param file   ファイル
     * @return ファイルのデータ or null
     */
    public static String fileReadContents(File file) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String line;
            StringBuilder contents = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                contents.append(line).append("\n");
            }
            reader.close();
            return contents.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
