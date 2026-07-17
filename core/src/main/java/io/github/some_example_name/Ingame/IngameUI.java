// IngameUI.java
package io.github.some_example_name.Ingame;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.some_example_name.Ingame.IngameCrew.*;
import io.github.some_example_name.Ingame.Others.Choice;
import io.github.some_example_name.Ingame.Others.TextboxLayout;
import io.github.some_example_name.Ingame.Others.TextboxLayoutManager;

import java.util.List;
import java.util.function.Consumer;

public class IngameUI implements Screen {

    private final Stage stage;
    private final Skin skin;

    private final DialoguePanel dialoguePanel;
    private final ChoicePanel choicePanel;
    private final ButtonPanel buttonPanel;
    private final ToggleUIVisibilityButton toggleButton;
    private final Image backgroundImage;
    private final Image leftCharacterImage;
    private final Image centerCharacterImage;
    private final Image rightCharacterImage;
    private final Image fadeOverlay;

    private boolean uiVisible = true;
    private String currentTextboxStyle = "";

    public IngameUI(Game game, Skin skin) {
        this.skin = skin;
        this.stage = new Stage(new FitViewport(1280, 720));
        Gdx.input.setInputProcessor(stage);

        backgroundImage = createFullscreenImage();
        stage.addActor(backgroundImage);

        leftCharacterImage = createCharacterSlot(-300);
        centerCharacterImage = createCharacterSlot(0);
        rightCharacterImage = createCharacterSlot(300);
        stage.addActor(leftCharacterImage);
        stage.addActor(centerCharacterImage);
        stage.addActor(rightCharacterImage);

        dialoguePanel = new DialoguePanel();
        dialoguePanel.setSize(1240, 250);
        stage.addActor(dialoguePanel);

        choicePanel = new ChoicePanel();
        buttonPanel = new ButtonPanel();
        toggleButton = new ToggleUIVisibilityButton(this::toggleUIVisibility);

        setTextboxStyle("Textbox1");

        choicePanel.setTouchable(Touchable.childrenOnly);
        dialoguePanel.setTouchable(Touchable.childrenOnly);
        buttonPanel.setTouchable(Touchable.childrenOnly);
        toggleButton.setTouchable(Touchable.enabled);

        stage.addActor(choicePanel);
        stage.addActor(dialoguePanel);
        stage.addActor(buttonPanel);
        stage.addActor(toggleButton);

        fadeOverlay = createFadeOverlay();
        stage.addActor(fadeOverlay);
        fadeOverlay.toFront();
    }

    public void setBackground(String bgName) {
        String path = "ingame/backgrounds/" + bgName + ".png";
        try {
            Texture texture = new Texture(Gdx.files.internal(path));
            backgroundImage.setDrawable(new TextureRegionDrawable(new TextureRegion(texture)));
        } catch (Exception e) {
            System.err.println("[IngameUI] Failed to load background: " + path);
        }
    }

    public void clearBackground() {
        backgroundImage.setDrawable(null);
    }

    public void setCharacterSprite(String slot, String spriteName) {
        Image image;
        float offsetX;

        switch (slot.toLowerCase()) {
            case "left":
                image = leftCharacterImage;
                offsetX = -300;
                break;
            case "right":
                image = rightCharacterImage;
                offsetX = 300;
                break;
            default:
                image = centerCharacterImage;
                offsetX = 0;
                break;
        }

        if (image == null) return;

        if (spriteName == null || spriteName.isEmpty()) {
            animateCharacterSprite(image, false, slot, 0.4f);
            return;
        }

        String path = "ingame/sprites/" + spriteName + ".png";
        try {
            boolean wasEmpty = image.getDrawable() == null;

            Texture texture = new Texture(Gdx.files.internal(path));
            image.setDrawable(new TextureRegionDrawable(new TextureRegion(texture)));
            image.setSize(texture.getWidth(), texture.getHeight());
            image.setPosition(
                (1280f - texture.getWidth()) / 2f + offsetX,
                70f
            );

            if (wasEmpty) {
                animateCharacterSprite(image, true, slot, 0.4f);
            }

        } catch (Exception e) {
            System.err.println("[IngameUI] Failed to load sprite: " + path);
        }
    }

    public void clearCharacterSprite(String slot) {
        setCharacterSprite(slot, null);
    }

    private void animateCharacterSprite(Image image, boolean fadeIn, String slot, float duration) {
        image.clearActions();

        float startX = image.getX();
        float offsetX = slot.equalsIgnoreCase("left") ? -30f :
            slot.equalsIgnoreCase("right") ? 30f : 0f;

        if (fadeIn) {
            image.getColor().a = 0f;
            image.setPosition(startX + offsetX, image.getY());
            image.addAction(
                Actions.parallel(
                    Actions.fadeIn(duration),
                    Actions.moveTo(startX, image.getY(), duration)
                )
            );
        } else {
            // 🔧 Disable fadeOut animation for center slot
            if (slot.equalsIgnoreCase("center")) {
                image.setDrawable(null);
                return;
            }

            // Normal fade-out animation for left/right
            image.addAction(
                Actions.sequence(
                    Actions.parallel(
                        Actions.fadeOut(duration),
                        Actions.moveTo(startX + offsetX, image.getY(), duration)
                    ),
                    Actions.run(() -> image.setDrawable(null))
                )
            );
        }
    }

    public void setTextboxStyle(String styleName) {
        if (!styleName.equals(currentTextboxStyle)) {
            currentTextboxStyle = styleName;
            dialoguePanel.setTextbox(styleName);
            applyTextboxLayout(styleName);
        }
    }

    public void clearTextboxStyle() {
        setTextboxStyle("Textbox1");
    }

    public void setDialogueBoxCentered(boolean centered) {
        if (centered) {
            dialoguePanel.setPosition(
                (stage.getWidth() - dialoguePanel.getWidth()) / 2f,
                (stage.getHeight() - dialoguePanel.getHeight()) / 2f
            );
        } else {
            applyTextboxLayout(currentTextboxStyle);
        }
    }

    public void showDialogue(String speaker, String dialogue, String textboxStyle) {
        dialoguePanel.setDialogue(dialogue);
        if (textboxStyle != null && !textboxStyle.isEmpty()) {
            dialoguePanel.setTextbox(textboxStyle);
        }
    }

    public void setFontColor(String hexColor) {
        dialoguePanel.setFontColor(hexColor);
    }

    public void resetFontColor() {
        dialoguePanel.resetFontColor();
    }

    private void applyTextboxLayout(String id) {
        TextboxLayout layout = TextboxLayoutManager.getLayout(id);
        if (layout != null) {
            dialoguePanel.setPosition(layout.x, layout.y);
            dialoguePanel.setTextAlignment(layout.align);
        }
    }

    public void showChoices(List<Choice> choices, Consumer<Choice> callback, int choiceIndex) {
        choicePanel.showChoices(choices, callback, choiceIndex);
    }

    private void toggleUIVisibility() {
        uiVisible = !uiVisible;
        dialoguePanel.setVisible(uiVisible);
        buttonPanel.setVisible(uiVisible);
        choicePanel.setVisible(uiVisible);
    }

    public Skin getSkin() { return skin; }

    public Stage getStage() { return stage; }

    public ButtonPanel getButtonPanel() { return buttonPanel; }

    public Image getFadeOverlay() { return fadeOverlay; }

    private Image createFullscreenImage() {
        Image img = new Image();
        img.setFillParent(true);
        img.setTouchable(Touchable.disabled);
        return img;
    }

    private Image createCharacterSlot(float offsetX) {
        Image image = new Image();
        image.setTouchable(Touchable.disabled);
        image.setOrigin(Align.center);
        image.setPosition(
            (1280f - image.getWidth()) / 2f + offsetX,
            70f
        );
        return image;
    }

    private Image createFadeOverlay() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 1);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        Image fade = new Image(new TextureRegionDrawable(new TextureRegion(texture)));
        fade.setSize(stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
        fade.setColor(0, 0, 0, 0);
        fade.setTouchable(Touchable.disabled);
        return fade;
    }

    public void fadeOut(float duration) {
        System.out.println("[UI] fadeOut triggered. Duration: " + duration + "s");
        // TODO: Add fade logic (alpha -> 1 over time)
    }

    public void fadeIn(float duration) {
        System.out.println("[UI] fadeIn triggered. Duration: " + duration + "s");
        // TODO: Add fade logic (alpha -> 0 over time)
    }

    @Override public void show() {}
    @Override public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        dialoguePanel.dispose();
        toggleButton.dispose();
    }
}
