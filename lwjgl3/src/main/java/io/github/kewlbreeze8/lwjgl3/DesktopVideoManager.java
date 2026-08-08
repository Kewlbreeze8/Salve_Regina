package io.github.kewlbreeze8.lwjgl3;

import java.io.File;

import com.badlogic.gdx.Gdx;

import io.github.kewlbreeze8.Ingame.Others.Video.VideoManager;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class DesktopVideoManager implements VideoManager {

    private static boolean initialized = false;

    private MediaPlayer currentPlayer;
    private Stage currentStage;

    @Override
    public void playVideo(String filename, Runnable onFinished) {

        System.out.println("[DesktopVideoManager] playVideo() " + filename);

        initJavaFX();

        System.out.println("[DesktopVideoManager] Before runLater");
        
        System.out.println("[DesktopVideoManager] currentPlayer = " + currentPlayer);
        System.out.println("[DesktopVideoManager] currentStage  = " + currentStage);

        Platform.runLater(() -> {

            System.out.println("[JavaFX] Is FX Thread = " + Platform.isFxApplicationThread());
            System.out.println("[JavaFX] runLater entered: " + filename);
            File file = new File("assets/video/" + filename);

            if (!file.exists()) {
                System.err.println("Video not found: " + file.getAbsolutePath());

                if (onFinished != null)
                    onFinished.run();

                return;
            }

            System.out.println("[JavaFX] Creating media...");
            Media media = new Media(file.toURI().toString());
            currentPlayer = new MediaPlayer(media);
            System.out.println("[JavaFX] Player created.");

            // =========================
            // DEBUG LISTENERS
            // =========================
            currentPlayer.setOnReady(() ->
                System.out.println("[JavaFX] READY: " + filename));

            currentPlayer.setOnPlaying(() ->
                System.out.println("[JavaFX] PLAYING: " + filename));

            currentPlayer.setOnPaused(() ->
                System.out.println("[JavaFX] PAUSED: " + filename));

            currentPlayer.setOnStopped(() ->
                System.out.println("[JavaFX] STOPPED: " + filename));

            currentPlayer.setOnError(() ->
                System.out.println("[JavaFX] ERROR: " + currentPlayer.getError()));

            // =========================

            MediaView view = new MediaView(currentPlayer);

            Group root = new Group(view);
            Scene scene = new Scene(root);

            currentStage = new Stage(StageStyle.UNDECORATED);
            System.out.println("[JavaFX] Stage created.");

            currentStage.setScene(scene);
            currentStage.setFullScreen(true);
            currentStage.show();
            System.out.println("[JavaFX] Stage shown.");

            view.fitWidthProperty().bind(currentStage.widthProperty());
            view.fitHeightProperty().bind(currentStage.heightProperty());
            view.setPreserveRatio(true);

            // Replace your current setOnEndOfMedia with this one
            currentPlayer.setOnEndOfMedia(() -> {

                System.out.println("[JavaFX] END: " + filename);

                currentPlayer.dispose();
                currentStage.close();

                currentPlayer = null;
                currentStage = null;

                if (onFinished != null) {
                    Gdx.app.postRunnable(onFinished);
                }
            });

            currentPlayer.play();

        });

    }

    private static void initJavaFX() {
        if (!initialized) {
            new JFXPanel(); // Starts the JavaFX runtime
            initialized = true;
        }
    }
}