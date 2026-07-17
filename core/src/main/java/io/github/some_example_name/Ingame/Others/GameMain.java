package io.github.some_example_name.Ingame.Others;

import com.badlogic.gdx.Game;
import io.github.some_example_name.Menu.MainMenu;

public class GameMain extends Game {
    @Override
    public void create() {
        setScreen(new MainMenu(this));
    }
}
