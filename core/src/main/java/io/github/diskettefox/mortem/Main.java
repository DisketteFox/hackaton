package io.github.diskettefox.mortem;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private SpriteBatch spriteBatch;
    private FitViewport viewport;

    private Texture backgroundTest;

    private Texture humanTexture;
    private Array<Sprite> humanSprites;

    private Texture spiderTexture;
    private Sprite spiderSprite;

    @Override
    public void create() {
        spiderTexture = new Texture("characters/spider/spider-1.png");
        backgroundTest = new Texture("stages/test/test.png");

        humanSprites = new Array<>();
        humanTexture = new Texture("characters/human/human-1.png");

        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(256, 144);

        spiderSprite = new Sprite(spiderTexture); // Initialize the sprite based on the texture
        spiderSprite.setSize(16, 16); // Define the size of the sprite
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true); // true centers the camera
    }

    private void logic() {

    }

    public void input() {
        float speed = 40f;
        float delta = Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            spiderSprite.translateX(speed * delta);
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            spiderSprite.translateX(-speed * delta);
        } else if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            spiderSprite.translateY(speed * delta);
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            spiderSprite.translateY(-speed * delta);
        } else if (Gdx.input.isKeyPressed(Input.Keys.F1)) {
            createHuman();
        }
    }

    public void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        spriteBatch.draw(backgroundTest, 0, 0, 256, 144);
        spiderSprite.draw(spriteBatch);

        for (Sprite humanSprite : humanSprites) {
            humanSprite.draw(spriteBatch);
        }

        spriteBatch.end();
    }

    private void createHuman() {
        System.out.println("Humano creado");
        float humanWidth = 16;
        float humanHeight = 16;
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldWidth();

        Sprite humanSprite = new Sprite(humanTexture);
        humanSprite.setSize(humanWidth, humanHeight);
        humanSprite.setX(MathUtils.random(0f, worldWidth - humanWidth));
        humanSprite.setY(MathUtils.random(0f, worldHeight - humanHeight));
        humanSprites.add(humanSprite);
    }

    @Override
    public void render() {
        input();
        logic();
        draw();
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        spiderTexture.dispose();
    }
}
