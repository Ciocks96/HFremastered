package JHeliFire.model;

public class GreenHeli extends Enemy {
    // ======================================
    // Costanti
    // ======================================
    public static final int LOGICAL_WIDTH = 64;
    public static final int LOGICAL_HEIGHT = 64;
    private static final int ANIM_FRAMES = 3;
    private static final int ANIM_DELAY = 8;
    private static final int WRAP_AROUND_X = 850;
    
    // Costanti per il bilanciamento del livello
    private static final double SPEED_LEVEL_FACTOR = 0.1;
    private static final double SHOOT_PROB_BASE = 0.004;
    private static final double SHOOT_PROB_LEVEL_FACTOR = 0.0003;
    private static final int COOLDOWN_BASE = 100;
    private static final int COOLDOWN_LEVEL_FACTOR = 4;

    // ======================================
    // Animazione
    // ======================================
    private int animIndex = 0;
    private int animCounter = 0;

    // ======================================
    // Movimento e Livello
    // ======================================
    private final int level;
    private final int baseSpeed = 2;

    // ======================================
    // Costruttore
    // ======================================
    public GreenHeli(int x, int y, int level, GameModel gameModel) {
        super(x, y, gameModel);
        this.level = level;
        this.width = LOGICAL_WIDTH;
        this.height = LOGICAL_HEIGHT;
        
        speed = (int) Math.min(baseSpeed * (1 + (level - 1) * SPEED_LEVEL_FACTOR), 4);
        shootProbability = Math.min(SHOOT_PROB_BASE + (level * SHOOT_PROB_LEVEL_FACTOR), 0.007);
        maxShootCooldown = Math.max(COOLDOWN_BASE - (level * COOLDOWN_LEVEL_FACTOR), 60);
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
    // Getters
    // ======================================
    @Override
    public int getScoreValue() {
        return 10;
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