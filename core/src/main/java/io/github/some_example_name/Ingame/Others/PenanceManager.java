package io.github.some_example_name.Ingame.Others;

import com.badlogic.gdx.utils.JsonValue;
import java.util.HashMap;
import java.util.Map;

public class PenanceManager {

    private int penance;
    private final Map<String, Integer> nodeModifiers;
    private String trueEndingId;
    private String falseEndingId;
    private int threshold;

    public PenanceManager(JsonValue penanceJson) {
        // Set default penance
        this.penance = penanceJson.get("defaults").getInt("penance", 50);

        // Parse stat modifiers from each node
        nodeModifiers = new HashMap<>();
        for (JsonValue rule : penanceJson.get("statRules")) {
            String node = rule.getString("onNodeEnter");
            JsonValue modStat = rule.get("statChange");

            if (modStat.has("penance")) {
                int delta = modStat.getInt("penance");
                nodeModifiers.put(node, delta);
            }
        }

        // Load ending rules
        JsonValue endings = penanceJson.get("endingRules");
        this.threshold = endings.getInt("threshold");
        this.trueEndingId = endings.getString("trueEnding");
        this.falseEndingId = endings.getString("falseEnding");

        System.out.println("[PenanceManager] Initialized with default penance: " + penance);
    }

    public void handleNodeEnter(String nodeId) {
        if (nodeModifiers.containsKey(nodeId)) {
            int change = nodeModifiers.get(nodeId);
            penance += change;

            // Optional: Clamp penance between 0 and 100
            penance = Math.max(0, Math.min(penance, 100));

            System.out.println("[Penance] Node triggered: " + nodeId + " | Change: " + change + " | New Value: " + penance);
        }
    }

    public String getEndingNodeId() {
        return (penance >= threshold) ? trueEndingId : falseEndingId;
    }

    public int getPenance() {
        return penance;
    }
}


