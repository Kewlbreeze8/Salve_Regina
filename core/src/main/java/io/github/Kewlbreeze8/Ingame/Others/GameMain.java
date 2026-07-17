package io.github.kewlbreeze8.Ingame.Others;

import com.badlogic.gdx.Game;

import io.github.kewlbreeze8.Menu.MainMenu;

public class GameMain extends Game {
    @Override
    public void create() {
        setScreen(new MainMenu(this));
    }
}
