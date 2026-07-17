package io.github.some_example_name.Menu;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.some_example_name.Menu.Others.AudioManager;
import io.github.some_example_name.Menu.Others.HoverSoundListener;

public class Options implements Screen {

    private ImageButton checkbox;
    private Slider textSpeedSlider, brightnessSlider, musicSlider, sfxSlider;

    private boolean fullscreenEnabled = false;

    private final Image brightnessOverlay;
    private final Preferences prefs = Gdx.app.getPreferences("VNOptions");
    private final Stage stage;
    private final Game game;
    private final Screen returnScreen;
    private final Array<Texture> disposableTextures = new Array<>();

    private int lastWindowWidth = 1280;
    private int lastWindowHeight = 720;

    public Options(Game game, Screen returnScreen) {
        this.game = game;
        this.returnScreen = returnScreen;
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        final int VIRTUAL_WIDTH = 1280;
        final int VIRTUAL_HEIGHT = 720;
        this.stage = new Stage(new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT));

        Gdx.input.setInputProcessor(stage);

        // === Brightness Overlay ===
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture overlayTexture = new Texture(pixmap);
        disposableTextures.add(overlayTexture);
        pixmap.dispose();

        brightnessOverlay = new Image(overlayTexture);
        brightnessOverlay.setSize(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        brightnessOverlay.setColor(0, 0, 0, 1 - prefs.getFloat("brightness", 0.5f));
        brightnessOverlay.setTouchable(null);
        stage.addActor(brightnessOverlay);
        brightnessOverlay.toFront();

        // === Table Root ===
        Table root = new Table();
        root.setFillParent(true);
        root.top().padTop(20);
        stage.addActor(root);

        // === Set Audio ===
        AudioManager.setMusicVolume(prefs.getFloat("music", 0.5f));
        AudioManager.setSFXVolume(prefs.getFloat("sfx", 0.5f));

        // === Title ===
        Texture titleTex = new Texture("options/OptionsLogo.png");
        disposableTextures.add(titleTex);
        Image titleImage = new Image(titleTex);
        titleImage.setSize(300, 100);
        root.add(titleImage).padBottom(40f).colspan(2).row();

        // === Sliders ===
        root.add(createSliderRow("TextSpeed.png", "textSpeed")).padBottom(15f).row();
        root.add(createSliderRow("Brightness.png", "brightness")).padBottom(15f).row();
        root.add(createSliderRow("Music.png", "music")).padBottom(15f).row();
        root.add(createSliderRow("SFX.png", "sfx")).padBottom(30f).row();

        // === Fullscreen ===
        Table fullscreenRow = new Table();
        Texture fullscreenTex = new Texture("options/Fullscreen.png");
        disposableTextures.add(fullscreenTex);
        Image fullscreenLabel = new Image(fullscreenTex);
        fullscreenLabel.setSize(240, 80);
        fullscreenRow.add(fullscreenLabel).width(240).height(80).padRight(15f);
        fullscreenRow.add(createFullscreenToggle()).size(50, 50);
        root.add(fullscreenRow).padBottom(30f).row();

        // === Bottom Buttons ===
        Table buttonRow = new Table();
        ImageButton creditsButton = createButton("options/Credits1.png", "options/Credits2.png");
        ImageButton toggleDefaults = createButton("options/ToggleDefaults1.png", "options/ToggleDefaults2.png");
        ImageButton backButton = createButton("options/Back1.png", "options/Back2.png");

        creditsButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                AudioManager.playConfirmSound();
                saveSettings();
                stage.addAction(Actions.sequence(
                    Actions.fadeOut(0.5f),
                    Actions.run(() -> game.setScreen(new Credits(game)))
                ));
            }
        });

        toggleDefaults.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                AudioManager.playConfirmSound();
                resetToDefaults();
            }
        });

        backButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                AudioManager.playConfirmSound();
                saveSettings();
                stage.addAction(Actions.sequence(
                    Actions.fadeOut(0.5f),
                    Actions.run(() -> game.setScreen(returnScreen != null ? returnScreen : new MainMenu(game)))
                ));
            }
        });

        buttonRow.add(creditsButton).size(200, 70).padRight(20f);
        buttonRow.add(toggleDefaults).size(200, 70).padRight(20f);
        buttonRow.add(backButton).size(200, 70);
        root.add(buttonRow).center();

        // === Fade In Effect ===
        stage.getRoot().getColor().a = 0;
        stage.addAction(Actions.fadeIn(0.5f));
    }

    private Table createSliderRow(String labelPath, String prefKey) {
        Table row = new Table();

        Texture labelTex = new Texture("options/" + labelPath);
        disposableTextures.add(labelTex);
        Image label = new Image(labelTex);
        label.setSize(200, 60);

        Slider slider = createCustomSlider();
        slider.setValue(prefs.getFloat(prefKey, 0.5f));

        switch (prefKey) {
            case "textSpeed":
                textSpeedSlider = slider;
                break;
            case "brightness":
                brightnessSlider = slider;
                break;
            case "music":
                musicSlider = slider;
                break;
            case "sfx":
                sfxSlider = slider;
                break;
        }


        // === Listeners ===
        if (prefKey.equals("brightness")) {
            slider.addListener(event -> {
                float alpha = 1 - slider.getValue();
                brightnessOverlay.setColor(0, 0, 0, alpha);
                return false;
            });
        }

        if (prefKey.equals("music")) {
            slider.addListener(event -> {
                AudioManager.setMusicVolume(slider.getValue());
                return false;
            });
        }

        if (prefKey.equals("sfx")) {
            final float[] lastPlayedValue = {slider.getValue()};
            final long[] lastPlayTime = {System.currentTimeMillis()};

            slider.addListener(event -> {
                float currentValue = slider.getValue();
                long now = System.currentTimeMillis();

                if (Math.abs(currentValue - lastPlayedValue[0]) >= 0.05f &&
                    now - lastPlayTime[0] >= 150) {

                    AudioManager.playConfirmSound();
                    lastPlayedValue[0] = currentValue;
                    lastPlayTime[0] = now;
                }
                AudioManager.setSFXVolume(currentValue);
                return false;
            });
        }

        row.add(label).width(200).height(60).padRight(10f);
        row.add(slider).width(400).height(50);
        return row;
    }

    private Slider createCustomSlider() {
        Slider.SliderStyle style = new Slider.SliderStyle();

        Texture bg = new Texture("options/SliderBar.png");
        Texture fill1 = new Texture("options/SliderBarFill1.png");
        Texture fill2 = new Texture("options/SliderBarFill2.png");
        Texture knob = new Texture("options/SliderKnob.png");

        disposableTextures.addAll(bg, fill1, fill2, knob);

        style.background = new TextureRegionDrawable(new TextureRegion(bg));
        style.knobBefore = new TextureRegionDrawable(new TextureRegion(fill1));
        style.knobAfter = new TextureRegionDrawable(new TextureRegion(fill2));

        TextureRegionDrawable knobDrawable = new TextureRegionDrawable(new TextureRegion(knob));
        knobDrawable.setMinSize(60, 60);
        style.knob = knobDrawable;

        return new Slider(0f, 1f, 0.01f, false, style);
    }

    private ImageButton createFullscreenToggle() {
        Texture box = new Texture("options/Box.png");
        Texture checked = new Texture("options/BoxCheck.png");
        disposableTextures.addAll(box, checked);

        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.imageUp = new TextureRegionDrawable(box);
        style.imageChecked = new TextureRegionDrawable(checked);

        checkbox = new ImageButton(style);
        fullscreenEnabled = prefs.getBoolean("fullscreen", false);
        checkbox.setChecked(fullscreenEnabled);

        boolean isCurrentlyFullscreen = Gdx.graphics.isFullscreen();

        if (fullscreenEnabled != isCurrentlyFullscreen) {
            toggleFullscreen(fullscreenEnabled);
        }

        checkbox.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                fullscreenEnabled = !fullscreenEnabled;
                checkbox.setChecked(fullscreenEnabled);
                toggleFullscreen(fullscreenEnabled);
            }
        });

        return checkbox;
    }

    private void toggleFullscreen(boolean enabled) {
        if (enabled) {
            lastWindowWidth = Gdx.graphics.getWidth();
            lastWindowHeight = Gdx.graphics.getHeight();
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        } else {
            Gdx.graphics.setWindowedMode(lastWindowWidth, lastWindowHeight);
        }
    }

    private ImageButton createButton(String upPath, String overPath) {
        Texture upTex = new Texture(upPath);
        Texture overTex = new Texture(overPath);
        disposableTextures.addAll(upTex, overTex);

        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.up = new TextureRegionDrawable(upTex);
        style.over = new TextureRegionDrawable(overTex);

        ImageButton button = new ImageButton(style);
        button.addListener(new HoverSoundListener());
        return button;
    }

    private void saveSettings() {
        prefs.putFloat("textSpeed", textSpeedSlider.getValue());
        prefs.putFloat("brightness", brightnessSlider.getValue());
        prefs.putFloat("music", musicSlider.getValue());
        prefs.putFloat("sfx", sfxSlider.getValue());
        prefs.putBoolean("fullscreen", fullscreenEnabled);
        prefs.flush();
    }

    private void resetToDefaults() {
        textSpeedSlider.setValue(0.5f);
        brightnessSlider.setValue(0.5f);
        musicSlider.setValue(0.5f);
        sfxSlider.setValue(0.5f);
        fullscreenEnabled = false;
        checkbox.setChecked(false);
        toggleFullscreen(false);
    }

    @Override public void show() {}
    @Override public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override public void dispose() {
        stage.dispose();
        for (Texture tex : disposableTextures) {
            tex.dispose();
        }
    }
}
