package io.github.kewlbreeze8.Ingame.Others;

import java.util.List;
import java.util.Map;

public class Choice {
    public String text;
    public String next;
    public List<Map<String, Number>> effects;

    public Choice() {}

    public String getText() {
        return text;
    }

    public String getTargetSceneId() {
        return next;
    }
    
    public List<Map<String, Number>> getEffects() {
        return effects;
    }
}

