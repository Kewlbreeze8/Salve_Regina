package io.github.kewlbreeze8.Menu.Others;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public class Assets {
    public static final AssetManager manager = new AssetManager();

    public static void loadMenuAssets() {
        // Background & title
        manager.load("menu/Background.png", Texture.class);
        manager.load("menu/Title.png", Texture.class);
        manager.load("menu/ElsaMainMenu1.png", Texture.class);
        manager.load("menu/ElsaMainMenu2.png", Texture.class);

        // Buttons
        manager.load("menu/buttons/NewGame1.png", Texture.class);
        manager.load("menu/buttons/NewGame2.png", Texture.class);
        manager.load("menu/buttons/LoadGame1.png", Texture.class);
        manager.load("menu/buttons/LoadGame2.png", Texture.class);
        manager.load("menu/buttons/Options1.png", Texture.class);
        manager.load("menu/buttons/Options2.png", Texture.class);
        manager.load("menu/buttons/Exit1.png", Texture.class);
        manager.load("menu/buttons/Exit2.png", Texture.class);
        manager.load("menu/buttons/Continue1.png", Texture.class);
        manager.load("menu/buttons/Continue2.png", Texture.class);

        // Dialog button textures
        manager.load("popup/Box.png", Texture.class);
        manager.load("popup/Yes1.png", Texture.class);
        manager.load("popup/Yes2.png", Texture.class);
        manager.load("popup/No1.png", Texture.class);
        manager.load("popup/No2.png", Texture.class);

        // Music & Effects
        Assets.loadBGM();
        Assets.loadSFX();

    }

    public static void unloadMenuAssets() {
        // Optional: unload these when switching away
        manager.unload("menu/Background.png");
        manager.unload("menu/Title.png");
        // ...etc
    }

    public static void loadBGM() {
        manager.load("sound/music/Title.mp3", Music.class);
    }

    public static void loadSFX() {
        manager.load("sound/effect/Confirm.wav", Sound.class);
        manager.load("sound/effect/Hover.wav", Sound.class);
        manager.load("sound/effect/Reject.wav", Sound.class);
    }

//  public static void loadGameAssets() { /* for ingame stuff */ }

    public static void loadOptionsAssets() {
        // Logo and section labels
        manager.load("options/OptionsLogo.png", Texture.class);
        manager.load("options/TextSpeed.png", Texture.class);
        manager.load("options/Brightness.png", Texture.class);
        manager.load("options/Music.png", Texture.class);
        manager.load("options/SFX.png", Texture.class);
        manager.load("options/Fullscreen.png", Texture.class);

        // Sliders
        manager.load("options/SliderBar.png", Texture.class);
        manager.load("options/SliderBarFill1.png", Texture.class);
        manager.load("options/SliderBarFill2.png", Texture.class);
        manager.load("options/SliderKnob.png", Texture.class);

        // Buttons
        manager.load("options/Back1.png", Texture.class);
        manager.load("options/Back2.png", Texture.class);
        manager.load("options/Credits1.png", Texture.class);
        manager.load("options/Credits2.png", Texture.class);
        manager.load("options/ToggleDefaults1.png", Texture.class);
        manager.load("options/ToggleDefaults2.png", Texture.class);

        // Checkbox
        manager.load("options/Box.png", Texture.class);
        manager.load("options/BoxCheck.png", Texture.class);
    }

    public static void unloadOptionsAssets() {
        manager.unload("options/OptionsLogo.png");
        manager.unload("options/TextSpeed.png");
        manager.unload("options/Brightness.png");
        manager.unload("options/Music.png");
        manager.unload("options/SFX.png");
        manager.unload("options/Fullscreen.png");

        manager.unload("options/SliderBar.png");
        manager.unload("options/SliderBarFill1.png");
        manager.unload("options/SliderBarFill2.png");
        manager.unload("options/SliderKnob.png");

        manager.unload("options/Back1.png");
        manager.unload("options/Back2.png");
        manager.unload("options/Credits1.png");
        manager.unload("options/Credits2.png");
        manager.unload("options/ToggleDefaults1.png");
        manager.unload("options/ToggleDefaults2.png");

        manager.unload("options/Box.png");
        manager.unload("options/BoxCheck.png");
    }


    public static void dispose() {
        manager.dispose();
    }
}
