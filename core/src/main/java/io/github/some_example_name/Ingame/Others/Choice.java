package io.github.some_example_name.Ingame.Others;

import java.util.List;
import java.util.Map;

public class Choice {
    public String text;
    public String next;
    public List<Map<String, Integer>> effects;

    public Choice() {}

    public String getText() {
        return text;
    }

    public String getTargetSceneId() {
        return next;
    }
}

