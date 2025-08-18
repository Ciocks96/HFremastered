package JHeliFire.model;

public class EnemyBomb {
    private int x;
    private int y;
    private boolean visible = true;
    private int speedY;

   
    public static final int LOGICAL_WIDTH = 14;
    public static final int LOGICAL_HEIGHT = 24;

    public EnemyBomb(int x, int y) {
        this.x = x;
        this.y = y;
        this.speedY = 2;
    }

    public void update() {
        y += speedY;
        if (y > 600) {
            visible = false;
        }
    }

    
    public java.awt.Rectangle getHitBox() {
        int hitBoxWidth = 14;
        int hitBoxHeight = 14;
        int hitBoxX = x + (LOGICAL_WIDTH - hitBoxWidth) / 2;
        int hitBoxY = y + (LOGICAL_HEIGHT - hitBoxHeight) / 2;
        return new java.awt.Rectangle(hitBoxX, hitBoxY, hitBoxWidth, hitBoxHeight);
    }

    public boolean isVisible() {
        return visible;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return LOGICAL_WIDTH;
    }

    public int getHeight() {
        return LOGICAL_HEIGHT;
    }

    public static int getBombWidth() {
        return LOGICAL_WIDTH;
    }
}