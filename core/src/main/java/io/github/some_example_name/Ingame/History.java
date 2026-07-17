package io.github.some_example_name.Ingame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.some_example_name.Menu.Others.AudioManager;
import io.github.some_example_name.Menu.Others.HoverSoundListener;

import java.util.List;

public class History implements Screen {
    private Texture backgroundTexture;
    private SpriteBatch batch; // add this at the top
    private Table rootTable;

    private final Stage stage;
    private final Skin skin;
    private final IngameController ingame;
    private final Game game;

    public History(Game game, IngameController ingame) {
        this.game = game;
        this.ingame = ingame;
        this.skin = ingame.getUI().getSkin();

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        buildUI();
    }

    private void buildUI() {
        rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.pad(20);
        stage.addActor(rootTable);

        // === Logo ===
        Texture logoTexture = new Texture(Gdx.files.internal("ingame/HistoryLogo.png"));
        Image logo = new Image(logoTexture);

        // Prevent layout from stretching the image
        logo.setScaling(Scaling.none); // This disables auto-resizing

        // Wrap it in a Container so the layout doesn’t distort it
        Container<Image> logoContainer = new Container<>(logo);
        logoContainer.size(300, 100);
        logoContainer.maxSize(300, 100);
        logoContainer.minSize(300, 100);

        rootTable.add(logoContainer).padBottom(10f).row();

        // === Scroll Content ===
        Table scrollContent = new Table().top().left();
        scrollContent.defaults().expandX().fillX().pad(10);
        ScrollPane scrollPane = new ScrollPane(scrollContent, skin);
        scrollPane.setFadeScrollBars(true);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setScrollbarsOnTop(true);
        scrollPane.layout();

        Gdx.app.postRunnable(() -> scrollPane.setScrollPercentY(1f));
        populateHistory(scrollContent); // Fill the scroll area with content

        // === Back Button (Image Style) ===
        ImageButton backButton = createBackButton();
        backButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.playConfirmSound();
                game.setScreen(ingame);
            }
        });

        rootTable.add(scrollPane).expand().fill().padTop(10).padLeft(100).padRight(100).row();
        rootTable.add(backButton).padTop(20f).bottom().center();
    }

    private void populateHistory(Table scrollContent) {
        List<String> printedLines = StoryManager.getPrintedLines();
        if (printedLines == null || printedLines.isEmpty()) {
            Label empty = new Label("No history available.", skin);
            empty.setColor(1f, 1f, 1f, 0.5f);
            empty.setFontScale(1.1f);
            scrollContent.add(empty).pad(10).row();
            return;
        }

        for (String raw : printedLines) {
            DialogueLine line = new DialogueLine(raw);

            Label label = new Label(line.toString(), skin);
            label.setWrap(true);
            label.setAlignment(Align.left);
            label.setFontScale(1.25f);
            scrollContent.add(label).expandX().fillX().padBottom(10).padLeft(20).padRight(20).row();
        }
    }

    private ImageButton createBackButton() {
        TextureRegionDrawable up = new TextureRegionDrawable(new Texture("options/Back1.png"));
        TextureRegionDrawable over = new TextureRegionDrawable(new Texture("options/Back2.png"));

        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.up = up;
        style.over = over;

        ImageButton backButton = new ImageButton(style);
        backButton.addListener(new HoverSoundListener());
        return backButton;
    }

    @Override public void show() {
        batch = new SpriteBatch();
        backgroundTexture = new Texture(Gdx.files.internal("menu/Background.png"));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw background
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();

        stage.act(delta);
        stage.draw();

    }


    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);

        // Recalculate layout properly
        if (rootTable != null) {
            rootTable.invalidateHierarchy();
        }
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (batch != null) batch.dispose();
        stage.dispose();
    }

    public static class DialogueLine {
        public String speaker;
        public String content;
        public boolean isNarration;

        public DialogueLine(String rawLine) {
            try {
                JsonValue json = new JsonReader().parse(rawLine);
                this.content = json.getString("text", "");
                this.speaker = json.getString("speaker", "Narrator").trim();
                this.isNarration = speaker.equalsIgnoreCase("Narrator") || speaker.equals("???");
            } catch (Exception e) {
                if (rawLine != null && rawLine.startsWith("[") && rawLine.contains("]")) {
                    int endIndex = rawLine.indexOf("]");
                    this.speaker = rawLine.substring(1, endIndex).trim();
                    this.content = rawLine.substring(endIndex + 1).trim();
                    this.isNarration = speaker.equalsIgnoreCase("Narrator") || speaker.equals("???");
                } else {
                    this.speaker = "Narrator";
                    this.content = rawLine != null ? rawLine.trim() : "";
                    this.isNarration = true;
                }
            }
        }

        @Override
        public String toString() {
            if (speaker.equals("???")) {
                return content; // mysterious lines stay unlabeled
            }
            return "[" + speaker + "] " + content;
        }
    }
}

