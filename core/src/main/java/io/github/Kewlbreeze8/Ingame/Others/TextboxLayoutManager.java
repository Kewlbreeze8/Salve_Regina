package io.github.kewlbreeze8.Ingame.Others;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;

public class TextboxLayoutManager {
    private static final ObjectMap<String, TextboxLayout> layoutMap = new ObjectMap<>();

    public static void load() {
        try {
            FileHandle file = Gdx.files.internal("ingame/textbox/TextboxPosition.json");
            Json json = new Json();
            ObjectMap<String, TextboxLayout> loaded = json.fromJson(ObjectMap.class, TextboxLayout.class, file);
            layoutMap.clear();
            layoutMap.putAll(loaded);
            Gdx.app.log("TextboxLayoutManager", "Loaded " + layoutMap.size + " textbox layout(s).");
        } catch (Exception e) {
            Gdx.app.error("TextboxLayoutManager", "Failed to load layout config", e);
        }
    }

    public static TextboxLayout getLayout(String id) {
        TextboxLayout layout = layoutMap.get(id);
        if (layout == null) {
            layout = layoutMap.get("default");
            if (layout == null) {
                layout = new TextboxLayout(15, 15, "left"); // fallback default
            }
        }
        return layout;
    }
}

