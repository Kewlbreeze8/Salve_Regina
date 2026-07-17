package io.github.some_example_name.Ingame.IngameCrew;

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
import com.badlogic.gdx.utils.Align;
import io.github.some_example_name.Ingame.Others.Choice;
import io.github.some_example_name.Menu.Others.AudioManager;
import io.github.some_example_name.Menu.Others.HoverSoundListener;

import java.util.List;
import java.util.function.Consumer;

public class ChoicePanel extends Group {

    private final Table choiceTable;
    private final Skin skin;

    public ChoicePanel() {
//        this.debug();           // Optional: show ChoicePanel bounds

        setBounds(0, 0, 1280, 720);
        setTouchable(Touchable.childrenOnly);

        skin = new Skin(Gdx.files.internal("data/uiskin.json"));

        choiceTable = new Table();
        choiceTable.setTouchable(Touchable.enabled);
        choiceTable.setVisible(false);
        choiceTable.setSize(1280, 300); // full width of the screen
        choiceTable.setPosition(0, 100); // position near bottom, adjust Y as needed

        addActor(choiceTable);

        // Log touch area debug
        this.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                System.out.println("[DEBUG] ChoicePanel touched at: " + x + ", " + y);
                return false;
            }
        });

//        choiceTable.debug();    // Optional: show table layout

    }

    public void showChoices(List<Choice> choices, Consumer<Choice> onChoiceSelected, int choiceIndex) {
        if (choices.size() != 2) {
            System.err.println("[ChoicePanel] Currently supports exactly 2 choices.");
            return;
        }

        choiceTable.clear();

        Button leftButton = createIndexedChoiceButton(choices.get(0).getText(), choiceIndex, true);
        Button rightButton = createIndexedChoiceButton(choices.get(1).getText(), choiceIndex, false);

        leftButton.getColor().a = 0f;
        leftButton.addAction(Actions.fadeIn(0.3f));

        rightButton.getColor().a = 0f;
        rightButton.addAction(Actions.fadeIn(0.3f));

        for (int i = 0; i < choices.size(); i++) {
            final int index = i;
            Button button = (i == 0) ? leftButton : rightButton;

            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    choiceTable.setVisible(false);
                    onChoiceSelected.accept(choices.get(index));
                }
            });
        }

        choiceTable.defaults().pad(10);
        choiceTable.add(leftButton).padRight(300); // optional spacing between buttons
        choiceTable.add(rightButton);

        choiceTable.pack();
        choiceTable.setPosition((1280 - choiceTable.getWidth()) / 2f, 300); // Centered X
        choiceTable.setVisible(true);
    }

    private Button createIndexedChoiceButton(String text, int choiceIndex, boolean isLeft) {
        String prefix = "Choice" + choiceIndex + (isLeft ? "Left" : "Right");

        Texture defaultTex = new Texture(Gdx.files.internal("ingame/choices/" + prefix + "1.png"));
        Texture hoverTex   = new Texture(Gdx.files.internal("ingame/choices/" + prefix + "2.png"));

        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.up = new TextureRegionDrawable(new TextureRegion(defaultTex));
        style.over = new TextureRegionDrawable(new TextureRegion(hoverTex));

        ImageButton button = new ImageButton(style);
        button.setSize(480, 180); // Adjust size as needed

        Label label = new Label(text, skin);
        label.setSize(480, 180);
        label.setAlignment(Align.center);
        label.setTouchable(Touchable.disabled);

        Stack stack = new Stack();
        stack.setSize(480, 180);
        stack.add(button);

        Button container = new Button(new Button.ButtonStyle());
        container.setSize(480, 180);
        container.add(stack).center().grow();
        container.setTouchable(Touchable.enabled);

        container.addListener(new HoverSoundListener());
        container.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.playConfirmSound();
            }
        });


        return container;
    }
}
