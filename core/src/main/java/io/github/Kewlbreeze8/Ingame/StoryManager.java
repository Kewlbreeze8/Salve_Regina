package io.github.Kewlbreeze8.Ingame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

import io.github.Kewlbreeze8.Ingame.Others.*;

import java.util.*;

public class StoryManager {
    private static StoryNode currentNode;
    private static String startNodeId;
    private static String currentChapterName; // ✅ New field
    private static PenanceManager penanceManager;

    private static int currentChapterIndex = 0;

    private static final Map<String, StoryNode> scenes = new HashMap<>();
    private static final List<String> printedLines = new ArrayList<>();
    private static final List<String> chapterFiles = new ArrayList<>(
        Arrays.asList("Chapter0", "Chapter1", "Chapter2", "Chapter3", "Endings")
    );
    private static final String CHAPTER_DIR = "script/";

    // --- Json Config ---
    private static Json createConfiguredJson() {
        Json json = new Json();
        json.setElementType(StoryNode.class, "lines", Line.class);
        json.setElementType(StoryNode.class, "actions", Action.class);
        json.setElementType(StoryNode.class, "choices", Choice.class);
        json.setSerializer(Map.class, new Json.ReadOnlySerializer<Map>() {
            @Override
            public Map read(Json json, JsonValue jsonData, Class type) {
                return json.readValue(HashMap.class, jsonData);
            }
        });
        return json;
    }

    // --- Chapter Loading ---

    public static void loadChapterByIndex(int index) {
        if (index < 0 || index >= chapterFiles.size()) {
            throw new IllegalArgumentException("[StoryManager] Invalid chapter index: " + index);
        }

        currentChapterIndex = index;
        String filePath = CHAPTER_DIR + chapterFiles.get(index) + ".json";

        clearCurrentChapter();

        try {
            loadFromJson(filePath); // Sets startNodeId internally
            setCurrentNode(startNodeId);
            System.out.println("[StoryManager] ✅ Chapter " + chapterFiles.get(index) + " loaded successfully.");
        } catch (Exception e) {
            System.err.println("[StoryManager] ❌ Failed to load chapter: " + e.getMessage());
        }
    }

    public static boolean loadNextChapter() {
        if (currentChapterIndex + 1 < chapterFiles.size()) {
            loadChapterByIndex(currentChapterIndex + 1);
            return true;
        } else {
            System.out.println("[StoryManager] 🏁 No more chapters available.");
            return false;
        }
    }

    public static StoryNode loadChapter(String chapterKey, String startNodeOverride) {
        clearCurrentChapter();
        currentChapterName = chapterKey; // ✅ Set the loaded chapter
        Json json = createConfiguredJson();

        JsonValue root;
        try {
            root = new JsonReader().parse(Gdx.files.internal(CHAPTER_DIR + chapterKey + ".json"));
        } catch (Exception e) {
            throw new RuntimeException("[ERROR] Failed to load chapter file: " + chapterKey + ".json", e);
        }

        String startingNodeId = (startNodeOverride != null) ? startNodeOverride : root.getString("startNode", null);
        if (startingNodeId == null) {
            throw new IllegalArgumentException("[ERROR] Chapter '" + chapterKey + "' missing 'startNode'.");
        }

        JsonValue nodesArray = root.get("nodes");
        if (nodesArray == null) {
            throw new IllegalStateException("[ERROR] Chapter '" + chapterKey + "' has no 'nodes' array.");
        }

        loadScenes(json, nodesArray);

        if (!scenes.containsKey(startingNodeId)) {
            System.err.println("[WARNING] Start node '" + startingNodeId + "' not found in chapter '" + chapterKey + "'. Falling back to default startNode.");
            startingNodeId = root.getString("startNode"); // force it to fallback to chapter's real startNode

            if (!scenes.containsKey(startingNodeId)) {
                throw new IllegalArgumentException("[FATAL] Even fallback startNode '" + startingNodeId + "' not found. Cannot continue.");
            }
        }

        currentNode = scenes.get(startingNodeId);
        startNodeId = startingNodeId;

        System.out.println("[INFO] ✅ Chapter '" + chapterKey + "' loaded. Starting at node: '" + startingNodeId + "'");
        return currentNode;
    }

    public static void loadFromJson(String filePath) {
        if (!Gdx.files.internal(filePath).exists()) {
            throw new RuntimeException("[StoryManager] File not found: " + filePath);
        }

        clearCurrentChapter();
        Json json = createConfiguredJson();

        JsonValue root = new JsonReader().parse(Gdx.files.internal(filePath));
        startNodeId = root.getString("startNode", null);
        if (startNodeId == null) {
            throw new IllegalArgumentException("JSON does not contain a valid 'startNode' key.");
        }

        loadScenes(json, root.get("nodes"));

        if (!scenes.containsKey(startNodeId)) {
            throw new IllegalArgumentException("startNode '" + startNodeId + "' not found among loaded scenes.");
        }
    }

    // --- Scene Loading ---
    private static void loadScenes(Json json, JsonValue nodes) {
        for (JsonValue node : nodes) {
            try {
                StoryNode storyNode = json.readValue(StoryNode.class, node);
                addScene(storyNode.getId(), storyNode);
            } catch (Exception e) {
                System.err.println("[ERROR] Failed to parse node: " + e.getMessage() + "\nRaw: " + node);
            }
        }
    }

    // --- Scene Navigation ---
    public static void addScene(String id, StoryNode scene) {
        if (id == null || scene == null) {
            System.err.println("[ERROR] Tried to add null scene or scene with null ID.");
            return;
        }
        scenes.put(id, scene);
    }

    public static void setCurrentNode(String sceneId) {
        currentNode = scenes.get(sceneId);
        if (currentNode == null) {
            System.err.println("[ERROR] Scene ID '" + sceneId + "' not found.");
            System.err.println("[DEBUG] Available scenes: " + scenes.keySet());
            throw new IllegalArgumentException("Scene ID '" + sceneId + "' not found in loaded scenes.");
        }
    }

    public static void goToScene(String sceneId) {
        if (hasScene(sceneId)) {
            setCurrentNode(sceneId);
            if (penanceManager != null) {
                penanceManager.handleNodeEnter(sceneId);
            }
        } else {
            System.err.println("[WARN] Scene '" + sceneId + "' not found. Attempting fallback...");

            // Try using the chapter's actual starting node instead
            String fallbackNode = getStartNodeId();
            if (fallbackNode != null && hasScene(fallbackNode)) {
                System.err.println("[Fallback] Scene fallback successful. Loading startNode: " + fallbackNode);
                setCurrentNode(fallbackNode);
                if (penanceManager != null) {
                    penanceManager.handleNodeEnter(fallbackNode);
                }
            } else {
                System.err.println("[FATAL] No valid fallback node found. Aborting scene load.");
                System.err.println("[DEBUG] Available scenes: " + scenes.keySet());
                throw new IllegalArgumentException("Scene ID '" + sceneId + "' not found in loaded scenes.");
            }
        }
    }

    public static boolean hasScene(String sceneId) {
        return scenes.containsKey(sceneId);
    }

    // --- Text Logging ---
    public static void registerLine(String speaker, String text) {
        if (text == null || text.isEmpty()) return;

        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        json.addChild("speaker", new JsonValue(speaker != null ? speaker : "Narrator"));
        json.addChild("text", new JsonValue(text.trim()));

        String finalLine = json.toJson(JsonWriter.OutputType.json);

        if (printedLines.isEmpty() || !printedLines.get(printedLines.size() - 1).equals(finalLine)) {
            printedLines.add(finalLine);
        }
    }

    public static List<String> getPrintedLines() {
        return printedLines;
    }

    public static void setPrintedLines(List<String> lines) {
        printedLines.clear();
        if (lines != null) {
            printedLines.addAll(lines);
        }
    }

    public static void clearPrintedLines() {
        printedLines.clear();
    }

    // --- Clear & Reset ---
    public static void clearCurrentChapter() {
        currentNode = null;
        startNodeId = null;
        scenes.clear();
    }

    public static void resetPrintedLines() {
        printedLines.clear();
    }

    public static void clearEverything() {
        clearCurrentChapter();
        penanceManager = null;
        currentChapterIndex = 0;
    }

    // --- Misc ---
    public static Set<String> getSceneKeys() {
        return scenes.keySet();
    }

    public static String getStartNodeId() {
        return startNodeId;
    }

    public static StoryNode getCurrentNode() {
        if (currentNode == null) {
            System.err.println("[WARN] getCurrentNode() returned null. Chapter may not be loaded.");
        }
        return currentNode;
    }

    public static StoryNode getScene(String sceneId) {
        return scenes.get(sceneId);
    }

    public static PenanceManager getPenanceManager() {
        return penanceManager;
    }

    public static void loadPenance(String filePath) {
        JsonValue root = new JsonReader().parse(Gdx.files.internal(filePath));
        penanceManager = new PenanceManager(root);
    }

    public static void validateChapter(JsonValue root, String filename) {
        String start = root.getString("startNode", null);
        JsonValue nodes = root.get("nodes");

        Set<String> nodeIds = new HashSet<>();
        for (JsonValue node : nodes) {
            nodeIds.add(node.getString("id", null));
        }

        if (start == null || !nodeIds.contains(start)) {
            throw new IllegalStateException("[VALIDATOR ERROR] In file " + filename + ", startNode '" + start + "' not found in node IDs: " + nodeIds);
        }

        System.out.println("[VALIDATOR] " + filename + " passed ✅. Found nodes: " + nodeIds);
    }

    public static String getCurrentChapterName() {
        return currentChapterName;
    }

}
