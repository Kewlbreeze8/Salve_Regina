package io.github.Kewlbreeze8.Ingame.Others;

import java.util.HashMap;
import java.util.Map;

public class Stats {
    private static final Map<String, Integer> values = new HashMap<>();

    public static void set(String key, int value) {
        values.put(key, value);
    }

    public static int get(String key) {
        return values.getOrDefault(key, 0);
    }

    public static void add(String key, int amount) {
        values.put(key, get(key) + amount);
    }

    public static void reset() {
        values.clear();
    }

    public static Map<String, Integer> getAll() {
        return new HashMap<>(values);
    }

    public static void update(String key, int delta) {
        int current = get(key);
        set(key, current + delta);
        System.out.println("[DEBUG] Stat '" + key + "' updated to: " + get(key));
    }
}
