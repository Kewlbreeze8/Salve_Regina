package io.github.some_example_name.Ingame.Others;

import java.util.List;
import java.util.Map;

public class Action {
    public String character;
    public String expression;
    public String slot;
    public String sprite;
    public String stat;
    public String type;
    public String value; // e.g. filename, video name, etc.

    public Float duration;

    public boolean fade;
    public List<Map<String, Object>> conditions;
    public Map<String, String> params;

    public Action() {} // Required for JSON parsing

    public static Action fromMap(Map<String, Object> map) {
        if (map == null || !map.containsKey("type")) return null;

        Action action = new Action();
        action.setType((String) map.get("type"));

        // Optional fields
        if (map.containsKey("value")) action.setValue((String) map.get("value"));
        if (map.containsKey("character")) action.setCharacter((String) map.get("character"));
        if (map.containsKey("expression")) action.setExpression((String) map.get("expression"));
        if (map.containsKey("slot")) action.setSlot((String) map.get("slot"));
        if (map.containsKey("fade")) action.setFade((Boolean) map.get("fade"));

        return action;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public void setFade(boolean fade) {
        this.fade = fade;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public Map<String, String> getParams() {
        return params;
    }
}
