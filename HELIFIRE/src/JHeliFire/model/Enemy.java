package JHeliFire.model;

import java.awt.Rectangle;

public abstract class Enemy {
    // ======================================
    // Costanti
    // ======================================
    private static final double BOMB_PROBABILITY = 0.2;
    private static final double DEFAULT_SHOOT_PROBABILITY = 0.01;
    private static final int HITBOX_WIDTH = 30;
    private static final int HITBOX_HEIGHT = 20;
    private static final double BULLET_SPAWN_HEIGHT_RATIO = 0.65;

    // ======================================
    // Posizione e Dimensioni
    // ======================================
    protected int x, y;
    protected int width, height;
    protected int speed;

    // ======================================
    // Sistema di Sparo
    // ======================================
    protected double shootProbability = DEFAULT_SHOOT_PROBABILITY;
    protected int shootCooldown = 0;
    protected int maxShootCooldown = 0;

    // ======================================
    // Riferimenti e Stato
    // ======================================
    protected GameModel gameModel;
    protected int level;

    // ======================================
    // Costruttore
    // ======================================
    public Enemy(int x, int y, GameModel gameModel) {
        this.x = x;
        this.y = y;
        this.gameModel = gameModel;
    }
    
    // ======================================
    // Metodi di Update
    // ======================================
    /**
     * Metodo finale per l'aggiornamento del nemico.
     * Esegue prima il movimento (definito dalle sottoclassi) e poi gestisce lo sparo.
     */
    public void update() {
        // Movimento specifico (sottoclassi)
        movement();
        
        // Gestione cooldown e sparo
        handleShooting();
    }

    /**
     * Metodo astratto che definisce il movimento specifico di ogni sottoclasse.
     * Ad esempio, GreenHeli si muove in linea retta, YellowHeli con una traiettoria rettangolare, ecc.
     */
    protected abstract void movement();
    
    // ======================================
    // Sistema di Sparo
    // ======================================
    /**
     * Gestione della logica di sparo: se il cooldown è 0 e (Math.random() < shootProbability), il nemico spara.
     */
    protected void handleShooting() {
        if (shootCooldown > 0) {
            shootCooldown--;
        } else {
            double randomValue = Math.random();
            if (randomValue < shootProbability) {
                // Decidi casualmente se sparare una bomba o un proiettile
                if (Math.random() < BOMB_PROBABILITY) {  // 30% di probabilità per sparare una bomba
                    shootBomb();
                } else {
                    shoot();
                }
                shootCooldown = maxShootCooldown;
            }
        }
    }

    protected void shootBomb() {
        int bombSpawnX = x + width / 2 - EnemyBomb.getBombWidth() / 2;
        int bombSpawnY = y + (int)(height * BULLET_SPAWN_HEIGHT_RATIO);
        EnemyBomb bomb = new EnemyBomb(bombSpawnX, bombSpawnY);
        gameModel.addEnemyBomb(bomb);  // Dovrai implementare il metodo addEnemyBomb() in GameModel
    }
    
    /**
     * Metodo per far sparare il nemico (crea un EnemyBullet).
     */
    public void shoot() {
        // Calcola la posizione di spawn del proiettile (centro orizzontale in basso)
        int bulletSpawnX = x + width / 2 - EnemyBullet.getBulletWidth() / 2;
        int bulletSpawnY = y + (int)(height * BULLET_SPAWN_HEIGHT_RATIO); // 75% dell'altezza del nemico
        // Crea un nuovo proiettile
        EnemyBullet bullet = new EnemyBullet(bulletSpawnX, bulletSpawnY);

        // Aggiunge il proiettile al GameModel tramite il riferimento
        gameModel.addEnemyBullet(bullet);
    }
    
    // ======================================
    // Collisioni
    // ======================================
    /**
     * Restituisce la bounding box completa per il rilevamento delle collisioni.
     */
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
    
    /**
     * Restituisce una hitbox più piccola e centrata per collisioni precise.
     */
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

    // ======================================
    // Metodi Astratti
    // ======================================
    /**
     * Restituisce il valore in punti del nemico quando viene distrutto.
     */
    public abstract int getScoreValue();
}