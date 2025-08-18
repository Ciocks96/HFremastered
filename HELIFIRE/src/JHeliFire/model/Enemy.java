package JHeliFire.model;

import java.awt.Rectangle;

import JHeliFire.utility.SoundManager;

public abstract class Enemy {
   
    private static final double BOMB_PROBABILITY = 0.2;
    private static final double DEFAULT_SHOOT_PROBABILITY = 0.01;
    private static final int HITBOX_WIDTH = 30;
    private static final int HITBOX_HEIGHT = 20;
    private static final double BULLET_SPAWN_HEIGHT_RATIO = 0.65;

    protected int x, y;
    protected int width, height;
    protected int speed;

    protected double shootProbability = DEFAULT_SHOOT_PROBABILITY;
    protected int shootCooldown = 0;
    protected int maxShootCooldown = 0;

    protected GameModel gameModel;
    protected int level;

    public Enemy(int x, int y, GameModel gameModel) {
        this.x = x;
        this.y = y;
        this.gameModel = gameModel;
    }
    
    public void update() {
        movement();
        handleShooting();
    }

    
    protected abstract void movement();
    
    protected void handleShooting() {
        if (shootCooldown > 0) {
            shootCooldown--;
        } else {
            double randomValue = Math.random();
            if (randomValue < shootProbability) {
                if (Math.random() < BOMB_PROBABILITY) {
                    shootBomb();
                } else {
                    shoot();
                }
                SoundManager.playSound("/assets/sounds/enemyshot.wav");
                shootCooldown = maxShootCooldown;
            }
        }
    }

   
    protected abstract void shoot();
    
   
    protected void shootBasic() {
        int bulletSpawnX = x + width / 2 - EnemyBullet.getBulletWidth() / 2;
        int bulletSpawnY = y + (int)(height * BULLET_SPAWN_HEIGHT_RATIO);
        EnemyBullet bullet = new EnemyBullet(bulletSpawnX, bulletSpawnY, 0, 3); // velocità verticale = 3
        gameModel.addEnemyBullet(bullet);
    }
    
   
    protected void shootAdvanced() {
        // Di default usa il pattern base
        shootBasic();
    }

    protected void shootBomb() {
        int bombSpawnX = x + width / 2 - EnemyBomb.getBombWidth() / 2;
        int bombSpawnY = y + (int)(height * BULLET_SPAWN_HEIGHT_RATIO);
        EnemyBomb bomb = new EnemyBomb(bombSpawnX, bombSpawnY);
        gameModel.addEnemyBomb(bomb);
    }
    
    /* 
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }*/
    
    
    public Rectangle getHitBox() {
        int hitBoxX = x + (width / 2) - (HITBOX_WIDTH / 2);
        int hitBoxY = y + (height / 2) - (HITBOX_HEIGHT / 2);
        return new Rectangle(hitBoxX, hitBoxY, HITBOX_WIDTH, HITBOX_HEIGHT);
    }

    // ======================================
    // Getters e Setters
    // ======================================
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public abstract int getScoreValue();
}