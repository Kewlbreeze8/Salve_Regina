package io.github.kewlbreeze8.Menu;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;

import io.github.kewlbreeze8.Menu.Others.AudioManager;
import io.github.kewlbreeze8.Menu.Others.HoverSoundListener;

import com.badlogic.gdx.scenes.scene2d.InputEvent;

public class Credits implements Screen {
    private boolean isAtBottom = false;
    private boolean isManualScroll = false;

    private final ScrollPane scrollPane;
    private final Skin skin;
    private final Stage stage;

    private float manualScrollCooldown = 0f;
    private float scrollStartDelay = 1f;
    private float bottomPauseTimer = 0f;

    public Credits(Game game) {
        this.skin = new Skin(Gdx.files.internal("data/uiskin.json"));
        this.stage = new Stage(new FitViewport(1280, 720));
        Gdx.input.setInputProcessor(stage);

        // === Root layout ===
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // === Logo ===
        Image logo = new Image(new Texture("options/CreditsLogo.png"));
        logo.setSize(300, 100); // Original size

        // === Content ===
        Table content = new Table();
        content.top().padTop(20f).padBottom(40f);
        String[] lines = {
            "A Visual Novel Horror Game by:",
            "Larry T. de Pedro",
            "Yves Rael C. Brillantes",
            "",
            "Story Writing:",
            "Larry T. de Pedro",
            "",
            "Art:",
            "UI/HUD elements:",
            "Yves Rael C. Brillantes",
            "",
            "Sprites and Animation:",
            "Larry T. de Pedro",
            "",
            "Music and Sound Effects:",
            "Larry T. de Pedro",
            "Ethel Cain",
            "",
            "Code:",
            "Yves Rael C. Brillantes",
            "Larry T. de Pedro",
            "",
            "Special Thanks:",
            "Clyde Chester R. Balaman",
            "Mapua Malayan Colleges Mindanao",
            "",
            "Made with LibGDX",
            "",
            "!! This project is made under a month and !!",
            "!! a submission for our Summative Assessment  !!"
        };
        for (String line : lines) {
            Label label = new Label(line, skin);
            label.setFontScale(1);
            label.setAlignment(1);
            content.add(label).padBottom(1f).center().row();
            content.defaults().width(800f);
        }

        // === ScrollPane ===
        scrollPane = new ScrollPane(content, skin);
        scrollPane.setScrollingDisabled(true, false);

        ScrollPane.ScrollPaneStyle transparentStyle = new ScrollPane.ScrollPaneStyle(scrollPane.getStyle());
        transparentStyle.background = null;
        scrollPane.setStyle(transparentStyle);

        // === Layout ===
        Table layout = new Table();
        layout.setFillParent(true);
        layout.top().padTop(20f);
        layout.add(logo).padBottom(5f).row();
        layout.add(scrollPane).expand().fill().padLeft(100f).padRight(100f).row();

        ImageButton backButton = createButton();
        backButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                stage.addAction(Actions.sequence(
                    Actions.fadeOut(0.5f),
                    Actions.run(() -> game.setScreen(new MainMenu(game)))
                ));
            }
        });
        layout.add(backButton).padTop(20f).bottom().center();
        stage.addActor(layout);

        // === Fade-in ===
        stage.getRoot().getColor().a = 0;
        stage.addAction(Actions.fadeIn(0.5f));

        // === Scroll wheel support ===
        InputMultiplexer multiplexer = new InputMultiplexer();

        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                isManualScroll = true;
                manualScrollCooldown = 3f; // duration before auto resumes
                float newScroll = scrollPane.getScrollY() + amountY * 30;
                newScroll = Math.max(0f, Math.min(newScroll, scrollPane.getMaxY()));
                scrollPane.setScrollY(newScroll);
                return true;
            }
        });

        multiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(multiplexer);

    }

    private ImageButton createButton() {
        TextureRegionDrawable up = new TextureRegionDrawable(new Texture("options/Back1.png"));
        TextureRegionDrawable over = new TextureRegionDrawable(new Texture("options/Back2.png"));

        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.up = up;
        style.over = over;

        ImageButton backButton = new ImageButton(style);  // create first

        backButton.addListener(new HoverSoundListener());
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.playConfirmSound();
            }
        });

        return backButton;
    }

    @Override public void show() {}

    @Override public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!isManualScroll) {
            if (scrollStartDelay > 0f) {
                scrollStartDelay -= delta;
            } else if (isAtBottom) {
                bottomPauseTimer -= delta;
                if (bottomPauseTimer <= 0f) {
                    scrollPane.setScrollY(0f); // loop to top
                    isAtBottom = false;
                }
            } else {
                float maxScroll = scrollPane.getMaxY();
                float currentScroll = scrollPane.getScrollY();
                float autoScrollSpeed = 20f;
                float newScroll = currentScroll + autoScrollSpeed * delta;

                if (newScroll >= maxScroll) {
                    scrollPane.setScrollY(maxScroll); // hold position at bottom
                    isAtBottom = true;
                    bottomPauseTimer = 1f; // 1 second pause
                } else {
                    scrollPane.setScrollY(newScroll);
                }
            }
        } else {
            manualScrollCooldown -= delta;
            if (manualScrollCooldown <= 0f) {
                isManualScroll = false;
                scrollStartDelay = 1f;  // delay restart
                isAtBottom = false;     // reset bottom state if user scrolled
            }
        }



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
        skin.dispose();
    }
}
