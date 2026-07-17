package io.github.kewlbreeze8.Ingame.Others;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class StoryNode {
    public String id;
    public String type;
    public String next;

    public List<Action> actions;
    public List<Line> lines;
    public List<Choice> choices;

    // 🔑 Optional metadata (used for chapter transitions or special flags)
    public Map<String, String> params = new HashMap<>();

    private int choiceIndex = 0;

    public static Map<String, StoryNode> storyNodes = new HashMap<>();

    public StoryNode() {} // Required for JSON parsing

    // ✅ Core Getters
    public String getId() { return id; }

    public String getType() { return type; }

    public String getNext() { return next; }

    public List<Action> getActions() { return actions; }

    public List<Line> getLines() { return lines; }

    public List<Choice> getChoices() { return choices; }

    public int getChoiceIndex() { return choiceIndex; }

    public void setChoiceIndex(int index) {
        this.choiceIndex = index;
    }

    public Map<String, String> getParams() {
        return params;
    }

    // 🧠 Utility method to check for chapter transition nodes
    public boolean isChapterTransition() {
        return "chapter_transition".equalsIgnoreCase(type)
            && params.containsKey("nextChapter");
    }

    public String getParam(String key) {
        return params.get(key);
    }
}

