package io.github.some_example_name.Ingame.IngameCrew;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import java.util.HashMap;
import java.util.Map;

public class DialoguePanel extends Group implements com.badlogic.gdx.utils.Disposable {
    private final Skin skin;
    private final Image textboxImage;
    private final Label dialogueLabel;

    private final Map<String, Texture> textboxTextureCache = new HashMap<>();
    private String currentTextboxPath = "";
    private Texture currentTextboxTexture = null;

    public DialoguePanel() {
        setBounds(0, 0, 1280, 720);
        skin = new Skin(Gdx.files.internal("data/uiskin.json"));

        // 🧱 Make textbox span nearly full width (like a visual novel style)
        textboxImage = new Image();
        textboxImage.setSize(1240, 250); // Almost full width (40px margin)
        textboxImage.setPosition((1280 - 1240) / 5f, 30); // Centered horizontally, bottom padding
        addActor(textboxImage);

        // 💬 Dialogue text inside the box
        dialogueLabel = new Label("", skin);
        dialogueLabel.setWrap(true);
        dialogueLabel.setWidth(1150); // Slight padding inside textbox
        dialogueLabel.setAlignment(Align.left);
        dialogueLabel.setPosition(textboxImage.getX() + 50, textboxImage.getY() + 125);
        dialogueLabel.setColor(Color.WHITE);
        addActor(dialogueLabel);

        setColor(1, 1, 1, 0); // Start transparent
        addAction(Actions.fadeIn(0.3f)); // Smooth entrance
    }

    public void setDialogue(String text) {
        dialogueLabel.setText(text != null ? text : "");
    }

    public void setTextbox(String texturePath) {
        Gdx.app.log("DialoguePanel", "Requested textbox: " + texturePath);

        if (texturePath.equals(currentTextboxPath)) {
            Gdx.app.log("DialoguePanel", "Textbox already set to: " + texturePath);
            return;
        }

        if (currentTextboxTexture != null) {
            currentTextboxTexture.dispose();
        }

        try {
            Texture newTexture = new Texture(Gdx.files.internal("ingame/textbox/" + texturePath + ".png"));
            TextureRegion region = new TextureRegion(newTexture);

            // 🧠 Force update no matter what
            textboxImage.clear();
            textboxImage.setDrawable(new TextureRegionDrawable(region));
            textboxImage.setSize(region.getRegionWidth(), region.getRegionHeight());
            textboxImage.invalidate(); // Force re-layout

            currentTextboxTexture = newTexture;
            currentTextboxPath = texturePath;

            Gdx.app.log("DialoguePanel", "Textbox image set successfully: " + texturePath);
        } catch (Exception e) {
            Gdx.app.error("DialoguePanel", "Failed to load textbox image: " + texturePath, e);
        }
    }

    public void setTextAlignment(String alignment) {
        if (alignment == null) return;

        switch (alignment.toLowerCase()) {
            case "center":
                dialogueLabel.setAlignment(Align.center);
                break;
            case "right":
                dialogueLabel.setAlignment(Align.right);
                break;
            default:
                dialogueLabel.setAlignment(Align.left);
                break;
        }
    }

    public void setFontColor(String hex) {
        try {
            dialogueLabel.setColor(Color.valueOf(hex));
        } catch (Exception e) {
            System.err.println("[ERROR] Invalid hex color: " + hex);
        }
    }

    public void resetFontColor() {
        setFontColor("#FFFFFF"); // or whatever your default is
    }



    @Override
    public void dispose() {
        if (currentTextboxTexture != null) currentTextboxTexture.dispose();
        skin.dispose();
    }
}
