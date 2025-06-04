package io.github.diskettefox.mortem;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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

    // Human variables
    private Texture humanTexture;
    private Texture humanTouchedTexture;
    private Array<Human> humans;

    // Spider variables
    private Texture spiderSheet;
    private Sprite spiderSprite;
    private Animation<TextureRegion> walkAnimation;
    private static final int FRAME_COLS = 3, FRAME_ROWS = 1;
    private int spiderMovement = 0;
    private int spiderFrame = 0;

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

        spiderSprite = new Sprite();
        spiderSprite.setSize(16, 16);
        spiderSprite.setX(128);

        // Spider animation
        spiderSheet = new Texture("characters/spider/spider.png");
        // spiderSheet = new Texture("characters/spider/arana.png");
        TextureRegion[][] tmp = TextureRegion.split(spiderSheet,
            spiderSheet.getWidth() / FRAME_COLS,
            spiderSheet.getHeight() / FRAME_ROWS);
        TextureRegion[] walkFrames = new TextureRegion[FRAME_COLS * FRAME_ROWS];
        walkFrames[0] = new TextureRegion(spiderSheet, 0, 0, 16, 16);
        walkFrames[1] = new TextureRegion(spiderSheet, 16, 0, 16, 16);
        walkFrames[2] = new TextureRegion(spiderSheet, 32, 0, 16, 16);
        // walkFrames[0] = new TextureRegion(spiderSheet, 0, 0, 64, 64);
        // walkFrames[1] = new TextureRegion(spiderSheet, 64, 0, 64, 64);
        // walkFrames[2] = new TextureRegion(spiderSheet, 128, 0, 64, 64);
        walkAnimation = new Animation<TextureRegion>(1f, walkFrames);


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
            moveSpider();
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN) && Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            spiderSprite.translateX((-speed * delta) / (root2 * 2));
            spiderSprite.translateY((-speed * delta) / root2);
            moveSpider();
        } else if (Gdx.input.isKeyPressed(Input.Keys.UP) && Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            spiderSprite.translateX((-speed * delta) / (root2 * 2));
            spiderSprite.translateY((speed * delta) / root2);
            moveSpider();
        } else if (Gdx.input.isKeyPressed(Input.Keys.UP) && Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            spiderSprite.translateX((speed * delta) / (root2 * 2));
            spiderSprite.translateY((speed * delta) / root2);
            moveSpider();
        }else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            spiderSprite.translateX(speed * delta);
            moveSpider();
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            spiderSprite.translateX(-speed * delta);
            moveSpider();
        } else if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            spiderSprite.translateY(speed * delta);
            moveSpider();
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            spiderSprite.translateY(-speed * delta);
            moveSpider();
        } else {
            spiderMovement = 0;
        }

        // Spawn a new human for testing
        if (Gdx.input.isKeyPressed(Input.Keys.F1)) {
            createHuman();
        }
    }

    public void moveSpider() {
        spiderFrame += 1;
        if (spiderFrame == 5) {
            spiderMovement += 1;
            if (spiderMovement > 2) {
                spiderMovement = 0;
            }
            spiderFrame = 0;
        }
    }

    public void draw() {
        TextureRegion currentFrame = walkAnimation.getKeyFrame(spiderMovement, true);

        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        spriteBatch.begin();

        spriteBatch.draw(backgroundTest, 0, 0, 256, 144);
        // spiderSprite.draw(spriteBatch);
        spriteBatch.draw(currentFrame, spiderSprite.getX(), spiderSprite.getY(), 16, 16);
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
        humanTexture.dispose();
        humanTouchedTexture.dispose();
        backgroundTest.dispose();
        mortemSound.dispose();
        music.dispose();
    }
}
