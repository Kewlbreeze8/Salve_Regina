package io.github.kewlbreeze8.Ingame.Others.Story;

import io.github.kewlbreeze8.Ingame.Main.IngameController;
import io.github.kewlbreeze8.Ingame.Main.IngameUI;
import io.github.kewlbreeze8.Ingame.Main.StoryManager;
import io.github.kewlbreeze8.Ingame.Others.Video.VideoScreen;
import io.github.kewlbreeze8.Menu.Main.Credits;
import io.github.kewlbreeze8.Menu.Others.System.GameInstance;

import static io.github.kewlbreeze8.Ingame.Main.StoryManager.setCurrentNode;

import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;

public class StoryActionExecutor {

    private final IngameUI ui;
    private final IngameController controller;

    public StoryActionExecutor(IngameUI ui, IngameController controller) {
        this.ui = ui;
        this.controller = controller;
    }

    public boolean execute(List<Action> actions) {
        if (actions == null) return false;

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
                    
                    if (videoKey == null || videoKey.isEmpty()) {
                        System.err.println("[playVideo] Missing video key.");
                        return true;
                    }

                    final String nextNode =
                    action.getParams() != null
                    ? (String) action.getParams().get("nextNode")
                    : null;

                    Gdx.app.postRunnable(() -> {
                        System.out.println("[StoryActionExecutor] setScreen -> " + videoKey);
                        GameInstance.getInstance().setScreen(
                            new VideoScreen(
                                videoKey + ".mp4",
                                () -> {

                                    System.out.println("[playVideo] Finished: " + videoKey);

                                    System.out.println(
                                        "[DEBUG] Current Screen = " +
                                        GameInstance.getInstance().getScreen().getClass().getName()
                                    );

                                    System.out.println(
                                        "[DEBUG] Current Node = " +
                                        StoryManager.getCurrentNode().id
                                    );

                                    if (nextNode != null) {
                                        StoryManager.setCurrentNode(nextNode);
                                    }

                                    controller.runCurrentNode();

                                }
                            )
                        );

                    });

                    System.out.println("[playVideo] Returning PAUSE");
                    return true;
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
                    System.out.println("[chapter_transition] Returning PAUSE");
                    StoryManager.loadChapter(nextChapter, startNode); // ✅ Correct method

                    System.out.println(
                        "[chapter_transition] Current node after load = "
                        + StoryManager.getCurrentNode()
                    );

                    controller.runCurrentNode(); // Start the node right after loading the new chapter
                    return true; // Pause execution until the new chapter is loaded
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
                            int penance = controller.getPenanceManager().getPenance();

                            Object stat = condition.get("stat");
                            Object ge = condition.get("greaterThanOrEqual");
                            Object lt = condition.get("lessThan");
                            String next = (String) condition.get("next");

                            if ("penance".equals(stat)) {

                                if (ge instanceof Number && penance >= ((Number) ge).intValue()) {
                                    setCurrentNode(next);
                                    return true;
                                }

                                if (lt instanceof Number && penance < ((Number) lt).intValue()) {
                                    setCurrentNode(next);
                                    return true;
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

        return false;
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

