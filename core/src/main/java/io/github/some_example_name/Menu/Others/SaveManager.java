package io.github.some_example_name.Menu.Others;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Json;
import java.util.ArrayList;
import java.util.List;

public class SaveManager {
    private static final int MAX_SLOTS = 3;

    public static boolean loadBool(String slotId, String key, boolean defaultValue) {
        Preferences prefs = getSlot(slotId);
        return prefs.getBoolean(key, defaultValue);
    }

    public static boolean hasKey(String slotId, String key) {
        Preferences prefs = getSlot(slotId);
        return prefs.contains(key);
    }

    public static boolean hasSlot(String slotId) {
        Preferences prefs = Gdx.app.getPreferences("VN_SAVE_SLOT_" + slotId);
        return prefs.contains("scene") && prefs.contains("dialogueIndex");
    }

    public static int getMaxSlots() {
        return MAX_SLOTS;
    }

    public static int loadInt(String slotId, String key, int defaultValue) {
        Preferences prefs = getSlot(slotId);
        return prefs.getInteger(key, defaultValue);
    }

    public static long loadLong(String slotId, String key, long defaultValue) {
        Preferences prefs = getSlot(slotId);
        return prefs.getLong(key, defaultValue);
    }

    public static String loadString(String slotId, String key, String defaultValue) {
        Preferences prefs = getSlot(slotId);
        return prefs.getString(key, defaultValue);
    }

    public static String getString(String slotId, String key) {
        Preferences prefs = getSlot(slotId);
        return prefs.getString(key, null);
    }

    public static void save(String slotId, String key, String value) {
        if (value == null) {
            System.err.println("[WARN] Tried to save null value for key '" + key + "' in slot '" + slotId + "'. Skipping.");
            return;
        }
        Preferences prefs = getSlot(slotId);
        prefs.putString(key, value);
        prefs.flush();
    }


    public static void save(String slotId, String key, int value) {
        Preferences prefs = getSlot(slotId);
        prefs.putInteger(key, value);
        prefs.flush();
    }

    public static void save(String slotId, String key, boolean value) {
        Preferences prefs = getSlot(slotId);
        prefs.putBoolean(key, value);
        prefs.flush();
    }

    public static void save(String slotId, String key, long value) {
        Preferences prefs = getSlot(slotId);
        prefs.putLong(key, value);
        prefs.flush();
    }

    public static void save(String slotId, String key, List<String> list) {
        Json json = new Json();
        String serialized = json.toJson(list);
        Preferences prefs = getSlot(slotId);
        prefs.putString(key, serialized);
        prefs.flush();
    }

    public static List<String> loadList(String slotId, String key) {
        Preferences prefs = getSlot(slotId);
        String serialized = prefs.getString(key, null);
        if (serialized != null) {
            Json json = new Json();
            return json.fromJson(ArrayList.class, serialized);
        }
        return new ArrayList<>();
    }

    public static void deleteSave(String slotId) {
        Preferences prefs = getSlot(slotId);
        prefs.clear();
        prefs.flush();
    }

    public static void clearSlot(String slotId) {
        deleteSave(slotId);  // This will now resolve correctly
    }

    private static Preferences getSlot(String slotId) {
        return Gdx.app.getPreferences("VN_SAVE_SLOT_" + slotId);
    }
}

