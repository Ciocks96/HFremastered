package JHeliFire.model;

import java.awt.Rectangle;

public class Bullet {
   
    public static final int LOGICAL_WIDTH = 10;
    public static final int LOGICAL_HEIGHT = 40;
    private static final int HITBOX_WIDTH = 6;
    private static final int HITBOX_HEIGHT = 28;
    private static final int BULLET_SPEED = 7;
    
   
    private final int width;
    private final int height;
    private int x;
    private int y;
    private boolean visible;
    
    public Bullet(int x, int y) {
        this.x = x;
        this.y = y;
        this.width = LOGICAL_WIDTH;
        this.height = LOGICAL_HEIGHT;
        this.visible = true;
    }
    
   
    public void update() {
        y -= BULLET_SPEED;
        if (y < 0) {
            visible = false;
        }
    }
    
    
    public Rectangle getHitBox() {
        int hitBoxX = x + (width - HITBOX_WIDTH) / 2;
        int hitBoxY = y + (height - HITBOX_HEIGHT) / 2;
        return new Rectangle(hitBoxX, hitBoxY, HITBOX_WIDTH, HITBOX_HEIGHT);
    }
    
    // Getters
    public boolean isVisible() { return visible; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
