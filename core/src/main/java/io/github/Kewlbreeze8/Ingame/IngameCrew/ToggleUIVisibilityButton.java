package io.github.Kewlbreeze8.Ingame.IngameCrew;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import io.github.Kewlbreeze8.Menu.Others.AudioManager;

public class ToggleUIVisibilityButton extends Image implements com.badlogic.gdx.utils.Disposable {

    private final Texture hideDefault, hideHover, showDefault, showHover;
    private boolean uiVisible = true;
    private final Runnable toggleCallback;

    public ToggleUIVisibilityButton(Runnable toggleCallback) {
        this.toggleCallback = toggleCallback;

        hideDefault = new Texture(Gdx.files.internal("ingame/buttons/ToggleHide1.png"));
        hideHover   = new Texture(Gdx.files.internal("ingame/buttons/ToggleHide2.png"));
        showDefault = new Texture(Gdx.files.internal("ingame/buttons/ToggleShow1.png"));
        showHover   = new Texture(Gdx.files.internal("ingame/buttons/ToggleShow2.png"));

        setSize(50, 50);
        setPosition(1190, 40);
        setDrawable(new TextureRegionDrawable(new TextureRegion(hideDefault)));

        setTouchable(Touchable.enabled); // 👈 This is the magic

        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                uiVisible = !uiVisible;
                toggleCallback.run();
                updateTexture();
                AudioManager.playConfirmSound(); // 👈 Add this line
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                updateHoverTexture();
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                updateTexture();
            }
        });
    }


    private void updateTexture() {
        setDrawable(new TextureRegionDrawable(new TextureRegion(uiVisible ? hideDefault : showDefault)));
    }

    private void updateHoverTexture() {
        setDrawable(new TextureRegionDrawable(new TextureRegion(uiVisible ? hideHover : showHover)));
    }

    @Override
    public void dispose() {
        hideDefault.dispose();
        hideHover.dispose();
        showDefault.dispose();
        showHover.dispose();
    }
}

