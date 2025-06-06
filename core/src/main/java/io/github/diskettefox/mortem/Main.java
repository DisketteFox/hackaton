// MortemGame.java
package io.github.diskettefox.mortem;

import com.badlogic.gdx.Game;

public class Main extends Game {
    @Override
    public void create() {
        setScreen(new io.github.diskettefox.mortem.Screens.MainGameScreen(this));
    }
}
