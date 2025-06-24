package JHeliFire.model;

public class BlueHeli extends Enemy {
    // ======================================
    // Costanti
    // ======================================
    public static final int LOGICAL_WIDTH = 64;
    public static final int LOGICAL_HEIGHT = 64;
    private static final int ANIM_FRAMES = 3;
    private static final int ANIM_DELAY = 8;
    private static final double BOMB_PROBABILITY = 0.2;
    private static final int SPREAD_BULLETS = 3;
    private static final double SPREAD_ANGLE = Math.toRadians(60);
    private static final double BULLET_SPEED = 3.0;
    private static final double BULLET_SPAWN_HEIGHT = 0.55;
    private static final int WRAP_AROUND_X = 850;

    // ======================================
    // Animazione
    // ======================================
    private int animIndex = 0;
    private int animCounter = 0;

    // ======================================
    // Movimento e Combattimento
    // ======================================
    private final int baseSpeed = 2;

    // ======================================
    // Costruttore
    // ======================================
    public BlueHeli(int x, int y, int level, GameModel gameModel) {
        super(x, y, gameModel);
        this.width = LOGICAL_WIDTH;
        this.height = LOGICAL_HEIGHT;

        // Calcola parametri basati sul livello
        speed = Math.min((int)(baseSpeed * (1 + (level - 1) * 0.15)), 6);
        shootProbability = Math.min(0.005 + (level * 0.000375), 0.008);
        maxShootCooldown = Math.max(90 - (level * 2), 60);
    }

    // ======================================
    // Metodi di Update
    // ======================================
    @Override
    public void update() {
        movement();
        handleShooting();
        updateAnimation();
    }

    private void updateAnimation() {
        animCounter++;
        if (animCounter >= ANIM_DELAY) {
            animCounter = 0;
            animIndex = (animIndex + 1) % ANIM_FRAMES;
        }
    }

    // ======================================
    // Movimento
    // ======================================
    @Override
    public void movement() {
        x -= speed;
        if (x + width < 0) {
            x = WRAP_AROUND_X;
        }
    }

    // ======================================
    // Sistema di Sparo
    // ======================================
    @Override
    protected void handleShooting() {
        if (shootCooldown > 0) {
            shootCooldown--;
        } else {
            if (Math.random() < shootProbability) {
                if (Math.random() < BOMB_PROBABILITY) {
                    shootBomb();
                } else {
                    shootSpread(SPREAD_BULLETS);
                }
                shootCooldown = maxShootCooldown;
            }
        }
    }

    private void shootSpread(int numBullets) {
        int bulletSpawnX = x + width / 2 - EnemyBullet.getBulletWidth() / 2;
        int bulletSpawnY = y + (int)(height * BULLET_SPAWN_HEIGHT);
        double startAngle = Math.PI / 2 - SPREAD_ANGLE / 2;

        for (int i = 0; i < numBullets; i++) {
            double angle = startAngle + i * (SPREAD_ANGLE / (numBullets - 1));
            double vx = Math.cos(angle) * BULLET_SPEED;
            double vy = Math.sin(angle) * BULLET_SPEED;
            EnemyBullet bullet = new EnemyBullet(bulletSpawnX, bulletSpawnY, vx, vy);
            gameModel.addEnemyBullet(bullet);
        }
    }

    // ======================================
    // Getters
    // ======================================
    @Override
    public int getScoreValue() {
        return 20;
    }

    public int getAnimIndex() {
        return animIndex;
    }

    public int getAnimFrames() {
        return ANIM_FRAMES;
    }

    public boolean isFacingLeft() {
        return true;
    }
}