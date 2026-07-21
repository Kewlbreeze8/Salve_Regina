package io.github.kewlbreeze8.Ingame.Others;

import com.badlogic.gdx.utils.JsonValue;
import java.util.HashMap;
import java.util.Map;
import io.github.kewlbreeze8.Ingame.Others.Choice;
public class PenanceManager {

    private int penance;
    private final Map<String, Integer> nodeModifiers;
    private String trueEndingId;
    private String falseEndingId;
    private int threshold;

    public PenanceManager() {
        this.penance = 50;
        this.nodeModifiers = new HashMap<>();

        this.threshold = 50;
        this.trueEndingId = "";
        this.falseEndingId = "";

        System.out.println("[PenanceManager] Default constructor loaded.");
    }

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

    // <<< ADD THE NEW METHOD HERE >>>

    public void applyChoice(Choice choice) {

        if (choice.getEffects() == null) return;

        for (Map<String, Number> effect : choice.getEffects()) {

            if (effect.containsKey("penance")) {

                Number value = effect.get("penance");
                int delta = value.intValue();

                penance += delta;

                penance = Math.max(0, Math.min(penance, 100));

                System.out.println("[PENANCE] " + delta + " -> " + penance);
            }
        }
    }


    public String getEndingNodeId() {
        return (penance >= threshold) ? trueEndingId : falseEndingId;
    }

    public int getPenance() {
        return penance;
    }

    public void addPenance(int amount) {

        penance += amount;

        penance = Math.max(0, Math.min(100, penance));

        System.out.println("[Penance] "
                + (amount >= 0 ? "+" : "")
                + amount
                + " -> "
                + penance);
    }    
}