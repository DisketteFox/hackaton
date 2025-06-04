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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private SpriteBatch spriteBatch;
    private FitViewport viewport;

    private Texture backgroundTest;

    private Texture humanTexture;
    private Texture humanTouchedTexture;
    private Array<Human> humans;

    private Texture spiderTexture;
    private Texture spiderTexture2;
    private Texture spiderTexture3;
    private Sprite spiderSprite;

    private Rectangle spiderRectangle;
    private Rectangle humanRectangle;

    private Music music;
    private Sound mortemSound;

    // Human class to track state
    private static class Human {
        Sprite sprite;
        boolean touched;

        Human(Sprite sprite) {
            this.sprite = sprite;
            this.touched = false;
        }
    }

    @Override
    public void create() {
        spiderTexture = new Texture("characters/spider/spider-1.png");
        spiderTexture2 = new Texture("characters/spider/spider-2.png");
        spiderTexture3 = new Texture("characters/spider/spider-3.png");
        spiderSprite = new Sprite(spiderTexture);
        spiderSprite.setSize(16, 16);
        spiderSprite.setX(128);

        humanTexture = new Texture("characters/human/human-1.png");
        humanTouchedTexture = new Texture("characters/human/human-d.png");
        humans = new Array<>();

        backgroundTest = new Texture("stages/test/test2.png");

        spiderRectangle = new Rectangle();
        humanRectangle = new Rectangle();

        mortemSound = Gdx.audio.newSound(Gdx.files.internal("assets/sounds/mortem.mp3"));

        music = Gdx.audio.newMusic(Gdx.files.internal("assets/music/song.mp3"));
        music.setLooping(true);
        music.setVolume(.8f);
        music.play();

        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(256, 144);

        createHuman();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    private void logic() {
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        float spiderWidth = spiderSprite.getWidth();
        float spiderHeight = spiderSprite.getHeight();

        spiderSprite.setX(MathUtils.clamp(spiderSprite.getX(), 0, worldWidth - spiderWidth));

        float delta = Gdx.graphics.getDeltaTime();
        spiderRectangle.set(spiderSprite.getX(), spiderSprite.getY(), spiderWidth, spiderHeight);

        for (int i = humans.size - 1; i >= 0; i--) {
            Human human = humans.get(i);
            Sprite dropSprite = human.sprite;
            float dropWidth = dropSprite.getWidth();
            float dropHeight = dropSprite.getHeight();

            humanRectangle.set(dropSprite.getX() + 5, dropSprite.getY(), 7, dropHeight);

            if (dropSprite.getY() < -dropHeight) {
                humans.removeIndex(i);
            } else if (!human.touched && spiderRectangle.overlaps(humanRectangle)) {
                dropSprite.setTexture(humanTouchedTexture);
                mortemSound.play();
                human.touched = true;
            }
        }
    }

    public void input() {
        float speed = 40f;
        float delta = Gdx.graphics.getDeltaTime();
        float root2 = (float)Math.sqrt(2);

        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) && Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            spiderSprite.translateX((speed * delta) / (root2 * 2));
            spiderSprite.translateY((-speed * delta) / root2);
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN) && Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            spiderSprite.translateX((-speed * delta) / (root2 * 2));
            spiderSprite.translateY((-speed * delta) / root2);
        } else if (Gdx.input.isKeyPressed(Input.Keys.UP) && Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            spiderSprite.translateX((-speed * delta) / (root2 * 2));
            spiderSprite.translateY((speed * delta) / root2);
        } else if (Gdx.input.isKeyPressed(Input.Keys.UP) && Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            spiderSprite.translateX((speed * delta) / (root2 * 2));
            spiderSprite.translateY((speed * delta) / root2);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            spiderSprite.translateX(speed * delta);
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            spiderSprite.translateX(-speed * delta);
        } else if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            spiderSprite.translateY(speed * delta);
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            spiderSprite.translateY(-speed * delta);
        } 

        // Spawn a new human for testing
        if (Gdx.input.isKeyPressed(Input.Keys.F1)) {
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
        for (Human human : humans) {
            human.sprite.draw(spriteBatch);
        }
        spriteBatch.end();
    }

    private void createHuman() {
        System.out.println("Human spawned");

        float humanWidth = 16;
        float humanHeight = 16;
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight(); // fixed typo

        Sprite humanSprite = new Sprite(humanTexture);
        humanSprite.setSize(humanWidth, humanHeight);
        humanSprite.setX(MathUtils.random(0f, worldWidth - 28 - humanWidth) + 14);
        humanSprite.setY(MathUtils.random(0f, worldHeight - 28 - humanHeight) + 14);

        humans.add(new Human(humanSprite));
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
        spiderTexture2.dispose();
        spiderTexture3.dispose();
        humanTexture.dispose();
        humanTouchedTexture.dispose();
        backgroundTest.dispose();
        mortemSound.dispose();
        music.dispose();
    }
}
