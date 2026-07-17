package io.github.Kewlbreeze8.Menu.Others;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.utils.viewport.FitViewport;
import java.util.function.Supplier;

public class LoadingScreen implements Screen {
    private final Game game;
    private final Runnable loadAssets;
    private final Supplier<Screen> nextScreenSupplier;

    private SpriteBatch batch;
    private BitmapFont font;
    private FitViewport viewport;

    public LoadingScreen(Game game, Runnable loadAssets, Supplier<Screen> nextScreenSupplier) {
        this.game = game;
        this.loadAssets = loadAssets;
        this.nextScreenSupplier = nextScreenSupplier;

        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        this.viewport = new FitViewport(1280, 720);

        this.loadAssets.run(); // Queue the assets
        AudioManager.loadSavedVolumes();
    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (Assets.manager.update()) {
            game.setScreen(nextScreenSupplier.get()); // ✅ Safe, lazy
        } else {
            float progress = Assets.manager.getProgress();

            batch.setProjectionMatrix(viewport.getCamera().combined);
            batch.begin();
            font.draw(batch, "Loading... Please Stand by..." + Math.round(progress * 100) + "%", 540, 360);
            batch.end();
        }
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void dispose() { batch.dispose(); font.dispose(); }
    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
