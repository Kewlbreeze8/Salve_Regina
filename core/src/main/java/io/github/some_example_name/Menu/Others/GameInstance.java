package io.github.some_example_name.Menu.Others;

import com.badlogic.gdx.Game;

public class GameInstance {
    private static Game game;

    public static void init(Game g) {
        game = g;
    }

    public static Game getInstance() {
        return game;
    }
}
