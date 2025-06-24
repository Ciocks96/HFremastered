package JHeliFire.model;

public class Explosion {
    private int x, y;
    private int frameCount = 0;
    private int duration = 20;
    private float scale;
    public static final int LOGICAL_WIDTH = 32;
    public static final int LOGICAL_HEIGHT = 32;

    /**
     * Costruttore standard: utilizza la scala 1.0 (dimensione originale).
     * Le coordinate fornite sono quelle del centro dell'esplosione.
     */
    public Explosion(int centerX, int centerY) {
        this(centerX, centerY, 1.0f);
    }
    
    /**
     * Costruttore con fattore di scala personalizzato.
     * Le coordinate (centerX, centerY) rappresentano il centro dell'esplosione.
     * @param scale Fattore di scala (ad es. 0.5 per il 50% delle dimensioni originali)
     */
    public Explosion(int centerX, int centerY, float scale) {
        this.scale = scale;
        int w = LOGICAL_WIDTH;
        int h = LOGICAL_HEIGHT;
        int effectiveWidth = (int)(w * scale);
        int effectiveHeight = (int)(h * scale);
        this.x = centerX - effectiveWidth / 2;
        this.y = centerY - effectiveHeight / 2;
    }
    
    public void update() {
        frameCount++;
    }
    
    public boolean isActive() {
        return frameCount < duration;
    }
    
    // Getter per rendering logico
    public int getAnimIndex() {
        int idx = frameCount / 5;
        if (idx >= 4) idx = 3;
        return idx;
    }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return (int)(LOGICAL_WIDTH * scale); }
    public int getHeight() { return (int)(LOGICAL_HEIGHT * scale); }
    public float getScale() { return scale; }
}