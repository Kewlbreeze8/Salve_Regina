package io.github.kewlbreeze8.Ingame.IngameCrew;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import io.github.kewlbreeze8.Menu.Others.AudioManager;
import io.github.kewlbreeze8.Menu.Others.HoverSoundListener;

public class ButtonPanel extends Group {

    private final Skin skin;

    public final ImageButton historyButton;
    public final ImageButton skipButton;
    public final ImageButton autoButton;
    public final ImageButton saveLoadButton;
    public final ImageButton optionsButton;
    public final ImageButton mainMenuButton;

    private boolean isAutoToggled = false;
    private boolean isSkipToggled = false;


    public ButtonPanel() {
        skin = new Skin(Gdx.files.internal("data/uiskin.json"));
        setBounds(0, 0, 1280, 100);
        setTouchable(Touchable.childrenOnly);

        Table buttonTable = new Table();
        buttonTable.setFillParent(true);
        buttonTable.bottom().pad(10);

        historyButton = createImageButton("History");
        skipButton = createImageButton("Skip");
        autoButton = createImageButton("Auto");
        saveLoadButton = createImageButton("SaveLoad");
        optionsButton = createImageButton("Options");
        mainMenuButton = createImageButton("MainMenu");

        buttonTable.add(historyButton).padRight(10);
        buttonTable.add(skipButton).padRight(10);
        buttonTable.add(autoButton).padRight(10);
        buttonTable.add(saveLoadButton).padRight(10);
        buttonTable.add(optionsButton).padRight(10);
        buttonTable.add(mainMenuButton);

        addActor(buttonTable);

//        buttonTable.debug();
//        this.debug();
    }

    private ImageButton createImageButton(String name) {
        Texture upTexture = new Texture(Gdx.files.internal("ingame/buttons/" + name + "1.png"));
        Texture overTexture = new Texture(Gdx.files.internal("ingame/buttons/" + name + "2.png"));

        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.up = new TextureRegionDrawable(upTexture);
        style.over = new TextureRegionDrawable(overTexture);

        ImageButton button = new ImageButton(style);
        button.setSize(125, 50);

        button.getColor().a = 0f;
        button.addAction(Actions.fadeIn(0.4f));

        button.addListener(new HoverSoundListener()); // For hover w/ cooldown
        if (name.equals("Auto") || name.equals("Skip")) {
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    AudioManager.playConfirmSound();

                    // Toggle logic
                    if (name.equals("Auto")) {
                        isAutoToggled = !isAutoToggled;
                        updateAutoVisual(button, isAutoToggled);
                    } else {
                        isSkipToggled = !isSkipToggled;
                        updateSkipVisual(button, isSkipToggled);
                    }

                    System.out.println("[CLICKED] " + name);
                }

                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    if ((name.equals("Auto") && !isAutoToggled) || (name.equals("Skip") && !isSkipToggled)) {
                        button.getStyle().imageUp = button.getStyle().over;
                    }
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    if ((name.equals("Auto") && !isAutoToggled) || (name.equals("Skip") && !isSkipToggled)) {
                        button.getStyle().imageUp = button.getStyle().up;
                    }
                }
            });
        } else {
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    AudioManager.playConfirmSound();
                    System.out.println("[CLICKED] " + name);
                }
            });
        }


        return button;
    }

    private void updateAutoVisual(ImageButton button, boolean toggled) {
        button.getStyle().imageUp = toggled ? button.getStyle().over : button.getStyle().up;
    }

    private void updateSkipVisual(ImageButton button, boolean toggled) {
        button.getStyle().imageUp = toggled ? button.getStyle().over : button.getStyle().up;
    }

    // Accessors for IngameController. !!! DO NOT CHANGE !!!
    public ImageButton getHistoryButton() { return historyButton; }
    public ImageButton getSkipButton() { return skipButton; }
    public ImageButton getAutoButton() { return autoButton; }
    public ImageButton getSaveLoadButton() { return saveLoadButton; }
    public ImageButton getOptionsButton() { return optionsButton; }
    public ImageButton getMainMenuButton() { return mainMenuButton; }
}
