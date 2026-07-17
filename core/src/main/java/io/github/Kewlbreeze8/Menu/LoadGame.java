package io.github.Kewlbreeze8.Menu;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;

import io.github.Kewlbreeze8.Ingame.IngameController;
import io.github.Kewlbreeze8.Ingame.StoryManager;
import io.github.Kewlbreeze8.Menu.Others.AudioManager;
import io.github.Kewlbreeze8.Menu.Others.HoverSoundListener;
import io.github.Kewlbreeze8.Menu.Others.SaveManager;
import io.github.Kewlbreeze8.Menu.Others.SlotUtil;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import java.util.List;

public class LoadGame implements Screen {
    private final Game game;
    private final Stage stage;
    private final Screen returnScreen;
    private final ImageButton[] slotButtons = new ImageButton[6];
    private final Label[] slotTimestampLabels = new Label[6];

    private int selectedSlot = -1;
    private ImageButton saveBtn, loadBtn;

    public LoadGame(Game game, Screen returnScreen) {
        this.game = game;
        this.returnScreen = returnScreen;
        this.stage = new Stage(new FitViewport(1280, 720));
        Gdx.input.setInputProcessor(stage);

        Image bg = new Image(new Texture("menu/Background.png"));
        bg.setSize(1280, 720);
        stage.addActor(bg);

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Image loadLogo = new Image(new Texture("load/LoadLogo.png"));
        loadLogo.setScaling(Scaling.fit);
        loadLogo.setSize(500f, 150f);

        Table logoTable = new Table();
        logoTable.add(loadLogo).size(500, 120).padTop(20f).center();

        Table slotsTable = new Table();
        for (int i = 1; i < 6; i++) {
            slotsTable.add(createSlot(i)).size(1000, 150).padTop(15f).colspan(3).center().row();
        }

        saveBtn = createButton("load/Save1.png", "load/Save2.png", "load/Save3.png", "load/Save4.png");
        loadBtn = createButton("load/Load1.png", "load/Load2.png", "load/Load3.png", "load/Load4.png");
        ImageButton clearBtn = createButton("load/Clear1.png", "load/Clear2.png");
        ImageButton backBtn = createButton("load/Back1.png", "load/Back2.png");

        saveBtn.setDisabled(true);
        loadBtn.setDisabled(true);

        Table buttonRow = new Table();
        buttonRow.add(saveBtn).padRight(120f);
        buttonRow.add(loadBtn).padRight(120f);
        buttonRow.add(clearBtn).padRight(120f);
        buttonRow.add(backBtn);

        clearBtn.addListener(new HoverSoundListener());
        clearBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                AudioManager.playConfirmSound();
                if (selectedSlot != -1) {
                    SaveManager.clearSlot(SlotUtil.indexToSlotId(selectedSlot));
                    Timer.schedule(new Timer.Task() {
                        @Override public void run() {
                            Gdx.input.setInputProcessor(null);
                            game.setScreen(new LoadGame(game, returnScreen));
                        }
                    }, 0.3f);
                }
            }
        });

        backBtn.addListener(new HoverSoundListener());
        backBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                AudioManager.playConfirmSound();
                fadeToScreen(returnScreen != null ? returnScreen : new MainMenu(game));
            }
        });

        saveBtn.addListener(new HoverSoundListener());
        saveBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (saveBtn.isDisabled()) {
                    AudioManager.playRejectSound();
                    showPopup("You can't be Saved from Grace!");
                    return;
                }

                AudioManager.playConfirmSound();

                if (returnScreen instanceof IngameController) {
                    String slotId = SlotUtil.indexToSlotId(selectedSlot);
                    if (SaveManager.hasKey(slotId, "scene")) {
                        showOverwriteConfirmation(() -> {
                            ((IngameController) returnScreen).saveToSlot(slotId);
                            showSavedPopup();
                            delayReload();
                        });
                    } else {
                        ((IngameController) returnScreen).saveToSlot(slotId);
                        showSavedPopup();
                        delayReload();
                    }
                    stage.setScrollFocus(null);
                }
            }
        });

        loadBtn.addListener(new HoverSoundListener());
        loadBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (loadBtn.isDisabled()) {
                    AudioManager.playRejectSound();
                    return;
                }

                if (selectedSlot != -1) {
                    String slotId = SlotUtil.indexToSlotId(selectedSlot);
                    if (!SaveManager.hasKey(slotId, "scene")) {
                        AudioManager.playRejectSound();
                        showErrorOverlay(randomErrorMessage());
                        return;
                    }

                    String chapter = SaveManager.getString(slotId, "chapter");
                    String scene = SaveManager.loadString(slotId, "scene", null);
                    int index = SaveManager.loadInt(slotId, "dialogueIndex", 0);

                    if (chapter != null && scene != null) {
                        StoryManager.loadChapter(chapter, scene); // ✅

                        // 🧠 Restore History Log (clear first to avoid duplication)
                        StoryManager.clearPrintedLines();
                        List<String> restoredLogs = SaveManager.loadList(slotId, "history");
                        StoryManager.setPrintedLines(restoredLogs);

                        AudioManager.playConfirmSound();
                        AudioManager.fadeOutAndStopMenuMusic();

                        stage.clear();
                        Gdx.input.setInputProcessor(null);
                        game.setScreen(new IngameController(game, scene, index));
                    } else {
                        AudioManager.playRejectSound();
                        showErrorOverlay("Save file is missing required data.");
                    }

                }
            }
        });

        root.top();
        root.add(logoTable).expandX().top().padTop(10f).row();

        ScrollPane scrollPane = new ScrollPane(slotsTable);
        scrollPane.setFlickScroll(true);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollbarsOnTop(true);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setOverscroll(false, true);
        scrollPane.setForceScroll(false, true);

        root.add(scrollPane).expand().fill().pad(10).row();
        root.add(buttonRow).expandX().bottom().padBottom(20f).row();

        stage.getRoot().getColor().a = 0;
        stage.addAction(Actions.fadeIn(0.5f));
        stage.setScrollFocus(scrollPane);
    }

    private Stack createSlot(int index) {
        final int slotIndex = index;
        final String slotId = SlotUtil.indexToSlotId(slotIndex);

        Stack slotStack = new Stack();
        slotStack.setSize(1000, 150);
        slotStack.setTouchable(Touchable.childrenOnly);

        TextureRegionDrawable bgDrawable = SaveManager.hasKey(slotId, "scene")
            ? new TextureRegionDrawable(new Texture("load/SlotSave.png"))
            : new TextureRegionDrawable(new Texture("load/SlotEmpty.png"));

        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.up = bgDrawable;
        style.over = new TextureRegionDrawable(new Texture("load/SlotHovered.png"));
        style.checked = new TextureRegionDrawable(new Texture("load/SlotSelected.png"));
        style.down = style.checked;

        ImageButton button = new ImageButton(style);
        button.setSize(1000, 150);
        button.setTouchable(Touchable.enabled);
        button.addListener(new HoverSoundListener());
        button.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                AudioManager.playConfirmSound();

                if (selectedSlot == slotIndex) {
                    button.setChecked(false);
                    selectedSlot = -1;
                    saveBtn.setDisabled(true);
                    loadBtn.setDisabled(true);
                } else {
                    for (int i = 0; i < slotButtons.length; i++) {
                        if (i != slotIndex && slotButtons[i] != null) {
                            slotButtons[i].setChecked(false);
                        }
                    }

                    button.setChecked(true);
                    selectedSlot = slotIndex;

                    boolean hasData = SaveManager.hasKey(slotId, "scene");
                    boolean isIngame = returnScreen instanceof IngameController;

                    saveBtn.setDisabled(!(slotIndex > 0 && isIngame));
                    loadBtn.setDisabled(false);
                }
            }
        });

        slotButtons[index] = button;
        slotStack.add(button);

        String pathKey = (selectedSlot == index && SaveManager.hasKey(slotId, "ingameScreenshotPath"))
            ? "ingameScreenshotPath"
            : "screenshotPath";

        if (SaveManager.hasKey(slotId, pathKey)) {
            String path = SaveManager.loadString(slotId, pathKey, "");
            if (Gdx.files.local(path).exists()) {
                Texture thumbTex = new Texture(Gdx.files.local(path));
                Image thumbnail = new Image(thumbTex);
                thumbnail.setSize(180, 120);
                thumbnail.setPosition(25, 15);
                thumbnail.setTouchable(Touchable.disabled);
                slotStack.add(thumbnail);

                Container<Image> wrapper = new Container<>(thumbnail);
                wrapper.setTransform(true);
                wrapper.addListener(new HoverSoundListener());
                wrapper.addListener(new InputListener() {
                    @Override public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                        wrapper.addAction(Actions.scaleTo(1.1f, 1.1f, 0.2f));
                    }

                    @Override public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                        wrapper.addAction(Actions.scaleTo(1f, 1f, 0.2f));
                    }
                });
                slotStack.add(wrapper);
            }
        }

        if (!SaveManager.hasKey(slotId, "scene")) {
            Image staticEffect = new Image(new Texture("load/Static.png"));
            staticEffect.setColor(1, 1, 1, 0.3f);
            staticEffect.setTouchable(Touchable.disabled);
            slotStack.add(staticEffect);
        }

        Label.LabelStyle labelStyle = new Label.LabelStyle(new BitmapFont(), Color.WHITE);
        Label label;

        if (SaveManager.hasKey(slotId, "scene")) {
            String scene = SaveManager.loadString(slotId, "scene", "Scene");
            String timestamp = SlotUtil.getTimestampForSlot(slotId);
            String date = SlotUtil.getDateForSlot(slotId);

            Table labelTable = new Table();
            labelTable.setFillParent(true);
//            labelTable.center().padLeft(250f);
            labelTable.setTouchable(Touchable.disabled);

            label = new Label(scene, labelStyle);
            Label timeLabel = new Label(timestamp, labelStyle);
            Label dateLabel = new Label(date, labelStyle);

            slotTimestampLabels[index] = timeLabel;

            label.setFontScale(1.2f);
            timeLabel.setFontScale(1f);
            dateLabel.setFontScale(1f);

            labelTable.add(new Label(scene, labelStyle)).center().row();
            labelTable.add(dateLabel).center().row();
            labelTable.add(timeLabel).center();

            slotStack.add(labelTable);
        } else {
            label = new Label("Empty Slot", labelStyle);
            label.setFontScale(1.3f);
            label.setAlignment(Align.center);
            label.setTouchable(Touchable.disabled);
            slotTimestampLabels[index] = label;
            slotStack.add(label);
        }

        return slotStack;
    }

    private HoverableImageButton createButton(String up, String over, String disabledUp, String disabledOver) {
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.up = new TextureRegionDrawable(new Texture(up));
        style.over = new TextureRegionDrawable(new Texture(over));
        style.checked = style.down = style.over;

        Drawable dUp = new TextureRegionDrawable(new Texture(disabledUp));
        Drawable dOver = new TextureRegionDrawable(new Texture(disabledOver));

        return new HoverableImageButton(style, dUp, dOver);
    }

    private ImageButton createButton(String up, String over) {
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.up = new TextureRegionDrawable(new Texture(up));
        style.over = new TextureRegionDrawable(new Texture(over));

        ImageButton btn = new ImageButton(style);
        btn.addListener(new HoverSoundListener());
        return btn;
    }

    public class HoverableImageButton extends ImageButton {
        private final Drawable disabledUp, disabledOver;

        public HoverableImageButton(ImageButtonStyle style, Drawable disabledUp, Drawable disabledOver) {
            super(style);
            this.disabledUp = disabledUp;
            this.disabledOver = disabledOver;
        }

        @Override public void draw(Batch batch, float parentAlpha) {
            if (isDisabled()) {
                Vector2 pos = stage.screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
                Drawable d = hit(pos.x - getX(), pos.y - getY(), true) != null ? disabledOver : disabledUp;
                if (d != null) d.draw(batch, getX(), getY(), getWidth(), getHeight());
            } else super.draw(batch, parentAlpha);
        }
    }

    private void showSavedPopup() {
        Image dim = new Image(new Texture("popup/Black.png"));
        dim.setSize(1280, 720);
        dim.setColor(0, 0, 0, 0.5f);
        stage.addActor(dim);

        Label label = new Label("Game Saved!", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        label.setFontScale(1.5f);
        label.setAlignment(Align.center);
        label.setPosition(640, 360, Align.center);
        stage.addActor(label);

        dim.addAction(Actions.sequence(
            Actions.fadeIn(0.2f),
            Actions.delay(1.2f),
            Actions.fadeOut(0.3f),
            Actions.removeActor()
        ));
        label.addAction(Actions.sequence(
            Actions.fadeIn(0.2f),
            Actions.delay(1.2f),
            Actions.fadeOut(0.3f),
            Actions.removeActor()
        ));
    }

    private void showPopup(String msg) {
        Image dim = new Image(new Texture("popup/Black.png"));
        dim.setSize(1280, 720);
        dim.setColor(0, 0, 0, 0);
        stage.addActor(dim);

        Label label = new Label(msg, new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        label.setFontScale(1.5f);
        label.setAlignment(Align.center);
        label.setPosition(640, 360, Align.center);
        stage.addActor(label);

        dim.addAction(Actions.sequence(Actions.fadeIn(0.3f), Actions.delay(1.2f), Actions.fadeOut(0.3f), Actions.removeActor()));
        label.addAction(Actions.sequence(Actions.fadeIn(0.3f), Actions.delay(1.2f), Actions.fadeOut(0.3f), Actions.removeActor()));
    }

    private void showErrorOverlay(String msg) {
        Image dim = new Image(new Texture("popup/Black.png"));
        dim.setSize(1280, 720);
        dim.setColor(0, 0, 0, 0);
        stage.addActor(dim);

        Label label = new Label(msg, new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        label.setFontScale(2f);
        label.setAlignment(Align.center);
        label.setPosition(640, 360, Align.center);
        stage.addActor(label);

        dim.addAction(Actions.sequence(Actions.fadeIn(0.3f), Actions.delay(1.0f), Actions.fadeOut(0.3f), Actions.removeActor()));
        label.addAction(Actions.sequence(Actions.fadeIn(0.3f), Actions.delay(1.0f), Actions.fadeOut(0.3f), Actions.removeActor()));
    }

    private void showOverwriteConfirmation(Runnable onConfirm) {
        Image dim = new Image(new Texture("popup/Black.png"));
        dim.setSize(1280, 720);
        dim.setColor(0, 0, 0, 0.7f);
        stage.addActor(dim);

        Image box = new Image(new Texture("popup/Box.png"));
        box.setSize(500, 300);
        box.setPosition(640, 360, Align.center);
        stage.addActor(box);

        Label label = new Label("Overwrite this save?", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        label.setFontScale(1.5f);
        label.setAlignment(Align.center);
        label.setPosition(640, 460, Align.center);
        stage.addActor(label);

        ImageButton yes = createButton("popup/Yes1.png", "popup/Yes2.png");
        ImageButton no = createButton("popup/No1.png", "popup/No2.png");

        yes.setSize(100, 100);
        yes.setPosition(560, 300, Align.center);
        yes.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                AudioManager.playConfirmSound();
                dim.remove(); box.remove(); label.remove(); yes.remove(); no.remove();
                onConfirm.run();
            }
        });

        no.setSize(100, 100);
        no.setPosition(720, 300, Align.center);
        no.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                AudioManager.playRejectSound();
                dim.remove(); box.remove(); label.remove(); yes.remove(); no.remove();
            }
        });

        stage.addActor(yes);
        stage.addActor(no);
    }

    private void fadeToScreen(Screen next) {
        stage.addAction(Actions.sequence(Actions.fadeOut(0.3f), Actions.run(() -> game.setScreen(next))));
    }

    private void delayReload() {
        Timer.schedule(new Timer.Task() {
            @Override public void run() {
                Gdx.input.setInputProcessor(null);
                game.setScreen(new LoadGame(game, returnScreen));
            }
        }, 0.2f);
    }

    private String randomErrorMessage() {
        String[] msgs = {
            "...", "Huh...?", "Something's... off.", "This isn't right...",
            "Can't... Remember...", "Unavailable Memory...", "Have I been here before...?"
        };
        return msgs[(int)(Math.random() * msgs.length)];
    }

    @Override public void show() {}
    @Override public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }
    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { stage.dispose(); }
}
