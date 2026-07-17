package io.github.Kewlbreeze8.Menu.Others;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.Kewlbreeze8.Menu.MainMenu;

public class WarningScreen implements Screen {

    private final Game game;
    private Stage stage;
    private Label warningLabel;
    private Label proceedLabel;
    private Skin skin;

    private String fullText = "!!! Warning !!!\nThis game contains themes of personal trauma, family abuse, and disturbing languages. \nPlayer discretion is advised.";
    private StringBuilder currentText = new StringBuilder();

    private float charTimer = 0f;
    private float charInterval = 0.04f;
    private int charIndex = 0;
    private boolean finishedTyping = false;

    private Sound confirmSound = null;
    private int soundInterval = 2;

    private Image fadeOverlay;

    public WarningScreen(Game game) {
        this.game = game;

        // 🔐 Safe sound check
        if (Assets.manager.isLoaded("sound/effect/Confirm.wav")) {
            confirmSound = Assets.manager.get("sound/effect/Confirm.wav", Sound.class);
        }

        setupStage();
    }

    private void setupStage() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("data/uiskin.json"));

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = skin.getFont("default-font");
        labelStyle.font.getData().setScale(1.5f);

        warningLabel = new Label("", labelStyle);
        warningLabel.setAlignment(Align.center);

        proceedLabel = new Label("Click to proceed", labelStyle);
        proceedLabel.setAlignment(Align.center);
        proceedLabel.getColor().a = 0f;

        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.add(warningLabel).expand().padBottom(40f).row();
        table.add(proceedLabel).padTop(10f).padBottom(100f);

        stage.addActor(table);

        // 🖤 Black overlay for fade transitions
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        Texture blackTexture = new Texture(pixmap);
        pixmap.dispose();

        fadeOverlay = new Image(blackTexture);
        fadeOverlay.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        fadeOverlay.getColor().a = 1f; // start fully black
        fadeOverlay.addAction(Actions.fadeOut(1f)); // fade in when screen starts

        stage.addActor(fadeOverlay);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        if (!finishedTyping) {
            charTimer += delta;
            if (charTimer >= charInterval && charIndex < fullText.length()) {
                charTimer = 0f;
                currentText.append(fullText.charAt(charIndex));
                warningLabel.setText(currentText.toString());

                // 💬 Play blip sound if loaded and char is not a space
                if (confirmSound != null &&
                    charIndex % soundInterval == 0 &&
                    fullText.charAt(charIndex) != ' ') {
                    confirmSound.play(0.3f);
                }

                charIndex++;
            } else if (charIndex >= fullText.length()) {
                finishedTyping = true;
                proceedLabel.addAction(Actions.fadeIn(1.5f));
            }
        } else if (Gdx.input.justTouched() && fadeOverlay.getActions().size == 0) {
            // 👋 Fade to main menu
            fadeOverlay.addAction(Actions.sequence(
                Actions.fadeIn(1f),
                Actions.run(() -> game.setScreen(new MainMenu(game)))
            ));
        }

        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
