package io.github.Kewlbreeze8.Menu.Others;

import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.*;

public class HoverSoundListener extends InputListener {

    private static final long COOLDOWN_MS = 100;
    private static long lastClickTime = 0;

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        lastClickTime = System.currentTimeMillis(); // Mark click
        return false;
    }

    @Override
    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        if (pointer != -1) return;

        long now = System.currentTimeMillis();
        if (now - lastClickTime < COOLDOWN_MS) return;

        Actor actor = event.getListenerActor();

        // Skip hover sound if button is toggled
        if (actor instanceof ImageButton) {
            ImageButton btn = (ImageButton) actor;
            if (btn.isChecked()) return; // Suppress sound for toggled buttons
        }

        AudioManager.playHoverSound();
    }


}

