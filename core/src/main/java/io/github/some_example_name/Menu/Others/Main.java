package io.github.some_example_name.Menu.Others;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.some_example_name.Menu.MainMenu;

public class Main extends Game {
    public SpriteBatch batch;

    @Override
    public void create() {
        Preferences prefs = Gdx.app.getPreferences("VNOptions");
        boolean fullscreen = prefs.getBoolean("fullscreen", false);

        batch = new SpriteBatch();

        if (fullscreen) {
            Graphics.DisplayMode currentMode = Gdx.graphics.getDisplayMode();
            Gdx.graphics.setFullscreenMode(currentMode);
        } else {
            Gdx.graphics.setWindowedMode(1280, 720);
        }

        GameInstance.init(this);

        // 🟢 Start with the WarningScreen FIRST
        setScreen(new WarningScreen(this));

        AudioManager.loadSavedVolumes();
    }

    @Override
    public void dispose() {
        batch.dispose();
        Assets.dispose();
        super.dispose();
    }
}
