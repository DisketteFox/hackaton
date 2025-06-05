package io.github.diskettefox.mortem;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private SpriteBatch spriteBatch;
    private FitViewport viewport;
    private boolean isPixel = false;
    private int currentMap = 1;

    private Texture background1, background2, humanTexture, humanTouchedTexture, spiderSheet;
    private Rectangle wall1 = new Rectangle(), wall2 = new Rectangle(), wall3 = new Rectangle(), wall4 = new Rectangle();
    private Rectangle spiderRectangle = new Rectangle();
    private Array<Human> humanslv1 = new Array<>();
    private Array<Human> humanslv2 = new Array<>();

    private Sprite spiderSprite = new Sprite();
    private Animation<TextureRegion> walkAnimation;
    private static final int FRAME_COLS = 3, FRAME_ROWS = 1;
    private int spiderMovement = 0, spiderFrame = 0;

    private Music music;
    private Sound mortemSound;

    // Controller lock
    private boolean lockUP = false, lockDOWN = false, lockLEFT = false, lockRIGHT = false;

    // Human class to track state and hitbox
    private static class Human {
        Sprite sprite;
        boolean touched;
        Rectangle rectangle;

        Human(Sprite sprite) {
            this.sprite = sprite;
            this.touched = false;
            this.rectangle = new Rectangle(sprite.getX() + 5, sprite.getY(), 7, sprite.getHeight());
        }

        void updateRectangle() {
            this.rectangle.set(sprite.getX() + 5, sprite.getY(), 7, sprite.getHeight());
        }
    }

    @Override
    public void create() {
        spiderSprite.setSize(16, 16);
        spiderSprite.setPosition(128, 32);

        // Spider animation
        spiderSheet = new Texture("characters/spider/arana.png");
        // spiderSheet = new Texture("characters/spider/spider.png");
        TextureRegion[] walkFrames = {
            new TextureRegion(spiderSheet, 0, 0, 64, 64),
            new TextureRegion(spiderSheet, 64, 0, 64, 64),
            new TextureRegion(spiderSheet, 128, 0, 64, 64)
            // new TextureRegion(spiderSheet, 0, 0, 16, 16);
            // new TextureRegion(spiderSheet, 16, 0, 16, 16);
            // new TextureRegion(spiderSheet, 32, 0, 16, 16);
        };
        walkAnimation = new Animation<>(1f, walkFrames);

        humanTexture = new Texture("characters/human/human-1.png");
        humanTouchedTexture = new Texture("characters/human/human-d.png");

        background1 = new Texture("stages/test/test1.png");
        background2 = new Texture("stages/test/test2.png");

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
        collisions();

        spiderRectangle.set(spiderSprite.getX(), spiderSprite.getY(), spiderSprite.getWidth(), spiderSprite.getHeight());
        wall1.set(0, 0, 14, 144);
        wall2.set(0, 115, 256, 14);
        wall3.set(242, 0, 14, 144);
        wall4.set(0, 0, 256, 14);

        if (currentMap == 1) {
            for (int i = humanslv1.size - 1; i >= 0; i--) {
                Human human = humanslv1.get(i);
                human.updateRectangle();

                if (human.sprite.getY() < -human.sprite.getHeight()) {
                    humanslv1.removeIndex(i);
                } else if (!human.touched && spiderRectangle.overlaps(human.rectangle)) {
                    human.sprite.setTexture(humanTouchedTexture);
                    mortemSound.play();
                    human.touched = true;
                }
            }
        } else if (currentMap == 2) {
            for (int i = humanslv2.size - 1; i >= 0; i--) {
                Human human = humanslv2.get(i);
                human.updateRectangle();

                if (human.sprite.getY() < -human.sprite.getHeight()) {
                    humanslv2.removeIndex(i);
                } else if (!human.touched && spiderRectangle.overlaps(human.rectangle)) {
                    human.sprite.setTexture(humanTouchedTexture);
                    mortemSound.play();
                    human.touched = true;
                }
            }
        }
    }

    public void input() {
        float speed = 40f, delta = Gdx.graphics.getDeltaTime(), root2 = (float)Math.sqrt(2);

        boolean up = Gdx.input.isKeyPressed(Keys.UP) && !lockUP;
        boolean down = Gdx.input.isKeyPressed(Keys.DOWN) && !lockDOWN;
        boolean left = Gdx.input.isKeyPressed(Keys.LEFT) && !lockLEFT;
        boolean right = Gdx.input.isKeyPressed(Keys.RIGHT) && !lockRIGHT;

        if (up && right || down && right || up && left || down && left) {
            float dx = (right ? 1 : (left ? -1 : 0)) * speed * delta / (root2 * 2);
            float dy = (up ? 1 : (down ? -1 : 0)) * speed * delta / root2;
            spiderSprite.translate(dx, dy);
            moveSpider();
        } else if (right) {
            spiderSprite.translateX(speed * delta);
            moveSpider();
        } else if (left) {
            spiderSprite.translateX(-speed * delta);
            moveSpider();
        } else if (up) {
            spiderSprite.translateY(speed * delta);
            moveSpider();
        } else if (down) {
            spiderSprite.translateY(-speed * delta);
            moveSpider();
        } else {
            spiderMovement = 0;
        }

        // Spawn a new human for testing
        if (Gdx.input.isKeyPressed(Keys.F1)) {
            createHuman();
        } else if (Gdx.input.isKeyJustPressed(Keys.P)) {
            isPixel = !isPixel;
        } else if (Gdx.input.isKeyJustPressed(Keys.NUM_1)) {
            changeMap(1);
        } else if (Gdx.input.isKeyJustPressed(Keys.NUM_2)) {
            changeMap(2);
        }
    }

    public void moveSpider() {
        int direction = 0;
        if (direction == 0) {
            if (++spiderFrame == 5) {
                if (++spiderMovement > 2) direction = 1;
                spiderFrame = 0;
            }
        } else {
            if (++spiderFrame == 5) {
                if (--spiderMovement < 0) direction = 0;
                spiderFrame = 0;
            }
        }
    }

    public void draw() {
        TextureRegion currentFrame = walkAnimation.getKeyFrame(spiderMovement, true);

        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        spriteBatch.begin();
        if (currentMap == 1) {
            spriteBatch.draw(background1, 0, 0, 256, 144);
        } else if (currentMap == 2) {
            spriteBatch.draw(background2, 0, 0, 256, 144);
        }
        spriteBatch.draw(currentFrame, spiderSprite.getX(), spiderSprite.getY(), 16, 16);
        if (currentMap == 1) {
            for (Human human : humanslv1) human.sprite.draw(spriteBatch);
        } else if (currentMap == 2) {
            for (Human human : humanslv2) human.sprite.draw(spriteBatch);
        }
        spriteBatch.end();
    }

    private void createHuman() {
        System.out.println("Human spawned");

        Sprite humanSprite = new Sprite(humanTexture);
        humanSprite.setSize(16, 16);
        float x = MathUtils.random(0f, viewport.getWorldWidth() - 28 - 16) + 14;
        float y = MathUtils.random(0f, viewport.getWorldHeight() - 28 - 16) + 14;
        humanSprite.setPosition(x, y);

        if (currentMap == 1) {
            humanslv1.add(new Human(humanSprite));
        } else if (currentMap == 2) {
            humanslv2.add(new Human(humanSprite));
        }
    }

    public void collisions() {
        // Reset locks before checking collisions
        lockDOWN = lockLEFT = lockUP = lockRIGHT = false;

        if (currentMap == 1) {
            for (Human human : humanslv1) {
                if (Intersector.overlaps(spiderRectangle, human.rectangle)) {
                    boolean xGreater = Math.abs(human.rectangle.x - spiderRectangle.x) > Math.abs(human.rectangle.y - spiderRectangle.y);
                    if (xGreater) {
                        if (human.rectangle.x > spiderRectangle.x) lockRIGHT = true;
                        else lockLEFT = true;
                    } else {
                        if (human.rectangle.y > spiderRectangle.y) lockUP = true;
                        else lockDOWN = true;
                    }
                }
            }
        } else if (currentMap == 1) {
            for (Human human : humanslv2) {
                if (Intersector.overlaps(spiderRectangle, human.rectangle)) {
                    boolean xGreater = Math.abs(human.rectangle.x - spiderRectangle.x) > Math.abs(human.rectangle.y - spiderRectangle.y);
                    if (xGreater) {
                        if (human.rectangle.x > spiderRectangle.x) lockRIGHT = true;
                        else lockLEFT = true;
                    } else {
                        if (human.rectangle.y > spiderRectangle.y) lockUP = true;
                        else lockDOWN = true;
                    }
                }
            }
        }

        if (Intersector.overlaps(spiderRectangle, wall1)) lockLEFT = true;
        if (Intersector.overlaps(spiderRectangle, wall2)) lockUP = true;
        if (Intersector.overlaps(spiderRectangle, wall3)) lockRIGHT = true;
        if (Intersector.overlaps(spiderRectangle, wall4)) lockDOWN = true;
    }

    public void changeMap(int level) {
        if (level == 1) {
            spiderSprite.setPosition(120, 100);
        } else if (level == 2) {
            spiderSprite.setPosition(120, 13);
        }
        currentMap = level;
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
        mortemSound.dispose();
        music.dispose();
    }
}
