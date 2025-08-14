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

    // Ottiene un tipo casuale di ShipDestroyer in base al livello con probabilità pesate
    public static ShipDestroyerSpeed getRandomForLevel(int level) {
        if (level == 2) {
            // Livello 2: solo SLOW
            return SLOW;
        } 
        else if (level == 3) {
            // Livello 3: 60% SLOW, 40% MEDIUM
            double random = Math.random();
            return (random < 0.6) ? SLOW : MEDIUM;
        } 
        else {
            // Livello 4+: 45% SLOW, 35% MEDIUM, 20% FAST
            double random = Math.random();
            if (random < 0.40) {
                return SLOW;       // 40% probabilità
            } else if (random < 0.75) {
                return MEDIUM;     // 35% probabilità
            } else {
                return FAST;       // 25% probabilità
            }
        }
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
        
        // Seleziona casualmente un tipo di ShipDestroyer tra quelli disponibili per il livello
        this.shipType = ShipDestroyerSpeed.getRandomForLevel(level);
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
    protected void shoot() {
        // La ShipDestroyer non spara mai, quindi questo metodo è vuoto
    }
    
    @Override
    public int getScoreValue() {
        return shipType.getPoints();
    }
    
    public boolean isVisible() {
        return visible;
    }
}