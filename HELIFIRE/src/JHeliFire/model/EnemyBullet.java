package JHeliFire.model;

public class EnemyBullet {
    private int x, y;
    private double vx, vy;
    private boolean visible = true;
    // Dimensioni logiche costanti
    public static final int LOGICAL_WIDTH = 10;
    public static final int LOGICAL_HEIGHT = 24;

    // Costruttore classico (compatibilità)
    public EnemyBullet(int x, int y) {
        this(x, y, 0, 3); // dritto verso il basso
    }

    // Nuovo costruttore per velocità personalizzate
    public EnemyBullet(int x, int y, double vx, double vy) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
    }

    // Aggiorna la posizione del proiettile (movimento verticale verso il basso)
    public void update() {
        x += vx;
        y += vy;
        if (y > 600) { // Puoi sostituire 600 con la variabile che rappresenta l'altezza dello schermo
            visible = false;
        }
    }

    // Hitbox più piccola e centrata
    public java.awt.Rectangle getHitBox() {
        int hitBoxWidth = 6;
        int hitBoxHeight = 18;
        int hitBoxX = x + (LOGICAL_WIDTH - hitBoxWidth) / 2;
        int hitBoxY = y + (LOGICAL_HEIGHT - hitBoxHeight) / 2;
        return new java.awt.Rectangle(hitBoxX, hitBoxY, hitBoxWidth, hitBoxHeight);
    }

    public boolean isVisible() {
        return visible;
    }

    // Getter per le coordinate e le dimensioni, utili per le collisioni
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return LOGICAL_WIDTH; }
    public int getHeight() { return LOGICAL_HEIGHT; }
    // Metodo statico per compatibilità con Enemy
    public static int getBulletWidth() { return LOGICAL_WIDTH; }
}