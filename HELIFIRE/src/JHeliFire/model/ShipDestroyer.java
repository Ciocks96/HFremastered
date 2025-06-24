package JHeliFire.model;

// Enum per rappresentare le velocità e i punti delle ShipDestroyer
enum ShipDestroyerSpeed {
    SLOW(2, 40),    // Velocità lenta, punti bassi
    MEDIUM(4, 60),  // Velocità media, punti medi
    FAST(6, 80);    // Velocità alta, punti alti

    private final int speed;
    private final int points;

    ShipDestroyerSpeed(int speed, int points) {
        this.speed = speed;
        this.points = points;
    }

    public int getSpeed() {
        return speed;
    }

    public int getPoints() {
        return points;
    }

    // Ottiene una velocità casuale
    public static ShipDestroyerSpeed getRandom() {
        ShipDestroyerSpeed[] speeds = values();
        return speeds[(int)(Math.random() * speeds.length)];
    }
}

public class ShipDestroyer extends Enemy {
    private boolean visible = true;
    // Dimensioni logiche costanti
    public static final int LOGICAL_WIDTH = 64;
    public static final int LOGICAL_HEIGHT = 64;
    
    private final ShipDestroyerSpeed shipType;

    public ShipDestroyer(int x, int y, GameModel gameModel, int level) {
        super(x, y, gameModel);
        this.width = LOGICAL_WIDTH;
        this.height = LOGICAL_HEIGHT;
        
        // Seleziona una velocità casuale usando l'enum e memorizza il tipo
        this.shipType = ShipDestroyerSpeed.getRandom();
        this.speed = shipType.getSpeed();
        
        // La ShipDestroyer non spara mai
        shootProbability = 0.0;
        maxShootCooldown = Integer.MAX_VALUE;
    }
    
    @Override
    protected void movement() {
        // Muove la nave da destra verso sinistra
        x -= speed;
        
        // Se esce dallo schermo a sinistra, segnala che non è più visibile
        if (x + width < 0) {
            visible = false;
        }
    }
    
    @Override
    public int getScoreValue() {
        return shipType.getPoints();
    }
    
    public boolean isVisible() {
        return visible;
    }
}