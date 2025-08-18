package JHeliFire.model;

import java.awt.Rectangle;

public class Player {
    public static final int LOGICAL_WIDTH = 60;
    public static final int LOGICAL_HEIGHT = 32;
    private static final int INVULNERABILITY_TIME = 180;
    private static final int DEFAULT_SPEED = 3;
    private static final int HITBOX_WIDTH = 50;
    private static final int HITBOX_HEIGHT = 15;
    
    private int x, y;
    private int width, height;
    private int dx = 0, dy = 0;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean upPressed = false;
    private boolean downPressed = false;
    private int speed = DEFAULT_SPEED;
    // Movement limits
    private int seaLeft;
    private int seaRight;
    private int seaTop;
    private int seaBottom;
    // Player state
    private boolean alive = true;
    private int lives = 3;
    private int invulnerabilityTimer = 0;

    public Player(int startX, int startY,
                  int seaLeft, int seaRight,
                  int seaTop, int seaBottom) {
        this.x = startX;
        this.y = startY;
        this.width = LOGICAL_WIDTH;
        this.height = LOGICAL_HEIGHT;
        this.seaLeft = seaLeft;
        this.seaRight = seaRight;
        this.seaTop = seaTop;
        this.seaBottom = seaBottom;
    }

    public Player(int startX, int startY) {
        this(startX, startY, 0, 800, 325, 600);
    }

    public void update() {
        updateMovement();
        updatePosition();
        updateInvulnerability();
    }
    
    private void updateMovement() {
        if (leftPressed && !rightPressed) {
            dx = -speed;
        } else if (rightPressed && !leftPressed) {
            dx = speed;
        } else {
            dx = 0;
        }
        
        if (upPressed && !downPressed) {
            dy = -speed;
        } else if (downPressed && !upPressed) {
            dy = speed;
        } else {
            dy = 0;
        }
        
        /* Normalizza la velocità diagonale
        if (dx != 0 && dy != 0) {
            double diagonalSpeed = speed / Math.sqrt(2);
            dx = (int) (dx > 0 ? diagonalSpeed : -diagonalSpeed);
            dy = (int) (dy > 0 ? diagonalSpeed : -diagonalSpeed);
        }
        */
    }
    
    private void updatePosition() {
        x += dx;
        y += dy;
        
        // Mantiene il player dentro i limiti
        if (x < seaLeft) x = seaLeft;
        if (x + width > seaRight) x = seaRight - width;
        if (y < seaTop) y = seaTop;
        if (y + height > seaBottom) y = seaBottom - height;
    }
    
    private void updateInvulnerability() {
        if (invulnerabilityTimer > 0) invulnerabilityTimer--;
    }
    
    public void loseLife() {
        if (invulnerabilityTimer == 0) {
            lives--;
            if (lives <= 0) {
                lives = 0;
                setAlive(false);
            } else {
                invulnerabilityTimer = INVULNERABILITY_TIME;
            }
        }
    }
    
    public void addLife() {
        if (lives < 3) {
            lives++;
        }
    }
    
    public void setLeftPressed(boolean pressed) {
        this.leftPressed = pressed;
    }
    
    public void setRightPressed(boolean pressed) {
        this.rightPressed = pressed;
    }
    
    public void setUpPressed(boolean pressed) {
        this.upPressed = pressed;
    }
    
    public void setDownPressed(boolean pressed) {
        this.downPressed = pressed;
    }
    
    // ======================================
    // Getters e Setters
    // ======================================
    public void setDx(int dx) { this.dx = dx; }
    public void setDy(int dy) { this.dy = dy; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setAlive(boolean alive) { this.alive = alive; }
    
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isAlive() { return alive; }
    public int getLives() { return lives; }
    public int getInvulnerabilityTimer() { return invulnerabilityTimer; }
    public boolean isInvulnerable() { return invulnerabilityTimer > 0; }
    

    public Rectangle getHitBox() {
        int hitBoxX = x + (width / 2) - (HITBOX_WIDTH / 2);
        int hitBoxY = y + (height / 2) - (HITBOX_HEIGHT / 2);
        return new Rectangle(hitBoxX, hitBoxY, HITBOX_WIDTH, HITBOX_HEIGHT);
    }
}

