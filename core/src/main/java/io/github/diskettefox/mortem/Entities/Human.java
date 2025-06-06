package io.github.diskettefox.mortem.Entities;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;

public class Human {
    public Sprite sprite;
    public boolean touched;
    public Rectangle rectangle;
    private int map;

    public void setMap(int map) {
        this.map = map;
    }

    public int getMap() {
        return map;
    }

    public Human(Sprite sprite) {
        this.sprite = sprite;
        this.touched = false;
        this.rectangle = new Rectangle(sprite.getX() + 5, sprite.getY(), 7, sprite.getHeight());
    }

    public void updateRectangle() {
        this.rectangle.set(sprite.getX() + 5, sprite.getY(), 7, sprite.getHeight());
    }
}
