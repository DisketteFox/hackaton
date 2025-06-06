// MainGameScreen.java
package io.github.diskettefox.mortem.Screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.diskettefox.mortem.Main;

public class MainGameScreen implements Screen {
    private final Main game;
    private SpriteBatch spriteBatch;
    private FitViewport viewport;

    private Texture background1, background2, background3, background4, humanTexture, humanTouchedTexture, spiderSheet;
    private Rectangle wall1 = new Rectangle(), wall2 = new Rectangle(), wall3 = new Rectangle(), wall4 = new Rectangle();
    private Rectangle gate1 = new Rectangle(), gate2 = new Rectangle(), gate3 = new Rectangle(), gate4 = new Rectangle();
    private Rectangle spiderRectangle = new Rectangle();
    private Array<Human> humans = new Array<>();
    private Sprite spiderSprite = new Sprite();
    private Animation<TextureRegion> walkAnimation;
    private static final int FRAME_COLS = 3, FRAME_ROWS = 1;
    private int spiderMovement = 0, spiderFrame = 0;
    private int currentMap = 1;
    private boolean isPixel = false;

    private Music music;
    private Sound mortemSound;

    private boolean lockUP = false, lockDOWN = false, lockLEFT = false, lockRIGHT = false;

    public MainGameScreen(Main game) {
        this.game = game;
    }

    private static class Human {
        Sprite sprite;
        boolean touched;
        Rectangle rectangle;
        int map;

        public void setMap(int map) {
            this.map = map;
        }

        public int getMap() {
            return map;
        }

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
    public void show() {
        spiderSprite.setSize(16, 16);
        spiderSprite.setPosition(128, 32);

        spiderSheet = new Texture("characters/spider/spider.png");
        TextureRegion[] walkFrames = {
            new TextureRegion(spiderSheet, 0, 0, 16, 16),
            new TextureRegion(spiderSheet, 16, 0, 16, 16),
            new TextureRegion(spiderSheet, 32, 0, 16, 16)
        };
        walkAnimation = new Animation<>(1f, walkFrames);

        humanTexture = new Texture("characters/human/human-1.png");
        humanTouchedTexture = new Texture("characters/human/human-d.png");

        background1 = new Texture("stages/test/test1.png");
        background2 = new Texture("stages/test/test2.png");
        background3 = new Texture("stages/test/test3.png");
        background4 = new Texture("stages/test/test4.png");

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
    public void render(float delta) {
        input();
        logic();
        draw();
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
        } else if (currentMap == 3) {
            spriteBatch.draw(background3, 0, 0, 256, 144);
        } else if (currentMap == 4) {
            spriteBatch.draw(background4, 0, 0, 256, 144);
        }
        spriteBatch.draw(currentFrame, spiderSprite.getX(), spiderSprite.getY(), 16, 16);
        for (Human human : humans){
            if (human.getMap() == currentMap) {
                human.sprite.draw(spriteBatch);
            }
        }
        spriteBatch.end();
    }

    private void input() {
        float speed = 40f, delta = Gdx.graphics.getDeltaTime(), root2 = (float) Math.sqrt(2);

        boolean up = Gdx.input.isKeyPressed(Input.Keys.UP) && !lockUP;
        boolean down = Gdx.input.isKeyPressed(Input.Keys.DOWN) && !lockDOWN;
        boolean left = Gdx.input.isKeyPressed(Input.Keys.LEFT) && !lockLEFT;
        boolean right = Gdx.input.isKeyPressed(Input.Keys.RIGHT) && !lockRIGHT;

        if ((up && right) || (down && right) || (up && left) || (down && left)) {
            float dx = (right ? 1 : (left ? -1 : 0)) * speed * delta / (root2 * 2);
            float dy = (up ? 1 : (down ? -1 : 0)) * speed * delta / root2;
            spiderSprite.translate(dx, dy);
            moveSpider();
        } else if (right || left || up || down) {
            if (right) spiderSprite.translateX(speed * delta);
            if (left) spiderSprite.translateX(-speed * delta);
            if (up) spiderSprite.translateY(speed * delta);
            if (down) spiderSprite.translateY(-speed * delta);
            moveSpider();
        } else {
            spiderMovement = 0;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) createHuman();
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) isPixel = !isPixel;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) changeMap(1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) changeMap(2);
    }

    private void logic() {
        collisions();

        spiderRectangle.set(spiderSprite.getX(), spiderSprite.getY(), spiderSprite.getWidth(), spiderSprite.getHeight());
        wall1.set(0, 0, 14, 144);
        wall2.set(0, 115, 256, 14);
        wall3.set(242, 0, 14, 144);
        wall4.set(0, 0, 256, 14);

        if (currentMap == 1) {
            gate1.set(120, 109, 15, 16);
            if (Intersector.overlaps(spiderRectangle, gate1)) {
                changeMap(2);
            }
        } else if (currentMap == 2) {
            gate1.set(120, 0, 15, 14);
            gate2.set(242, 58, 14, 15);
            gate3.set(120, 109, 15, 16);
            if (Intersector.overlaps(spiderRectangle, gate1)) {
                changeMap(1);
            }
            if (Intersector.overlaps(spiderRectangle, gate2)) {
                changeMap(4);
            }
            if (Intersector.overlaps(spiderRectangle, gate3)) {
                changeMap(3);
            }
        } else if (currentMap == 3) {
            gate1.set(120, 0, 15, 14);
            if (Intersector.overlaps(spiderRectangle, gate1)) {
                changeMap(2);
            }
        } else if (currentMap == 4) {
            gate1.set(0, 58, 14, 15);
            if (Intersector.overlaps(spiderRectangle, gate1)) {
                changeMap(2);
            }
        }

        for (Human human : humans) {
            int i = 0;
            if (human.getMap() == currentMap) {
                human.updateRectangle();
                if (human.sprite.getY() < -human.sprite.getHeight()) {
                    humans.removeIndex(i);
                } else if (!human.touched && spiderRectangle.overlaps(human.rectangle)) {
                    human.sprite.setTexture(humanTouchedTexture);
                    mortemSound.play();
                    human.touched = true;
                }
            }
            i++;
        }
    }

    private void moveSpider() {
        if (++spiderFrame == 5) {
            if (++spiderMovement > 2) spiderMovement = 0;
            spiderFrame = 0;
        }
    }

    private void createHuman() {
        Sprite humanSprite = new Sprite(humanTexture);
        humanSprite.setSize(16, 16);
        float x = MathUtils.random(14f, viewport.getWorldWidth() - 14f - 16f);
        float y = MathUtils.random(14f, viewport.getWorldHeight() - 28f - 16f);
        humanSprite.setPosition(x, y);
        Human human = new Human(humanSprite);
        human.setMap(currentMap);
        humans.add(human);
    }

    private void collisions() {
        lockDOWN = lockLEFT = lockUP = lockRIGHT = false;

        for (Human human : humans) {
            if (Intersector.overlaps(spiderRectangle, human.rectangle) && human.getMap() == currentMap) {
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

        if (Intersector.overlaps(spiderRectangle, wall1)) lockLEFT = true;
        if (Intersector.overlaps(spiderRectangle, wall2)) lockUP = true;
        if (Intersector.overlaps(spiderRectangle, wall3)) lockRIGHT = true;
        if (Intersector.overlaps(spiderRectangle, wall4)) lockDOWN = true;
    }

    public void changeMap(int map) {
        if (map == 1) {
            spiderSprite.setPosition(120, 90);
        } else if (map == 2) {
            if (currentMap == 1) {
                spiderSprite.setPosition(120, 20);
            } if (currentMap == 3) {
                spiderSprite.setPosition(120, 90);
            } if (currentMap == 4) {
                spiderSprite.setPosition(220, 71);
            }
        } else if (map == 3) {
            spiderSprite.setPosition(120, 20);
        } else if (map == 4) {
            spiderSprite.setPosition(20, 71);
        }
        currentMap = map;
    }

    @Override public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        spriteBatch.dispose();
        humanTexture.dispose();
        humanTouchedTexture.dispose();
        mortemSound.dispose();
        music.dispose();
    }
}
