package io.github.Kewlbreeze8.Menu.Others;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class AudioManager {
    private static final Preferences prefs = Gdx.app.getPreferences("VNOptions");

    private static float musicVolume = prefs.getFloat("music", 0.5f);
    private static float sfxVolume = prefs.getFloat("sfx", 0.5f);

    private static Music menuMusic;
    private static Sound hoverSound;
    private static Sound confirmSound;
    private static Sound rejectSound;

    private static long lastConfirmTime = 0;
    private static long lastHoverTime = 0;
    private static long lastRejectTime = 0;

    private static final long sfxCooldown = 100; // ms
    private static final long hoverCooldownAfterClick = 300; // ms

    private static Music currentStoryMusic;

    // Custom SFX tracking
    private static Sound currentSFX;
    private static long currentSFXId = -1;
    private static float currentSFXVolume = 0f;

    // === Initialization ===
    public static void load() {
        hoverSound = Assets.manager.get("sound/effect/Hover.wav", Sound.class);
        confirmSound = Assets.manager.get("sound/effect/Confirm.wav", Sound.class);
        rejectSound = Assets.manager.get("sound/effect/Reject.wav", Sound.class);

        setMusicVolume(musicVolume);
        setSFXVolume(sfxVolume);
    }

    // === Music Control ===
    public static void playMenuMusic() {
        if (menuMusic == null) {
            menuMusic = Assets.manager.get("sound/music/Title.mp3", Music.class);
            menuMusic.setLooping(true);
        }

        if (!menuMusic.isPlaying()) {
            menuMusic.setVolume(0f);
            menuMusic.play();
            fadeVolume(menuMusic, 0f, musicVolume, 20);
        }
    }

    public static void stopMenuMusic() {
        if (menuMusic != null) menuMusic.stop();
    }

    public static void fadeOutAndStopMenuMusic() {
        if (menuMusic != null && menuMusic.isPlaying()) {
            fadeVolume(menuMusic, musicVolume, 0f, 20, AudioManager::stopMenuMusic);
        }
    }

    // === Story Music ===
    public static void playStoryMusic(String fileName, boolean fade) {
        stopStoryMusic(fade);

        try {
            currentStoryMusic = Gdx.audio.newMusic(Gdx.files.internal("sound/music/" + fileName));
            currentStoryMusic.setLooping(true);

            if (fade) {
                currentStoryMusic.setVolume(0f);
                currentStoryMusic.play();
                fadeVolume(currentStoryMusic, 0f, musicVolume, 20);
            } else {
                currentStoryMusic.setVolume(musicVolume);
                currentStoryMusic.play();
            }
        } catch (Exception e) {
            System.err.println("[AudioManager] Failed to load story music: " + fileName);
            e.printStackTrace();
        }
    }

    public static void stopStoryMusic(boolean fade) {
        if (currentStoryMusic != null) {
            if (fade) {
                fadeVolume(currentStoryMusic, musicVolume, 0f, 20, () -> {
                    if (currentStoryMusic != null) {
                        currentStoryMusic.stop();
                        currentStoryMusic.dispose();
                        currentStoryMusic = null;
                    }
                });
            } else {
                currentStoryMusic.stop();
                currentStoryMusic.dispose();
                currentStoryMusic = null;
            }
        }
    }

    // === Custom SFX Control ===
    public static void playCustomSFX(String fileName) {
        try {
            stopCustomSFX();

            currentSFX = Gdx.audio.newSound(Gdx.files.internal("sound/effect/" + fileName));
            currentSFXVolume = sfxVolume;
            currentSFXId = currentSFX.play(sfxVolume);
        } catch (Exception e) {
            System.err.println("[AudioManager] Failed to load SFX: " + fileName);
            e.printStackTrace();
        }
    }

    public static void stopCustomSFX() {
        if (currentSFX != null && currentSFXId != -1) {
            new Thread(() -> {
                float step = 0.05f;
                float delay = 30f;
                for (float v = currentSFXVolume; v > 0f; v -= step) {
                    final float vol = v;
                    Gdx.app.postRunnable(() -> {
                        if (currentSFX != null) {
                            currentSFX.setVolume(currentSFXId, Math.max(0f, vol));
                        }
                    });
                    try {
                        Thread.sleep((long) delay);
                    } catch (InterruptedException ignored) {}
                }
                Gdx.app.postRunnable(() -> {
                    currentSFX.stop(currentSFXId);
                    currentSFX.dispose();
                    currentSFX = null;
                    currentSFXId = -1;
                    currentSFXVolume = 0f;
                });
            }).start();
        }
    }

    public static void fadeOutCustomSFX(int durationMs) {
        if (currentSFX != null && currentSFXId != -1) {
            new Thread(() -> {
                float volume = sfxVolume;
                float step = volume / 20f;
                for (int i = 0; i < 20; i++) {
                    float newVolume = volume - step * i;
                    Gdx.app.postRunnable(() -> currentSFX.setVolume(currentSFXId, Math.max(0f, newVolume)));
                    try {
                        Thread.sleep(durationMs / 20);
                    } catch (InterruptedException ignored) {}
                }
                Gdx.app.postRunnable(() -> stopCustomSFX());
            }).start();
        }
    }

    // === SFX Methods ===
    public static void playSFX(Sound sound) {
        if (sound != null) sound.play(sfxVolume);
    }

    public static void playHoverSound() {
        long now = System.currentTimeMillis();
        if (now - lastConfirmTime >= hoverCooldownAfterClick && now - lastHoverTime >= sfxCooldown) {
            lastHoverTime = now;
            if (hoverSound != null) hoverSound.play(sfxVolume);
        }
    }

    public static void playConfirmSound() {
        long now = System.currentTimeMillis();
        if (now - lastConfirmTime >= sfxCooldown) {
            lastConfirmTime = now;
            if (confirmSound != null) confirmSound.play(sfxVolume);
        }
    }

    public static void playRejectSound() {
        long now = System.currentTimeMillis();
        if (now - lastRejectTime >= sfxCooldown) {
            lastRejectTime = now;
            if (rejectSound != null) rejectSound.play(sfxVolume);
        }
    }

    public static void stopAllSFX() {
        if (hoverSound != null) hoverSound.stop();
        if (confirmSound != null) confirmSound.stop();
        if (rejectSound != null) rejectSound.stop();
        stopCustomSFX();
    }

    // === Volume Control ===
    public static void setMusicVolume(float volume) {
        musicVolume = volume;
        if (menuMusic != null && menuMusic.isPlaying()) {
            menuMusic.setVolume(volume);
        }
        if (currentStoryMusic != null && currentStoryMusic.isPlaying()) {
            currentStoryMusic.setVolume(volume);
        }
        prefs.putFloat("music", volume).flush();
    }

    public static void setSFXVolume(float volume) {
        sfxVolume = volume;
        prefs.putFloat("sfx", volume).flush();

        if (currentSFX != null && currentSFXId != -1) {
            currentSFX.setVolume(currentSFXId, sfxVolume);
        }
    }

    public static void fadeToMusicVolume(float targetVolume, float durationSeconds) {
        if (menuMusic == null && currentStoryMusic == null) return;

        new Thread(() -> {
            float current = musicVolume;
            float diff = targetVolume - current;
            int steps = 20;
            for (int i = 1; i <= steps; i++) {
                float vol = current + (diff * i / steps);
                float clamped = Math.max(0f, Math.min(1f, vol));
                Gdx.app.postRunnable(() -> {
                    if (menuMusic != null && menuMusic.isPlaying()) {
                        menuMusic.setVolume(clamped);
                    }
                    if (currentStoryMusic != null && currentStoryMusic.isPlaying()) {
                        currentStoryMusic.setVolume(clamped);
                    }
                });
                try {
                    Thread.sleep((long)(durationSeconds * 1000f / steps));
                } catch (InterruptedException ignored) {}
            }
            musicVolume = targetVolume;
            prefs.putFloat("music", targetVolume).flush();
        }).start();
    }

    public static float getMusicVolume() {
        return musicVolume;
    }

    public static float getSFXVolume() {
        return sfxVolume;
    }

    // === Utility Fade Logic ===
    private static void fadeVolume(Music music, float from, float to, int stepMs) {
        fadeVolume(music, from, to, stepMs, null);
    }

    private static void fadeVolume(Music music, float from, float to, int stepMs, Runnable onFinish) {
        new Thread(() -> {
            float step = 0.01f * (to > from ? 1 : -1);
            for (float v = from; (step > 0 ? v <= to : v >= to); v += step) {
                float volume = Math.max(0f, Math.min(1f, v));
                Gdx.app.postRunnable(() -> {
                    if (music != null && music.isPlaying()) {
                        music.setVolume(volume);
                    }
                });
                try {
                    Thread.sleep(stepMs);
                } catch (InterruptedException ignored) {}
            }
            if (onFinish != null) {
                Gdx.app.postRunnable(onFinish);
            }
        }).start();
    }

    public static void loadSavedVolumes() {
        // Currently unused
    }
}
