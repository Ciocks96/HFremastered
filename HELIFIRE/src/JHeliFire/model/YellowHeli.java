package JHeliFire.model;

public class YellowHeli extends Enemy {
    // ======================================
    // Costanti
    // ======================================
    public static final int LOGICAL_WIDTH = 64;
    public static final int LOGICAL_HEIGHT = 64;
    private static final int ANIM_FRAMES = 3;
    private static final int ANIM_DELAY = 8;
    
    // Costanti di movimento
    private static final int UPPER_LIMIT = 50;
    private static final int LOWER_LIMIT = 325;
    private static final double DIRECTION_CHANGE_PROBABILITY = 0.2;
    private static final int SCREEN_RIGHT = 800;
    
    // Costanti di combattimento
    private static final int DEFAULT_BURST_MAX = 3;
    private static final int DEFAULT_BURST_DELAY = 6;
    private static final double SPEED_LEVEL_FACTOR = 0.3;
    private static final double BULLET_SPEED = 3.0;
    private static final double BASE_SHOOT_PROBABILITY = 0.008;

    // Costanti per il bilanciamento del livello
    private static final double SHOOT_PROB_LEVEL_POW = 0.5;
    private static final double SHOOT_PROB_LEVEL_FACTOR = 0.0003;
    private static final double SHOOT_PROB_BASE = BASE_SHOOT_PROBABILITY;

    // ======================================
    // Enums
    // ======================================
    private enum MovementState { LEFT, UP, RIGHT, DOWN, DIAGONAL }

    // ======================================
    // Stato di Movimento
    // ======================================
    private MovementState state = MovementState.LEFT;
    private boolean facingLeft = true;
    private int targetX, targetY;
    private MovementState nextStateAfterDiagonal;
    
    // ======================================
    // Animazione
    // ======================================
    private int animIndex = 0;
    private int animCounter = 0;

    // ======================================
    // Movimento e Livello
    // ======================================
    private final int baseSpeed = 3;
    private final int verticalSpeed = 2;

    // ======================================
    // Sistema di Sparo a Raffica
    // ======================================
    private int burstShots = 0;
    private int burstMax = DEFAULT_BURST_MAX;
    private int burstDelay = DEFAULT_BURST_DELAY;
    private int burstCooldown = 0;

    // ======================================
    // Costruttore
    // ======================================
    public YellowHeli(int x, int y, int level, GameModel gameModel) {
        super(x, y, gameModel);
        this.level = level;
        this.width = LOGICAL_WIDTH;
        this.height = LOGICAL_HEIGHT;
        
        speed = Math.min((int)(baseSpeed * (1 + (level - 1) * SPEED_LEVEL_FACTOR)), 8);
        shootProbability = Math.min(SHOOT_PROB_BASE + Math.pow(level, SHOOT_PROB_LEVEL_POW) * SHOOT_PROB_LEVEL_FACTOR, 0.015);
        maxShootCooldown = Math.max(70 - level * 3, 40);
    }

    // ======================================
    // Sistema di Sparo
    // ======================================
    @Override
    protected void handleShooting() {
        if (shootCooldown > 0) {
            shootCooldown--;
            return;
        }

        // Nei primi 3 livelli, usa il sistema di sparo normale
        if (level <= 3) {
            if (Math.random() < shootProbability) {
                shoot();
                shootCooldown = maxShootCooldown;
            }
            return;
        }

        // Dal livello 4 in poi, usa il sistema di raffica
        if (burstShots > 0) {
            handleBurstShots();
        } else {
            tryStartNewBurst();
        }
    }

    private void handleBurstShots() {
        if (burstCooldown > 0) {
            burstCooldown--;
        } else {
            shootAdvanced(); // Dal livello 4 usa sempre il pattern avanzato
            burstShots--;
            burstCooldown = burstDelay;
            if (burstShots == 0) {
                shootCooldown = maxShootCooldown;
            }
        }
    }

    private void tryStartNewBurst() {
        if (Math.random() < shootProbability) {
            burstShots = burstMax;
            shootAdvanced(); // Dal livello 4 usa sempre il pattern avanzato
            burstShots--;
            burstCooldown = burstDelay;
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

    @Override
    protected void shootAdvanced() {
        // YellowHeli spara un proiettile mirato verso il giocatore
        int bulletSpawnX = x + width / 2 - EnemyBullet.getBulletWidth() / 2;
        int bulletSpawnY = y + (int)(height * 0.65);

        // Ottiene la posizione del giocatore
        Player player = gameModel.getPlayer();
        if (player != null) {
            // Calcola la direzione verso il centro del giocatore
            double targetX = player.getX() + player.getWidth()/2;
            double targetY = player.getY() + player.getHeight()/2;
            
            // Calcola direzione e normalizza la velocità
            double dx = targetX - bulletSpawnX;
            double dy = targetY - bulletSpawnY;
            double length = Math.sqrt(dx * dx + dy * dy);
            
            // Aggiungi un po' di predizione del movimento del giocatore
            // ma mantieni una componente verticale minima per non sparare orizzontalmente
            dx = (dx / length) * BULLET_SPEED;
            dy = Math.max((dy / length) * BULLET_SPEED, BULLET_SPEED * 0.5); // Forza una componente verticale minima
            
            // Crea il proiettile
            EnemyBullet bullet = new EnemyBullet(bulletSpawnX, bulletSpawnY, dx, dy);
            gameModel.addEnemyBullet(bullet);
        }
    }

    // ======================================
    // Movimento
    // ======================================
    @Override
    public void movement() {
        handleMovement();
        updateAnimation();
    }

    private void handleMovement() {
        switch(state) {
            case LEFT:
                handleLeftMovement();
                break;
            case UP:
                handleUpMovement();
                break;
            case RIGHT:
                handleRightMovement();
                break;
            case DOWN:
                handleDownMovement();
                break;
            case DIAGONAL:
                handleDiagonalMovement();
                break;
        }
    }

    private void handleLeftMovement() {
        x -= speed;
        if (x <= 0) {
            x = 0;
            if (Math.random() < DIRECTION_CHANGE_PROBABILITY) {
                prepareDiagonalMovement(SCREEN_RIGHT - width, UPPER_LIMIT, MovementState.RIGHT);
            } else {
                state = MovementState.UP;
            }
        }
    }

    private void handleUpMovement() {
        y -= verticalSpeed;
        if (y <= UPPER_LIMIT) {
            y = UPPER_LIMIT;
            if (Math.random() < DIRECTION_CHANGE_PROBABILITY) {
                prepareDiagonalMovement(SCREEN_RIGHT - width, LOWER_LIMIT - height, MovementState.DOWN);
            } else {
                facingLeft = false;
                state = MovementState.RIGHT;
            }
        }
    }

    private void handleRightMovement() {
        x += speed;
        if (x + width >= SCREEN_RIGHT) {
            x = SCREEN_RIGHT - width;
            if (Math.random() < DIRECTION_CHANGE_PROBABILITY) {
                prepareDiagonalMovement(0, LOWER_LIMIT - height, MovementState.LEFT);
            } else {
                facingLeft = true;
                state = MovementState.DOWN;
            }
        }
    }

    private void handleDownMovement() {
        y += verticalSpeed;
        if (y + height >= LOWER_LIMIT) {
            y = LOWER_LIMIT - height;
            if (Math.random() < DIRECTION_CHANGE_PROBABILITY) {
                prepareDiagonalMovement(0, UPPER_LIMIT, MovementState.UP);
            } else {
                facingLeft = true;
                state = MovementState.LEFT;
            }
        }
    }

    private void handleDiagonalMovement() {
        int dx = targetX - x;
        int dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance > 0) {
            double speedX = (dx / distance) * speed;
            double speedY = (dy / distance) * speed;
            x += (int) speedX;
            y += (int) speedY;
            if (Math.abs(targetX - x) <= Math.abs(speedX) && Math.abs(targetY - y) <= Math.abs(speedY)) {
                x = targetX;
                y = targetY;
                state = nextStateAfterDiagonal;
            }
        }
    }

    private void prepareDiagonalMovement(int targetX, int targetY, MovementState nextState) {
        this.targetX = targetX;
        this.targetY = targetY;
        this.nextStateAfterDiagonal = nextState;
        facingLeft = (targetX < x);
        state = MovementState.DIAGONAL;
    }

    private void updateAnimation() {
        animCounter++;
        if (animCounter >= ANIM_DELAY) {
            animCounter = 0;
            animIndex = (animIndex + 1) % ANIM_FRAMES;
        }
    }

    // ======================================
    // Getters
    // ======================================
    @Override
    public int getScoreValue() {
        return 40;
    }

    public int getAnimIndex() {
        return animIndex;
    }

    public int getAnimFrames() {
        return ANIM_FRAMES;
    }

    public boolean isFacingLeft() {
        return facingLeft;
    }


}