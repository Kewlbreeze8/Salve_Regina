package io.github.Kewlbreeze8.Menu;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;

import io.github.Kewlbreeze8.Ingame.IngameController;
import io.github.Kewlbreeze8.Ingame.StoryManager;
import io.github.Kewlbreeze8.Menu.Others.*;

public class MainMenu implements Screen {

    private final Game game;

    private final boolean cameFromIngame;

    private OrthographicCamera camera;
    private FitViewport viewport;
    private SpriteBatch batch;
    private Stage stage;
    private Skin skin;

    private Texture backgroundTexture;
    private Texture girlTexture1;
    private Texture girlTexture2;
    private Texture titleTexture;

    private Animation<TextureRegion> girlAnimation;
    private float stateTime = 0f;

    private Image fadeOverlay;
    private ImageButton continueButton;

    private boolean continueCheckDone = false;
    private boolean transitionInProgress = false;

    private Stack dialogGroup = new Stack();

    private float buttonWidth;
    private float buttonHeight;

    public MainMenu(Game game) {
        this(game, false); // Default: came from anywhere but Ingame
    }

    public MainMenu(Game game, boolean cameFromIngame) {
        this.game = game;
        this.cameFromIngame = cameFromIngame;

        initCameraAndViewport();
        loadAssetsAndSetupAudio();
        setupStageAndInput();
        setupTexturesAndAnimation();
        setupUI();
        setupFadeOverlay();
    }

    private void initCameraAndViewport() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        camera.position.set(1280 / 2f, 720 / 2f, 0);
        camera.update();
    }

    private void loadAssetsAndSetupAudio() {
        Assets.loadMenuAssets();
        Assets.manager.finishLoading();
        AudioManager.loadSavedVolumes();
        AudioManager.load();
    }

    private void setupStageAndInput() {
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);
        batch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("data/uiskin.json"));
    }

    private void setupTexturesAndAnimation() {
        backgroundTexture = Assets.manager.get("menu/Background.png", Texture.class);
        titleTexture = Assets.manager.get("menu/Title.png", Texture.class);
        girlTexture1 = Assets.manager.get("menu/ElsaMainMenu1.png", Texture.class);
        girlTexture2 = Assets.manager.get("menu/ElsaMainMenu2.png", Texture.class);

        TextureRegion[] frames = {
            new TextureRegion(girlTexture1),
            new TextureRegion(girlTexture2)
        };
        girlAnimation = new Animation<>(0.75f, frames);
        girlAnimation.setPlayMode(Animation.PlayMode.LOOP);
    }

    private void setupUI() {
        // Load button textures
        Texture newUp = Assets.manager.get("menu/buttons/NewGame1.png", Texture.class);
        Texture newHover = Assets.manager.get("menu/buttons/NewGame2.png", Texture.class);
        Texture loadUp = Assets.manager.get("menu/buttons/LoadGame1.png", Texture.class);
        Texture loadHover = Assets.manager.get("menu/buttons/LoadGame2.png", Texture.class);
        Texture optUp = Assets.manager.get("menu/buttons/Options1.png", Texture.class);
        Texture optHover = Assets.manager.get("menu/buttons/Options2.png", Texture.class);
        Texture exitUp = Assets.manager.get("menu/buttons/Exit1.png", Texture.class);
        Texture exitHover = Assets.manager.get("menu/buttons/Exit2.png", Texture.class);
        Texture continueUp = Assets.manager.get("menu/buttons/Continue1.png", Texture.class);
        Texture continueHover = Assets.manager.get("menu/buttons/Continue2.png", Texture.class);

        // Create buttons
        ImageButton newButton = makeButton(newUp, newHover);
        ImageButton loadButton = makeButton(loadUp, loadHover);
        ImageButton optionsButton = makeButton(optUp, optHover);
        ImageButton exitButton = makeButton(exitUp, exitHover);
        continueButton = makeButton(continueUp, continueHover);
        continueButton.setVisible(false);

        setupButtonListeners(newButton, loadButton, optionsButton, exitButton);

        this.buttonWidth = viewport.getWorldWidth() * 0.22f;
        this.buttonHeight = viewport.getWorldHeight() * 0.12f;
        float spacing = 40f;

        Table leftCol = new Table();
        leftCol.add(newButton).size(buttonWidth, buttonHeight).padBottom(spacing).row();
        leftCol.add(optionsButton).size(buttonWidth, buttonHeight);

        Table rightCol = new Table();
        rightCol.add(loadButton).size(buttonWidth, buttonHeight).padBottom(spacing).row();
        rightCol.add(exitButton).size(buttonWidth, buttonHeight);

        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.bottom().padBottom(viewport.getWorldHeight() * 0.125f);
        mainTable.add().width(viewport.getWorldWidth() * 0.12f);
        mainTable.add(leftCol).width(buttonWidth);
        mainTable.add().expandX();
        mainTable.add(rightCol).width(buttonWidth);
        mainTable.add().width(viewport.getWorldWidth() * 0.12f);

        Table continueTable = new Table();
        continueTable.setFillParent(true);
        continueTable.bottom().padBottom(viewport.getWorldHeight() * 0.05f);
        continueTable.add(continueButton).center().size(buttonWidth, buttonHeight);

        stage.addActor(mainTable);
        stage.addActor(continueTable);
    }

    private void setupButtonListeners(ImageButton newButton, ImageButton loadButton, ImageButton optionsButton, ImageButton exitButton) {
        newButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Load story BEFORE saving or launching the scene
                StoryManager.loadFromJson("script/Chapter0.json"); // Or your actual path
                StoryManager.resetPrintedLines();

                String startNode = StoryManager.getStartNodeId();
                SaveManager.save("1", "scene", startNode);
                SaveManager.save("1", "dialogueIndex", 0);

                AudioManager.playConfirmSound();
                AudioManager.fadeOutAndStopMenuMusic();

                fadeOverlay.addAction(Actions.sequence(
                    Actions.fadeIn(0.5f),
                    Actions.run(() -> game.setScreen(new IngameController(game, startNode, 0)))
                ));
            }
        });

        loadButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                AudioManager.playConfirmSound();
                fadeOverlay.addAction(Actions.sequence(
                    Actions.fadeIn(0.5f),
                    Actions.run(() -> game.setScreen(new LoadGame(game, MainMenu.this)))
                ));
            }
        });

        optionsButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                AudioManager.playConfirmSound();
                fadeOverlay.addAction(Actions.sequence(
                    Actions.fadeIn(0.5f),
                    Actions.run(() -> game.setScreen(new Options(game, MainMenu.this)))
                ));
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                AudioManager.playConfirmSound();
                showExitConfirmation();
            }
        });

        continueButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showContinuePopup();
            }
        });

    }

    private ImageButton makeButton(Texture upTex, Texture hoverTex) {
        TextureRegionDrawable up = new TextureRegionDrawable(new TextureRegion(upTex));
        TextureRegionDrawable over = new TextureRegionDrawable(new TextureRegion(hoverTex));

        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.up = up;
        style.over = over;

        ImageButton button = new ImageButton(style);

        button.addListener(new ClickListener() {
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

        return button;
    }

    private void setupFadeOverlay() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 1);
        pixmap.fill();
        Texture blackTexture = new Texture(pixmap);
        pixmap.dispose();

        fadeOverlay = new Image(new TextureRegionDrawable(new TextureRegion(blackTexture)));
        fadeOverlay.setSize(viewport.getWorldWidth(), viewport.getWorldHeight());
        fadeOverlay.setColor(0, 0, 0, 1);
        fadeOverlay.setTouchable(Touchable.disabled);
        stage.addActor(fadeOverlay);
    }

    private void showExitConfirmation() {
        // 🔒 Block all input behind the popup
        Actor inputBlocker = createInputBlocker();
        stage.addActor(inputBlocker); // Add blocker before the popup

        // 📦 Popup box
        Texture boxTexture = Assets.manager.get("popup/Box.png", Texture.class);
        Image boxBackground = new Image(boxTexture);
        boxBackground.setSize(500, 300);

        // 📝 Label setup
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = skin.getFont("default-font");

        Label message = new Label("Are you sure?", labelStyle);
        message.setFontScale(1.5f);
        message.setAlignment(Align.center);

        // ✅/❌ Buttons
        Texture yesUp = Assets.manager.get("popup/Yes1.png", Texture.class);
        Texture yesOver = Assets.manager.get("popup/Yes2.png", Texture.class);
        Texture noUp = Assets.manager.get("popup/No1.png", Texture.class);
        Texture noOver = Assets.manager.get("popup/No2.png", Texture.class);

        ImageButton yesButton = makeButton(yesUp, yesOver);
        ImageButton noButton = makeButton(noUp, noOver);

        yesButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.playConfirmSound();
                dialogGroup.addAction(Actions.sequence(
                    Actions.fadeOut(0.1f),
                    Actions.run(() -> fadeOverlay.addAction(Actions.sequence(
                        Actions.fadeIn(0.5f),
                        Actions.delay(0.1f),
                        Actions.run(() -> Gdx.app.exit())
                    )))
                ));
            }
        });

        noButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.playRejectSound();
                dialogGroup.remove(); // Remove the dialog
                inputBlocker.remove(); // 🧼 Remove the blocker too
            }
        });

        addHoverSound(yesButton);
        addHoverSound(noButton);

        // 🔧 Layout
        Table content = new Table();
        content.setFillParent(true);
        content.pad(30f);
        content.add(message).colspan(2).expandX().center().padBottom(30f).row();
        content.add(yesButton).size(100, 100).padRight(40f);
        content.add(noButton).size(100, 100);

        dialogGroup.setSize(500, 300);
        dialogGroup.clear(); // Clean previous contents, just in case
        dialogGroup.add(boxBackground);
        dialogGroup.add(content);

        dialogGroup.setPosition(
            (viewport.getWorldWidth() - dialogGroup.getWidth()) / 2f,
            (viewport.getWorldHeight() - dialogGroup.getHeight()) / 2f
        );

        stage.addActor(dialogGroup);
    }

    private void addHoverSound(Actor button) {
        button.addListener(new ClickListener() {
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

    private void showContinuePopup() {
        Texture boxTexture = Assets.manager.get("popup/Box.png", Texture.class);
        Image boxBackground = new Image(boxTexture);
        boxBackground.setSize(500, 300);

        Label message = new Label("Continue from your last save?", skin);
        message.setFontScale(1.3f);
        message.setAlignment(Align.center);

        Texture yesUp = Assets.manager.get("popup/Yes1.png", Texture.class);
        Texture yesOver = Assets.manager.get("popup/Yes2.png", Texture.class);
        Texture noUp = Assets.manager.get("popup/No1.png", Texture.class);
        Texture noOver = Assets.manager.get("popup/No2.png", Texture.class);

        ImageButton yesButton = makeButton(yesUp, yesOver);
        ImageButton noButton = makeButton(noUp, noOver);

        yesButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.playConfirmSound();

                // Clean up popup before transition
                dialogGroup.remove();

                fadeOverlay.addAction(Actions.sequence(
                    Actions.fadeIn(0.5f),
                    Actions.run(() -> game.setScreen(new LoadGame(game, MainMenu.this)))
                ));
            }
        });

        noButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
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

        dialogGroup.clear();
        dialogGroup.setSize(500, 300);
        dialogGroup.add(boxBackground);
        dialogGroup.add(content);

        dialogGroup.setPosition(
            (viewport.getWorldWidth() - dialogGroup.getWidth()) / 2f,
            (viewport.getWorldHeight() - dialogGroup.getHeight()) / 2f
        );

        stage.addActor(dialogGroup);
    }

    private String getMostRecentValidSlot() {
        for (int i = 1; i <= 5; i++) {
            String slotId = "slot" + i;
            String scene = SaveManager.getString(slotId, "scene");
            if (scene != null && !scene.isEmpty()) {
                return slotId;
            }
        }
        return null;
    }

    private Actor createInputBlocker() {
        Actor blocker = new Actor();
        blocker.setSize(viewport.getWorldWidth(), viewport.getWorldHeight());
        blocker.setTouchable(Touchable.enabled); // Eats all input
        return blocker;
    }

    @Override public void show() {
        Gdx.input.setInputProcessor(stage);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        AudioManager.playMenuMusic();
        fadeOverlay.getColor().a = 1;
        fadeOverlay.addAction(Actions.fadeOut(0.5f));
        continueButton.setVisible(false);
        continueCheckDone = false;

        String recentSlot = getMostRecentValidSlot();
        continueButton.setVisible(recentSlot != null);
    }

    @Override public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float vw = viewport.getWorldWidth();
        float vh = viewport.getWorldHeight();
        stateTime += delta;

        if (!continueCheckDone && !cameFromIngame) {
            continueCheckDone = true;

            String validSlot = getMostRecentValidSlot();
            if (validSlot != null) {
                continueButton.setVisible(true);
                continueButton.clearActions();
                continueButton.getColor().a = 0f;
                continueButton.addAction(Actions.fadeIn(1f));

                // Optionally store this for use in the continueButton's click handler
                continueButton.setUserObject(validSlot);
            }
        }

        TextureRegion frame = girlAnimation.getKeyFrame(stateTime, true);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, vw, vh);
        batch.draw(frame, (vw - 500) / 2f, (vh - 700) / 2f - 50, 500, 700);
        batch.draw(titleTexture, (vw - 500) / 2f, vh - 220, 500, 200);
        batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override public void dispose() {
        batch.dispose();
        stage.dispose();
        skin.dispose();
    }
}
