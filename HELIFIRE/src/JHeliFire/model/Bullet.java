package JHeliFire.model;

import java.awt.Rectangle;

/**
 * Rappresenta il proiettile sparato dal giocatore.
 * Il proiettile si muove verso l'alto e diventa invisibile quando esce dallo schermo.
 */
public class Bullet {
    // Dimensioni logiche costanti
    public static final int LOGICAL_WIDTH = 10;
    public static final int LOGICAL_HEIGHT = 40;
    
    // Costanti per la hitbox
    private static final int HITBOX_WIDTH = 6;
    private static final int HITBOX_HEIGHT = 28;
    
    // Velocità del proiettile (pixel per frame)
    private static final int BULLET_SPEED = 7;
    
    // Stato del proiettile
    private final int width;
    private final int height;
    private int x;
    private int y;
    private boolean visible;
    
    /**
     * Crea un nuovo proiettile nella posizione specificata.
     *
     * @param x coordinata X iniziale
     * @param y coordinata Y iniziale
     */
    public Bullet(int x, int y) {
        this.x = x;
        this.y = y;
        this.width = LOGICAL_WIDTH;
        this.height = LOGICAL_HEIGHT;
        this.visible = true;
    }
    
    /**
     * Aggiorna la posizione del proiettile.
     * Il proiettile si muove verso l'alto e diventa invisibile quando esce dallo schermo.
     */
    public void update() {
        y -= BULLET_SPEED;
        if (y < 0) {
            visible = false;
        }
    }
    
    /**
     * Restituisce la hitbox del proiettile, più piccola e centrata rispetto alle dimensioni visive.
     * Questa hitbox viene usata per il rilevamento delle collisioni.
     *
     * @return Rectangle che rappresenta l'area di collisione del proiettile
     */
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
