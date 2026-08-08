package io.github.kewlbreeze8.Ingame.Others.Video;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.github.kewlbreeze8.Menu.Others.System.GameInstance;

public class VideoScreen extends ScreenAdapter {

    private final String videoFile;
    private final Runnable onFinished;

    private SpriteBatch batch;
    private Texture blackTexture;

    private float fadeAlpha = 1f;
    private float fadeSpeed = 1.5f;

    private boolean fadingIn = true;
    private boolean fadingOut = false;
    private boolean callbackCalled = false;

    public VideoScreen(String videoFile, Runnable onFinished) {
        this.videoFile = videoFile;
        this.onFinished = onFinished;
    }

    @Override
    public void show() {

        System.out.println("[VideoScreen] show() called");
        batch = new SpriteBatch();
        Gdx.input.setInputProcessor(null);
        System.out.println("[VideoScreen] Playing " + videoFile);

        VideoService.playVideo(videoFile, () -> {
            System.out.println("[VideoScreen] Video finished.");
            fadingOut = true;
        });
    }

    @Override
    public void render(float delta) {
//        System.out.println("VideoScreen render");
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        if (fadingIn) {
            fadeAlpha -= delta * fadeSpeed;

            if (fadeAlpha <= 0f) {
                fadeAlpha = 0f;
                fadingIn = false;
            }
        }

        if (fadingOut) {
            fadeAlpha += delta * fadeSpeed;

            if (fadeAlpha >= 1f) {
                fadeAlpha = 1f;

                if (!callbackCalled) {
                    callbackCalled = true;
                    System.out.println("[VideoScreen] callbackCalled = " + callbackCalled);
                    System.out.println("[VideoScreen] fadingOut = " + fadingOut);
                    System.out.println("[VideoScreen] currentScreen = "
                            + GameInstance.getInstance().getScreen().getClass().getSimpleName());

                    if (onFinished != null) {

                        System.out.println("[VideoScreen] Scheduling callback");

                        Gdx.app.postRunnable(() -> {

                            System.out.println("[VideoScreen] Executing callback");

                            try {
                                onFinished.run();
                            } catch (Throwable t) {
                                System.err.println("========== CALLBACK EXCEPTION ==========");
                                t.printStackTrace();
                            }

                        });
                    }
                }

            }

        }

        if (fadeAlpha > 0f) {
            Color old = batch.getColor();
            batch.setColor(0,0,0,fadeAlpha);

            batch.draw(getBlackTexture(),
                    0,
                    0,
                    Gdx.graphics.getWidth(),
                    Gdx.graphics.getHeight());

            batch.setColor(old);
        }
        batch.end();
    }

    private Texture getBlackTexture() {

        if (blackTexture == null) {
            Pixmap pix = new Pixmap(1,1, Pixmap.Format.RGBA8888);
            pix.setColor(Color.BLACK);
            pix.fill();
            blackTexture = new Texture(pix);
            pix.dispose();
        }
        return blackTexture;
    }

    @Override
    public void hide() {
        System.out.println("[VideoScreen] HIDE");
        new Exception().printStackTrace();
    }

    @Override
    public void dispose() {

        System.out.println("================================");
        System.out.println("[VideoScreen] DISPOSE");
        new Exception().printStackTrace();
        System.out.println("================================");

        if (batch != null)
            batch.dispose();

        if (blackTexture != null)
            blackTexture.dispose();
    }    

}