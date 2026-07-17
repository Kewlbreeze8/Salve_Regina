package io.github.Kewlbreeze8.Ingame;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;

import io.github.Kewlbreeze8.Ingame.IngameCrew.ButtonPanel;
import io.github.Kewlbreeze8.Ingame.Others.*;
import io.github.Kewlbreeze8.Menu.*;
import io.github.Kewlbreeze8.Menu.Others.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;


import java.util.List;

public class IngameController implements Screen {

    private static final String baseFolder = "video";

    private final Game game;
    private final IngameUI ui;
    private final StringBuilder visibleText = new StringBuilder();
    private final StoryActionExecutor actionExecutor;

    private Line currentLine;
    private StoryNode currentNode;
    private String currentSceneId;
    private String fullLine = "";

    private boolean isTyping = false;
    private boolean skipMode = false;
    private boolean autoMode = false;
    private boolean hasSaved = false;

    private float typeTimer = 0f;
    private int choiceCount = 0;
    private int dialogueIndex;

    public IngameController(Game game, String sceneId, int dialogueIndex) {
        this.game = game;

        if (StoryManager.getStartNodeId() == null) {
            System.out.println("[DEBUG] StoryManager not loaded. Loading default story...");
            StoryManager.loadFromJson("script/Chapter0.json");
        }

        Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));
        this.ui = new IngameUI(game, skin);

        this.dialogueIndex = dialogueIndex;
        this.currentSceneId = sceneId;

        AudioManager.loadSavedVolumes();
        setupButtonListeners();

        if (sceneId == null || !StoryManager.hasScene(sceneId)) {
            System.out.println("[WARN] Scene '" + sceneId + "' not found. Falling back to 'Intro'.");
            sceneId = "Intro";
        }

        StoryManager.setCurrentNode(sceneId);
        currentNode = StoryManager.getCurrentNode();
        this.actionExecutor = new StoryActionExecutor(ui, this);
    }

    private void setupButtonListeners() {
        ButtonPanel bp = ui.getButtonPanel();
        bp.getHistoryButton().addListener(new ButtonClick(() -> game.setScreen(new History(game, this))));
        bp.getSkipButton().addListener(new ButtonClick(this::toggleSkip));
        bp.getAutoButton().addListener(new ButtonClick(this::toggleAuto));
        bp.getSaveLoadButton().addListener(new ButtonClick(this::openSaveLoad));
        bp.getOptionsButton().addListener(new ButtonClick(this::openOptions));
        bp.getMainMenuButton().addListener(new ButtonClick(this::confirmMainMenu));
    }

    public void runCurrentNode() {
        currentNode = StoryManager.getCurrentNode();
        dialogueIndex = 0;

        List<Action> nodeActions = currentNode.getActions();
        if (nodeActions != null && !nodeActions.isEmpty()) {
            System.out.println("[DEBUG] Executing node-level actions: " + nodeActions);
            actionExecutor.execute(nodeActions);
        }

        switch (currentNode.type) {
            case "dialogue":
                ui.setDialogueBoxCentered(false);
                continueDialogue();
                break;

            case "choice":
                showChoicePrompt();
                break;

            case "system":
                actionExecutor.execute(currentNode.getActions());
                if (currentNode.next != null) {
                    StoryManager.setCurrentNode(currentNode.next);
                    runCurrentNode();
                }
                break;

            default:
                System.err.println("[ERROR] Unknown node type: " + currentNode.type);
        }
    }

    private void showChoicePrompt() {
        if (currentNode.choices != null && !currentNode.choices.isEmpty()) {
            String promptText = (currentNode.lines != null && !currentNode.lines.isEmpty())
                ? currentNode.lines.get(0).text
                : "";

            ui.setDialogueBoxCentered(promptText.isEmpty());
            ui.showDialogue("", promptText, null);

            // ✅ Pass the correct dynamic index here
            int currentChoiceIndex = choiceCount;
            ui.showChoices(currentNode.choices, choice -> {
                StoryManager.setCurrentNode(choice.getTargetSceneId());
                Gdx.app.postRunnable(this::runCurrentNode);
            }, currentChoiceIndex);

            choiceCount++; // ✅ Increment for next time
        } else {
            System.err.println("[ERROR] Choice node has no choices.");
        }
    }

    private void updateNodeLine(int index) {
        if (currentNode == null || currentNode.lines == null) {
            System.err.println("[ERROR] currentNode or lines are null.");
            return;
        }

        // New block: execute all action-only lines first
        while (index < currentNode.lines.size()) {
            Line line = currentNode.lines.get(index);
            if (line.text == null || line.text.isEmpty()) {
                if (line.action != null) {
                    List<Action> actions = new java.util.ArrayList<>();
                    actions.add(line.action);
                    actionExecutor.execute(actions);
                }
                index++;
                dialogueIndex = index;
            } else {
                break;
            }
        }

        // 💥 Prevent crashing when no more lines exist
        if (index >= currentNode.lines.size()) {
            if (currentNode.next != null) {
                StoryManager.setCurrentNode(currentNode.next);
                runCurrentNode();
            } else {
                System.out.println("[DEBUG] Reached end of current node.");
            }
            return;
        }

        // Continue as normal
        prepareAssetsThenType(index);
    }

    private void prepareAssetsThenType(int index) {
        currentLine = currentNode.lines.get(index);
        fullLine = currentLine.text != null ? currentLine.text : "";
        visibleText.setLength(0);
        isTyping = false;

        StoryManager.registerLine(currentLine.speaker, fullLine);

        if (currentLine.action != null) {
            List<Action> actions = new java.util.ArrayList<>();
            actions.add(currentLine.action);
            actionExecutor.execute(actions);
        }

        isTyping = true;
        typeTimer = 0f;

        String speaker = currentLine.speaker != null ? currentLine.speaker : "";
        ui.showDialogue(speaker, "", null); // shows empty dialogue box with name

    }

    public void continueDialogue() {
        if (currentNode.lines != null && dialogueIndex < currentNode.lines.size()) {
            updateNodeLine(dialogueIndex);
        } else {
            if (currentNode.next != null) {
                StoryManager.setCurrentNode(currentNode.next);
                runCurrentNode();
            }
        }
    }

    private void toggleSkip() {
        skipMode = !skipMode;
        System.out.println("[DEBUG] Skip mode: " + (skipMode ? "ON" : "OFF"));
    }

    private void toggleAuto() {
        autoMode = !autoMode;
        System.out.println("[DEBUG] Auto mode: " + (autoMode ? "ON" : "OFF"));
    }

    private void openSaveLoad() {
        game.setScreen(new LoadGame(game, this));
    }

    private void openOptions() {
        game.setScreen(new Options(game, this));
    }

    private void confirmMainMenu() {
        if (hasSaved) {
            game.setScreen(new MainMenu(game, true)); // ✅ Tell MainMenu you came from Ingame
            return;
        }

        // === Load Textures and Create Background ===
        Texture boxTexture = Assets.manager.get("popup/Box.png", Texture.class);
        Image boxBackground = new Image(new TextureRegionDrawable(new TextureRegion(boxTexture)));
        boxBackground.setSize(700, 500);

        // === Create Label ===
        BitmapFont font = ui.getSkin().getFont("default-font");
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);

        Label message = new Label("Return to Main Menu?\nUnsaved progress will be lost.", labelStyle);
        message.setFontScale(1.5f);
        message.setAlignment(Align.center);

        // === Buttons ===
        Texture yesUpTex = Assets.manager.get("popup/Yes1.png", Texture.class);
        Texture yesOverTex = Assets.manager.get("popup/Yes2.png", Texture.class);
        Texture noUpTex = Assets.manager.get("popup/No1.png", Texture.class);
        Texture noOverTex = Assets.manager.get("popup/No2.png", Texture.class);

        ImageButton yesButton = makeButton(yesUpTex, yesOverTex);
        ImageButton noButton = makeButton(noUpTex, noOverTex);

        // === Dialog Group ===
        Group dialogGroup = new Group();

        yesButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                AudioManager.playConfirmSound();
                AudioManager.stopStoryMusic(true);
                dialogGroup.addAction(Actions.sequence(
                    Actions.fadeOut(0.1f),
                    Actions.run(() -> {
                        Actor fadeOverlay = ui.getFadeOverlay(); // Ensure this exists
                        fadeOverlay.addAction(Actions.sequence(
                            Actions.fadeIn(0.5f),
                            Actions.delay(0.1f),
                            Actions.run(() -> game.setScreen(new MainMenu(game)))
                        ));
                    })
                ));
            }
        });

        noButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                AudioManager.playRejectSound();
                dialogGroup.remove();
            }
        });

        addHoverSound(yesButton);
        addHoverSound(noButton);

        Table content = new Table();
        content.setFillParent(true);
        content.pad(30f);
        content.add(message).colspan(2).expandX().center().padBottom(30f).row();
        content.add(yesButton).size(100, 100).padRight(40f);
        content.add(noButton).size(100, 100);

        dialogGroup.setSize(700, 500);
        dialogGroup.addActor(boxBackground);
        dialogGroup.addActor(content);

        dialogGroup.setPosition(
            (ui.getStage().getViewport().getWorldWidth() - dialogGroup.getWidth()) / 2f,
            (ui.getStage().getViewport().getWorldHeight() - dialogGroup.getHeight()) / 2f
        );

        ui.getStage().addActor(dialogGroup);
    }

    private void addHoverSound(Actor button) {
        button.addListener(new InputListener() {
            boolean hovered = false;

            @Override public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (!hovered) {
                    AudioManager.playHoverSound();
                    hovered = true;
                }
            }

            @Override public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                hovered = false;
            }
        });
    }

    public void saveToSlot(String slotId) {
        SaveManager.save(slotId, "scene", currentNode.id);
        SaveManager.save(slotId, "dialogueIndex", dialogueIndex);
        SaveManager.save(slotId, "chapter", StoryManager.getCurrentChapterName());
        SaveManager.save(slotId, "history", StoryManager.getPrintedLines());
        hasSaved = true;

        SlotUtil.updateTimestamp(slotId);

        if (!Gdx.files.local("saves/").exists()) {
            Gdx.files.local("saves/").mkdirs();
        }

        Pixmap pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Pixmap flipped = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), pixmap.getFormat());

        for (int y = 0; y < pixmap.getHeight(); y++) {
            for (int x = 0; x < pixmap.getWidth(); x++) {
                flipped.drawPixel(x, pixmap.getHeight() - y - 1, pixmap.getPixel(x, y));
            }
        }

        String path = "saves/thumb_" + slotId + ".png";
        PixmapIO.writePNG(Gdx.files.local(path), flipped);
        SaveManager.save(slotId, "screenshotPath", path);

        pixmap.dispose();
        flipped.dispose();
    }

    private ImageButton makeButton(Texture up, Texture over) {
        Drawable upDrawable = new TextureRegionDrawable(new TextureRegion(up));
        Drawable overDrawable = new TextureRegionDrawable(new TextureRegion(over));

        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.up = upDrawable;
        style.over = overDrawable;

        return new ImageButton(style);
    }

    public void playVideo(String fileName, String nextNodeId, boolean isEnding, String penanceLevel) {
        System.out.println("[DEBUG] Playing video: " + fileName);
        GameInstance.getInstance().setScreen(
            new FrameVideoScreen(GameInstance.getInstance(), baseFolder, fileName, nextNodeId, isEnding, penanceLevel)
        );
    }







    @Override
    public void show() {
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(ui.getStage());
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (isTyping) {
                    visibleText.setLength(0);
                    visibleText.append(fullLine);
                    ui.showDialogue(null, fullLine, null);
                    isTyping = false;

                    // ✅ Log the skipped line if it hasn't been logged yet
                    if (!StoryManager.getPrintedLines().contains(fullLine)) {
                        StoryManager.registerLine(currentLine.speaker, fullLine);
                    }

                } else {
                    dialogueIndex++;
                    updateNodeLine(dialogueIndex);
                }
                return true;
            }

        });

        Gdx.input.setInputProcessor(multiplexer);

        ui.getStage().getRoot().getColor().a = 0f;
        ui.getStage().getRoot().addAction(
            Actions.sequence(
                Actions.fadeIn(1.0f),
                Actions.run(() -> updateNodeLine(dialogueIndex))
            )
        );
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        ui.getStage().act(delta);

        if (isTyping) {
            typeTimer += delta;
            if (typeTimer >= 0.03f && visibleText.length() < fullLine.length()) {
                visibleText.append(fullLine.charAt(visibleText.length()));
                ui.showDialogue(null, visibleText.toString(), null);
                typeTimer = 0f;
            }
        }

        if (!isTyping && skipMode) {
            dialogueIndex++;
            updateNodeLine(dialogueIndex);
        }

        ui.getStage().draw();
    }

    @Override public void resize(int width, int height) {
        ui.getStage().getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { ui.dispose(); }

    public IngameUI getUI() { return ui; }
    public boolean isTyping() { return isTyping; }
    public String getCurrentSceneId() { return currentSceneId; }
    public int getDialogueIndex() { return dialogueIndex; }

    public void playMusic(String trackName, boolean fade) {
        AudioManager.playStoryMusic(trackName, fade);
        System.out.println("[DEBUG] Playing BGM: " + trackName + " (fade=" + fade + ")");
    }

    public void stopMusic(boolean fade) {
        AudioManager.stopStoryMusic(fade);
        System.out.println("[DEBUG] Stopping BGM (fade=" + fade + ")");
    }

    public void playSFX(String sfxName) {
        AudioManager.playCustomSFX(sfxName);
        System.out.println("[DEBUG] Playing SFX: " + sfxName);
    }

    public void stopSFX() {
        AudioManager.stopCustomSFX();
        System.out.println("[DEBUG] Stopping SFX with fade");
    }

    private static class ButtonClick extends ClickListener {
        private final Runnable action;
        public ButtonClick(Runnable action) { this.action = action; }
        @Override public void clicked(InputEvent event, float x, float y) {
            action.run();
        }
    }
}
