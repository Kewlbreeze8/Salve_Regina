package io.github.Kewlbreeze8.Ingame.Others;

import com.badlogic.gdx.Game;

import io.github.Kewlbreeze8.Menu.MainMenu;

public class GameMain extends Game {
    @Override
    public void create() {
        setScreen(new MainMenu(this));
    }
}
