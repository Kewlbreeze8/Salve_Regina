package io.github.Kewlbreeze8.Ingame.Others;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;

import io.github.Kewlbreeze8.Ingame.IngameController;
import io.github.Kewlbreeze8.Ingame.StoryManager;
import io.github.Kewlbreeze8.Menu.Credits;
import io.github.Kewlbreeze8.Menu.Others.GameInstance;

import java.util.*;
import java.util.stream.Collectors;

public class FrameVideoScreen implements Screen {
    private final Game game;
    private final SpriteBatch batch = new SpriteBatch();
    private final ArrayList<Texture> frames = new ArrayList<>();
    private final Music audio;
    private final String nextNode;
    private final boolean isEndingSequence;
    private final String penanceLevel;

    private final float frameDuration = 1f / 30f;
    private float elapsed = 0f;
    private int currentFrame = 0;

    private List<FileHandle> frameHandles;

    private float fadeAlpha = 1f;
    private float fadeSpeed = 1.5f;
    private boolean fadingIn = true;
    private boolean fadingOut = false;

    private BitmapFont font;
    private boolean showChapterText = false;
    private boolean waitingForClick = false;
    private boolean postEndingPlaying = false;

    private Texture blackTexture;

    public FrameVideoScreen(Game game, String baseFolder, String videoFolderName, String nextNode, boolean isEndingSequence, String penanceLevel) {
        this.game = game;
        this.nextNode = nextNode;
        this.isEndingSequence = isEndingSequence;
        this.penanceLevel = penanceLevel;

        font = new BitmapFont();
        font.getData().setScale(2.5f);
        font.setColor(Color.WHITE);

        // Load audio
        FileHandle audioFile = Gdx.files.internal(baseFolder + "/MP3/" + videoFolderName + ".mp3");
        if (!audioFile.exists()) throw new RuntimeException("Missing audio: " + audioFile.path());
        audio = Gdx.audio.newMusic(audioFile);
        audio.setOnCompletionListener(music -> Gdx.app.postRunnable(this::onVideoFinished));

        // Load frames
        FileHandle folder = Gdx.files.internal(baseFolder + "/FPS/" + videoFolderName);
        System.out.println("[DEBUG] Checking folder: " + folder.path());
        System.out.println("[DEBUG] Exists? " + folder.exists());
        System.out.println("[DEBUG] Is Directory? " + folder.isDirectory());

        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("[ERROR] Folder not found or is not a directory: " + folder.path());
            fallbackToNextNode();
            return;
        }

        FileHandle[] frameFiles = folder.list((dir, name) -> name.toLowerCase().endsWith(".png"));
        if (frameFiles == null || frameFiles.length == 0) {
            System.err.println("[ERROR] No PNG frames found in: " + folder.path());
            fallbackToNextNode();
            return;
        }

        Arrays.sort(frameFiles, Comparator.comparing(FileHandle::name));
        int maxFramesToLoad = 150;
        for (int i = 0; i < Math.min(maxFramesToLoad, frameFiles.length); i++) {
            frames.add(new Texture(frameFiles[i]));
        }

        System.out.println("[DEBUG] Total frames loaded: " + frames.size());

        audio.play();

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (waitingForClick && !fadingOut) {
                    waitingForClick = false;
                    fadingOut = true;
                }
                return true;
            }
        });
    }

    private void onVideoFinished() {
        if (isEndingSequence && !postEndingPlaying) {
            showChapterText = true;
            waitingForClick = true;
        } else if (postEndingPlaying) {
            Gdx.app.postRunnable(() -> GameInstance.getInstance().setScreen(new Credits(game)));
        } else {
            Gdx.app.postRunnable(() -> {
                StoryManager.setCurrentNode(nextNode);
                GameInstance.getInstance().setScreen(new IngameController(game, nextNode, 0));
            });
        }
    }

    private void loadPostEndingVideo() {
        String penance = penanceLevel.toLowerCase();
        String penanceVideo = "Ending_" + penance;

        Gdx.app.postRunnable(() -> {
            GameInstance.getInstance().setScreen(
                new FrameVideoScreen(game, "video", penanceVideo, "", true, penance)
            );
        });
    }

    private void fallbackToNextNode() {
        if (nextNode == null || nextNode.isEmpty()) {
            System.err.println("[FATAL] No nextNode specified for FrameVideoScreen fallback.");
            GameInstance.getInstance().setScreen(new IngameController(game, "Break", 0));
            return;
        }

        StoryManager.setCurrentNode(nextNode);
        GameInstance.getInstance().setScreen(new IngameController(game, nextNode, 0));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (frames.isEmpty()) {
            System.err.println("[FATAL] No frames to render! Skipping video screen.");
            fallbackToNextNode();
            return;
        }

        elapsed += delta;
        if (currentFrame < frames.size() - 1 && elapsed >= frameDuration) {
            currentFrame++;
            elapsed = 0f;
        }

        batch.begin();
        batch.draw(frames.get(currentFrame), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

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
                fadingOut = false;

                if (showChapterText) {
                    postEndingPlaying = true;
                    showChapterText = false;
                    loadPostEndingVideo();
                    return;
                }
            }
        }

        if (fadingIn || fadingOut || fadeAlpha > 0f) {
            Color c = batch.getColor();
            batch.setColor(0f, 0f, 0f, fadeAlpha);
            batch.draw(getBlackTexture(), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.setColor(c);
        }

        batch.end();
    }

    private Texture getBlackTexture() {
        if (blackTexture == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.BLACK);
            pixmap.fill();
            blackTexture = new Texture(pixmap);
            pixmap.dispose();
        }
        return blackTexture;
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void show() {}

    @Override
    public void dispose() {
        for (Texture t : frames) t.dispose();
        if (blackTexture != null) blackTexture.dispose();
        audio.dispose();
        font.dispose();
        batch.dispose();
    }
}
