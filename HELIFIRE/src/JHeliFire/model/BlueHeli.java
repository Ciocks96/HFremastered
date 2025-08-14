package JHeliFire.model;

public class BlueHeli extends Enemy {
    // ======================================
    // Costanti
    // ======================================
    public static final int LOGICAL_WIDTH = 64;
    public static final int LOGICAL_HEIGHT = 64;
    private static final int ANIM_FRAMES = 3;
    private static final int ANIM_DELAY = 8;
    private static final int SPREAD_BULLETS = 3;
    private static final double SPREAD_ANGLE = Math.toRadians(60);
    private static final double BULLET_SPEED = 3.0;
    private static final double BULLET_SPAWN_HEIGHT_RATIO = 0.55;
    private static final int WRAP_AROUND_X = 850;
    private static final double SPEED_LEVEL_FACTOR = 0.2;
    private static final double SHOOT_PROB_BASE = 0.005;
    private static final double SHOOT_PROB_LEVEL_FACTOR = 0.000375;
    private static final int COOLDOWN_BASE = 90;
    private static final int COOLDOWN_LEVEL_FACTOR = 2;
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
        this.level = level;
        this.width = LOGICAL_WIDTH;
        this.height = LOGICAL_HEIGHT;

        // Calcola parametri basati sul livello
        
        speed = (int) Math.min(baseSpeed * (1 + (level - 1) * SPEED_LEVEL_FACTOR), 5);
        shootProbability = Math.min(SHOOT_PROB_BASE + (level * SHOOT_PROB_LEVEL_FACTOR), 0.008);
        maxShootCooldown = Math.max(COOLDOWN_BASE - (level * COOLDOWN_LEVEL_FACTOR), 50);

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
    protected void shootAdvanced() {
        // Pattern di sparo del BlueHeli: spara 3 proiettili a ventaglio
        int bulletSpawnX = x + width / 2 - EnemyBullet.getBulletWidth() / 2;
        int bulletSpawnY = y + (int)(height * BULLET_SPAWN_HEIGHT_RATIO);
        double startAngle = Math.PI / 2 - SPREAD_ANGLE / 2;

        for (int i = 0; i < SPREAD_BULLETS; i++) {
            double angle = startAngle + i * (SPREAD_ANGLE / (SPREAD_BULLETS - 1));
            double vx = Math.cos(angle) * BULLET_SPEED;
            double vy = Math.sin(angle) * BULLET_SPEED;
            EnemyBullet bullet = new EnemyBullet(bulletSpawnX, bulletSpawnY, vx, vy);
            gameModel.addEnemyBullet(bullet);
        }
    }

    @Override
    protected void shoot() {
        if (level <= 3) {
            shootBasic(); // Pattern base per i primi 3 livelli
        } else {
            shootAdvanced(); // Pattern avanzato dal livello 4 in poi
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