package io.github.kewlbreeze8.Ingame.Others.Video;

public class VideoService {

    private static VideoManager manager;

    public static void setManager(VideoManager videoManager) {
        manager = videoManager;
    }

    public static VideoManager getManager() {
        return manager;
    }

    // ADD THIS
    public static void playVideo(String filename, Runnable onFinished) {

        System.out.println("[VideoService] playVideo: " + filename);
        if (manager == null) {
            throw new IllegalStateException("VideoManager has not been initialized.");
        }

        manager.playVideo(filename, onFinished);
    }

}