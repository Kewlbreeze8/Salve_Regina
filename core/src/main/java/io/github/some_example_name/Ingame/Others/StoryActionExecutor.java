package io.github.some_example_name.Ingame.Others;

import com.badlogic.gdx.utils.Timer;
import io.github.some_example_name.Ingame.IngameUI;
import io.github.some_example_name.Ingame.IngameController;
import io.github.some_example_name.Ingame.StoryManager;
import io.github.some_example_name.Menu.Credits;
import io.github.some_example_name.Menu.Others.GameInstance;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import static io.github.some_example_name.Ingame.StoryManager.setCurrentNode;

public class StoryActionExecutor {

    private final IngameUI ui;
    private final IngameController controller;

    public StoryActionExecutor(IngameUI ui, IngameController controller) {
        this.ui = ui;
        this.controller = controller;
    }

    public void execute(List<Action> actions) {
        if (actions == null) return;

        for (Action action : actions) {
            switch (action.type) {

                // --------------------------
                // Visual Asset - Intro
                // --------------------------

                case "setBG":
                    ui.setBackground(action.value);
                    break;

                case "setFontColor":
                    ui.setFontColor(action.value);
                    break;

                case "setTextbox":
                    ui.setTextboxStyle(action.value);
                    break;

                case "setSprite": {
                    String slot = action.slot != null ? action.slot : "center";
                    String character = action.character;
                    String expression = action.expression != null ? action.expression :
                        action.sprite != null ? action.sprite : ""; // fallback
                    String spriteId = character + expression;

                    ui.setCharacterSprite(slot, spriteId);
                    break;
                }

                // --------------------------
                // Visual Asset - Middle
                // --------------------------

                case "changeBG":
                    ui.setBackground(action.value);
                    break;

                case "changeFontColor":
                    ui.setFontColor(action.value);
                    break;

                case "changeTextbox":
                    ui.setTextboxStyle(action.value);
                    break;

                case "changeSprite": {
                    String slot = action.slot != null ? action.slot : "center";

                    String character = action.character;
                    String expression = action.expression != null ? action.expression :
                        action.sprite != null ? action.sprite : "";

                    if (character != null && !character.isEmpty()) {
                        String spriteId = character + expression;
                        ui.setCharacterSprite(slot, spriteId);
                    } else {
                        System.err.println("[ActionExecutor] changeSprite: character is null or empty.");
                    }
                    break;
                }


                // --------------------------
                // Visual Asset - Outro
                // --------------------------

                case "clearBG":
                    ui.clearBackground();
                    break;

                case "clearFontColor":
                    ui.resetFontColor();
                    break;

                case "clearTextbox":
                    ui.clearTextboxStyle();
                    break;

                case "clearSprite": {
                    String slot = action.slot;
                    if (slot == null) {
                        slot = guessSlotFromCharacter(action.character);
                    }
                    ui.clearCharacterSprite(slot);
                    break;
                }

                case "clearAllSprites":
                    ui.clearCharacterSprite("left");
                    ui.clearCharacterSprite("center");
                    ui.clearCharacterSprite("right");
                    break;

                // --------------------------
                // Audio
                // --------------------------

                case "playBGM":
                    controller.playMusic(action.value, action.fade);
                    break;

                case "stopBGM":
                    controller.stopMusic(action.fade);
                    break;

                case "playSFX":
                    controller.playSFX(action.value);
                    break;

                case "stopSFX":
                    controller.stopSFX();
                    break;

                // --------------------------
                // Transitions
                // --------------------------

                case "loadChapter":
                    int chapterIndex = Integer.parseInt(action.getValue());
                    StoryManager.loadChapterByIndex(chapterIndex);
                    break;

                case "gotoScene":
                    try {
                        String targetScene = action.value;

                        // Special-case handling for UI Screens
                        if ("Credits".equalsIgnoreCase(targetScene)) {
                            GameInstance.getInstance().setScreen(new Credits(GameInstance.getInstance()));
                        }
                        else if (StoryManager.hasScene(targetScene)) {
                            StoryManager.goToScene(targetScene);
                        } else {
                            System.err.println("[ActionHandler] Scene '" + targetScene + "' not found.");
                        }

                    } catch (Exception e) {
                        System.err.println("[ActionHandler] Failed to go to scene: " + e.getMessage());
                    }
                    break;


                case "gotoNode":
                    String targetNodeId = action.value;
                    if (StoryManager.hasScene(targetNodeId)) {
                        setCurrentNode(targetNodeId);
                        controller.runCurrentNode();
                    } else {
                        System.err.println("[StoryActionExecutor] gotoNode: Scene ID '" + targetNodeId + "' not found.");
                    }
                    break;

                case "playVideo": {
                    String videoKey = action.value;
                    String videoUrl;

                    switch (videoKey) {
                        case "Pre_Ending":
                            videoUrl = "https://youtu.be/Y_9jlTIQvBg";
                            break;
                        case "True_Ecstasy":
                            videoUrl = "https://youtu.be/rXxUhpO3cYc";
                            break;
                        case "False_Guilt":
                            videoUrl = "https://youtu.be/VmqN1SsxZGs";
                            break;
                        default:
                            System.err.println("[playVideo] Unknown video key: " + videoKey);
                            return;
                    }

                    try {
                        java.awt.Desktop.getDesktop().browse(new java.net.URI(videoUrl));
                    } catch (Exception e) {
                        System.err.println("[playVideo] Failed to open browser: " + e.getMessage());
                    }

                    // Optional: immediately continue to next node if specified
                    if (action.getParams() != null && action.getParams().containsKey("nextNode")) {
                        String nextNodeId = action.getParams().get("nextNode");
                        setCurrentNode(nextNodeId);
                        controller.runCurrentNode();
                    }

                    break;
                }





                // --------------------------
                // Placeholder for Effects & Transitions
                // --------------------------

                case "visualEffect":
                    System.out.println("[VisualEffect] (WIP) Effect: " + action.value);
                    break;

                case "visualTransition":
                    System.out.println("[VisualTransition] (WIP) Transition: " + action.value);
                    break;

                case "chapter_transition": {
                    if (action.getParams() == null) {
                        System.err.println("[ActionExecutor] chapter_transition: Missing params.");
                        break;
                    }

                    String nextChapter = action.getParams().get("nextChapter");
                    String startNode = action.getParams().get("startNode");

                    if (nextChapter == null || startNode == null) {
                        System.err.println("[ActionExecutor] chapter_transition: Missing 'nextChapter' or 'startNode'.");
                        break;
                    }

                    System.out.println("Transitioning to Chapter: " + nextChapter + ", Start Node: " + startNode);
                    StoryManager.loadChapter(nextChapter, startNode); // ✅ Correct method
                    controller.runCurrentNode(); // Start the node right after loading the new chapter
                    break;
                }


                // --------------------------
                // Others
                // --------------------------

                case "fadeOut": {
                    Float duration = action.duration != null ? action.duration : 1.5f;
                    ui.fadeOut(duration);
                    break;
                }

                case "fadeIn": {
                    Float duration = action.duration != null ? action.duration : 1.5f;
                    ui.fadeIn(duration);
                    break;
                }

                case "checkStats": {
                    System.out.println("[DEBUG] Executing Action: " + action.type + " | Value: " + action.value);
                    if (action.getParams() != null) {
                        System.out.println("[DEBUG] Params: " + action.getParams());
                    }

                    if (action.conditions != null) {
                        for (Map<String, Object> condition : action.conditions) {
                            int penance = Stats.get("penance");

                            Object stat = condition.get("stat");
                            Object ge = condition.get("greaterThanOrEqual");
                            Object lt = condition.get("lessThan");
                            String next = (String) condition.get("next");

                            if ("penance".equals(stat)) {
                                if (ge instanceof Number && penance >= ((Number) ge).intValue()) {
                                    setCurrentNode(next);
                                    controller.runCurrentNode(); // ✅ Start it
                                    break;
                                }
                                if (lt instanceof Number && penance < ((Number) lt).intValue()) {
                                    setCurrentNode(next);
                                    controller.runCurrentNode(); // ✅ Start it
                                    break;
                                }
                            }
                        }
                    }
                    break;
                }

                default:
                    System.err.println("[ActionExecutor] Unknown action type: " + action.type);

            }
        }
    }

    private String guessSlotFromCharacter(String character) {
        if (character == null) return "center";
        switch (character.toLowerCase()) {
            case "elsa": return "center";
            case "father":
            case "fatherpast": return "right";
            case "mother":
            case "mother1":
            case "mother2": return "left";
            case "psychiatrist1":
            case "psychiatrist2":
            case "priest": return "left";
            default: return "center"; // fallback
        }
    }

}

