package JHeliFire.model;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;

//import static JHeliFire.model.GameState.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameModel {
    private int width, height;
    private Player player;
    private List<EnemyBullet> enemyBullets;
    private List<Bullet> bullets;
    private List<Enemy> enemies;
    private List<EnemyBomb> enemyBombs;
    private List<Explosion> explosions;
    private int score;
    private int highScore;

    // Variabili per ondate e livelli
    private int waveIndex = 0;  // 0 = green, 1 = blue, 2 = yellow
    private boolean waveInProgress = false;
    private int level = 1;      // livello di difficoltà (parte da 1)

   // In GameModel, come campi:
    private int greenCount = 5;
    private int blueCount = 3;
    private int yellowCount = 1;

    // Per il cooldown dello ShipDestroyer
    private int shipDestroyerSpawnCooldown = 0;
    private static final int SHIP_DESTROYER_SPAWN_DELAY = 160; // in frame

    private boolean inVictory = false;
    private boolean bonusReady = false;

    

    
    

    
   

    // Per il cooldown dello sparo del player
    private boolean shooting = false;
    private int shootCooldown = 0;
    private static final int SHOOT_DELAY = 20;

    private final java.util.List<GameModelListener> listeners = new CopyOnWriteArrayList<>();
    
    // Listener per eventi sonori (da implementare nel Controller)
    public interface GameSoundListener {
        void onPlayerHit();
        void onEnemyDestroyed();
        void onPlayerShoot();
        void onPlayerDeath();
    }

    private GameSoundListener soundListener;

    public void setSoundListener(GameSoundListener listener) {
        this.soundListener = listener;
    }
    
    public GameModel(int width, int height) {
        this.width = width;
        highScore = 0;  // Inizializza il punteggio massimo a 0
        init();
    }

    public void init() {
       // Inizializza player e liste
       player = new Player(width / 2 - 20, 600 - 40 - 30);//aggiungi variabile per il mare
       enemies = new ArrayList<>();
       bullets = new ArrayList<>();
       enemyBombs = new ArrayList<>();
       enemyBullets = new ArrayList<>();
       explosions = new ArrayList<>();
       score = 0;
       
       waveIndex = 0; 
       level = 1;
       waveInProgress = false;
       inVictory = false;
       // Valori base per i nemici
        greenCount = 5;
        blueCount = 3;
        yellowCount = 1;
       
       // Avvia la prima ondata
       spawnWave();
        // Reset dei parametri di difficoltà
       // level = DEFAULT_LEVEL;
       notifyModelChanged();
    }

    
    public void update() {
        // Aggiorna il giocatore
        updatePlayer();
        // Aggiorna i proiettili del giocatore
        updatePlayerBullets();
        // Aggiorna i nemici
        updateEnemies();
        // Aggiorna i proiettili dei nemici
        updateEnemyBullets();
        // Aggiorna le bombe dei nemici
        updateEnemyBombs();
        // Aggiorna le esplosioni
        updateExplosions();
        // Gestione delle collisioni tra proiettili del giocatore e nemici
        handlePlayerBulletEnemyCollisions();
        // Collisioni tra proiettili dei nemici e il giocatore
        handleEnemyBulletPlayerCollisions();
        // Collisione tra ShipDestroyer e il giocatore
        handleShipDestroyerPlayerCollision();
        // Gestione delle collisioni tra bombe nemiche e il giocatore
        handleEnemyBombPlayerCollisions();
        // Gestione della progressione delle ondate e dei livelli
        handleWaveAndLevelProgression();
        // Gestione spawn ShipDestroyer
        handleShipDestroyerSpawning();    
        notifyModelChanged();
  }
    private void  updatePlayer() {
        // Aggiorna il giocatore
            player.update();
            if (shooting && player.isAlive()) {
            if (shootCooldown <= 0) {
                shoot();
                shootCooldown = SHOOT_DELAY;
            } else {
                shootCooldown--;
            }
        } else if (shootCooldown > 0) {
            shootCooldown--;
        }
    }
    private void updatePlayerBullets() {
    for (int i = 0; i < bullets.size(); i++) {
        Bullet b = bullets.get(i);
        b.update();
        if (!b.isVisible()) {
            bullets.remove(i);
            i--;
        }
    }
}
    private void updateEnemies() {
    for (int i = 0; i < enemies.size(); i++) {
        Enemy e = enemies.get(i);
        e.update();
        // Se il nemico è un ShipDestroyer ed è uscito dallo schermo a sinistra, rimuovilo
        if (e instanceof ShipDestroyer && (e.getX() + e.getWidth() < 0)) {
            enemies.remove(i);//lo rimuovo perchè sennò rimane in memoria e non vale 
                // if (waveInProgress && enemies.isEmpty()
                // e non va aventi il livello perchè non si verifivano le condizioni
            i--;
        }
    }
}
    private void updateEnemyBullets() {
    for (int i = 0; i < enemyBullets.size(); i++) {
        EnemyBullet eb = enemyBullets.get(i);
        eb.update();
        if (!eb.isVisible()) {
            enemyBullets.remove(i);
            i--;
        }
    }
}
    private void updateEnemyBombs() {
    for (int i = 0; i < enemyBombs.size(); i++) {
        EnemyBomb bomb = enemyBombs.get(i);
        bomb.update();
        if (!bomb.isVisible()) {
            enemyBombs.remove(i);
            i--;
        }
    }
}
    private void updateExplosions() {
    for (int i = 0; i < explosions.size(); i++) {
        Explosion ex = explosions.get(i);
        ex.update();
        if (!ex.isActive()) {
            explosions.remove(i);
            i--;
        }
    }
}
    private void handlePlayerBulletEnemyCollisions() {
    List<Enemy> enemiesToRemove = new ArrayList<>();
    List<Bullet> bulletsToRemove = new ArrayList<>();

    for (Bullet b : bullets) {
        for (Enemy enemy : enemies) {
            // Utilizza getHitBox() per avere collisioni più precise
            if (b.getHitBox().intersects(enemy.getHitBox())) {
                enemiesToRemove.add(enemy);
                bulletsToRemove.add(b);
                explosions.add(new Explosion(
                        enemy.getX() + enemy.getWidth() / 2, 
                        enemy.getY() + enemy.getHeight() / 2
                    ));
                // Sostituisco SoundManager.playExplosion() con notifica
                if (soundListener != null) soundListener.onEnemyDestroyed();
                addScore(enemy.getScoreValue()); 
                // Esci dal ciclo interno se il bullet ha colpito un nemico
                break;
            }
        }
    }
    enemies.removeAll(enemiesToRemove);
    bullets.removeAll(bulletsToRemove);
}
    private void handleEnemyBulletPlayerCollisions() {
    List<EnemyBullet> enemyBulletsToRemove = new ArrayList<>();
    if (player.isAlive() && player.getInvulnerabilityTimer() == 0) {
        for (EnemyBullet eb : enemyBullets) {
            if (eb.getHitBox().intersects(player.getHitBox())) {
                // Aggiungi l'esplosione del proiettile
                explosions.add(new Explosion(
                    eb.getX() + eb.getWidth() / 2, 
                    eb.getY() + eb.getHeight() / 2, 0.5f
                ));
                if (player.getLives() > 1) {
                    player.loseLife();
                    // Sostituisco SoundManager.playSound("/assets/sounds/lifeLost.wav") con notifica
                    if (soundListener != null) soundListener.onPlayerHit();
                    // player.setInvulnerabilityTimer(120); // opzionale
                } else {
                    player.loseLife();
                    explosions.add(new Explosion(
                        player.getX() + player.getWidth() / 2, 
                        player.getY() + player.getHeight() / 2
                    ));
                    // Sostituisco SoundManager.playExplosion() (morte player) con notifica
                    if (soundListener != null) soundListener.onPlayerDeath();
                }
                enemyBulletsToRemove.add(eb);
                break; // Esci dal ciclo dopo aver colpito il player
            }
        }
    }
    enemyBullets.removeAll(enemyBulletsToRemove);
}
    private void handleShipDestroyerPlayerCollision() {
    for (int i = 0; i < enemies.size(); i++) {
        Enemy enemy = enemies.get(i);
        if (enemy instanceof ShipDestroyer) {
            if (player.isAlive() && player.getInvulnerabilityTimer() == 0 &&
                enemy.getHitBox().intersects(player.getHitBox())) {
                if (player.getLives() > 1) {
                    player.loseLife();
                    explosions.add(new Explosion(
                        enemy.getX() + enemy.getWidth() / 2,
                        enemy.getY() + enemy.getHeight() / 2
                    ));
                    // Sostituisco SoundManager.playSound("/assets/sounds/lifeLost.wav") con notifica
                    if (soundListener != null) soundListener.onPlayerHit();
                    // player.setInvulnerabilityTimer(120); // opzionale
                } else {
                    player.loseLife();
                    explosions.add(new Explosion(
                        player.getX() + player.getWidth() / 2,
                        player.getY() + player.getHeight() / 2
                    ));
                    explosions.add(new Explosion(
                        enemy.getX() + enemy.getWidth() / 2,
                        enemy.getY() + enemy.getHeight() / 2
                    ));
                   // Sostituisco SoundManager.playExplosion() (morte player) con notifica
                   if (soundListener != null) soundListener.onPlayerDeath();
                }
                enemies.remove(i);
                i--;
                break; // Esci dal ciclo dopo la collisione
            }
        }
    }
}
    private void handleEnemyBombPlayerCollisions() {
    List<EnemyBomb> enemyBombsToRemove = new ArrayList<>();
    if (player.isAlive() && player.getInvulnerabilityTimer() == 0) {
        for (EnemyBomb bomb : enemyBombs) {
            if (bomb.getHitBox().intersects(player.getHitBox())) {
                // aggiungi l'esplosione della bomba
                explosions.add(new Explosion(
                    bomb.getX() + bomb.getWidth() / 2,
                    bomb.getY() + bomb.getHeight() / 2, 0.6f 
                ));
                if (player.getLives() > 1) {
                    player.loseLife();
                    // Sostituisco SoundManager.playSound("/assets/sounds/lifeLost.wav") con notifica
                    if (soundListener != null) soundListener.onPlayerHit();
                    // player.setInvulnerabilityTimer(120); // opzionale
                } else {
                    player.loseLife(); // Ora le vite diventano 0 e il giocatore muore
                    explosions.add(new Explosion(
                        player.getX() + player.getWidth() / 2,
                        player.getY() + player.getHeight() / 2
                    ));
                    // Sostituisco SoundManager.playExplosion() (morte player) con notifica
                    if (soundListener != null) soundListener.onPlayerDeath();
                }
                enemyBombsToRemove.add(bomb);
                break; // Esci dal ciclo se il giocatore è stato colpito
            }
        }
    }
    enemyBombs.removeAll(enemyBombsToRemove);
}

    
private void handleWaveAndLevelProgression() {
    if (waveInProgress && waveEnemiesCleared()) {
        waveInProgress = false;
        waveIndex++;

        if (waveIndex > 2) {
            waveIndex = 0;

            // Controllo speciale per attivazione BONUS al termine del livello 4
           if  (level == 4 && score >= 1000) {
                bonusReady = true;
                return;
            }

            if (level < 7) {
                Timer levelTimer = new Timer(2000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        level++;
                        greenCount  = 5 + level;
                        blueCount   = 3 + (level - 1) / 2;
                        yellowCount = 1 + (level - 1) / 2;
                        spawnWave();
                        ((Timer)evt.getSource()).stop();
                    }
                });
                levelTimer.setRepeats(false);
                levelTimer.start();
            } else {
                inVictory = true;
            }
        } else {
            spawnWave();
        }
    }
}
   private void handleShipDestroyerSpawning() {
    if (shipDestroyerSpawnCooldown > 0) {
        shipDestroyerSpawnCooldown--;
        return;
    }
    if (level >= 2 && Math.random() < 0.02 && getShipDestroyerCount() < getMaxShipDestroyers()) {
        spawnShipDestroyer();
        shipDestroyerSpawnCooldown = Math.max(80, SHIP_DESTROYER_SPAWN_DELAY - level * 10);
    }
}

    public void updateHighScore() {
        if (score > highScore) {
            highScore = score;
        }
    }
    public void addScore(int points) {
        score += points;
        notifyModelChanged();
    }

    // Getter per il punteggio corrente
    public int getScore() {
        return score;
    }

    // Getter per il high score
    public int getHighScore() {
        return highScore;
    }

    /**
     * Spawna un'ondata di nemici in base a waveIndex e level.
     */
    private void spawnWave() {
        waveInProgress = true;
        switch (waveIndex) {
            case 0:
                // Spawn Green
                spawnGreenWave();
                break;
            case 1:
                // Spawn Blue
                spawnBlueWave();
                break;
            case 2:
                // Spawn Yellow
                spawnYellowWave();
                break;
        }
    }

    private boolean waveEnemiesCleared() {
        // Restituisce true se non ci sono nemici che NON sono ShipDestroyer
        for (Enemy enemy : enemies) {
            if (!(enemy instanceof ShipDestroyer)) {
                return false;
            }
        }
        return true;
    }
    
    private void spawnGreenWave() {
        // Esempio: spawn 8 green elicotteri
        // Magari spawna gradualmente, oppure tutti insieme
        for (int i = 0; i < greenCount; i++) {
            if (enemies.size() > 10) {
                break;
            }
            int randomY = 50 + (int)(Math.random() * 200); // tra 50 e 250
            int spawnX = width + i * 40; // si dispongono un po' a destra
            enemies.add(new GreenHeli(spawnX, randomY, level, this)); 
        }
    }
    
    private void spawnBlueWave() {
        // Simile a spawnGreenWave ma con BlueHeli
        for (int i = 0; i < blueCount; i++) {
            if(enemies.size() > 10) {
                break;
            }
            int randomY = 50 + (int)(Math.random() * 200);
            int spawnX = width + i * 40;
            enemies.add(new BlueHeli(spawnX, randomY, level, this));
        }
       
    }
    
    private void spawnYellowWave() {
        // Spawn Yellow, con traiettoria più complessa
        for (int i = 0; i < yellowCount; i++) {
            if (enemies.size() > 8) {
                break;
            }
            int randomY = 175 + (int)(Math.random() * 100); // Alzato per evitare che sia dentro il mare
            int spawnX = width + i * 50;
            enemies.add(new YellowHeli(spawnX, randomY, level, this));
        }
            
        }


    private void spawnShipDestroyer() {
        
        // Definisci un range Y per il "mare"
        int seaTop = 300;    // adatta ai tuoi valori
        int seaBottom = 600; // adatta ai tuoi valori
        
        // Posizione Y casuale entro il mare
        int randomY = seaTop + (int)(Math.random() * (seaBottom - seaTop - 50));
        
        // Posizione X di spawn (ad es. sulla destra fuori dallo schermo)
        int spawnX = width + 50; // se width è la larghezza della finestra
        
        // Crea l'istanza
        ShipDestroyer destroyer = new ShipDestroyer(spawnX, randomY, this, level);
        enemies.add(destroyer);
    }
    

    private int getShipDestroyerCount() {
        int count = 0;
        for (Enemy enemy : enemies) {
            if (enemy instanceof ShipDestroyer) {
                count++;
            }
        }
       
        return count;
    }

    // Funzione per determinare il numero massimo di ShipDestroyer in base al livello
    private int getMaxShipDestroyers() {
        // Ad esempio: al livello 1 o 2, max = 1; al livello 3 o 4, max = 2; al livello 5 o 6, max = 3; al livello 7 o 8, max = 4.
       return Math.min(1 + level / 2, 5);
    }

    public Player getPlayer() {
        return player;
    }

    
    public List<Bullet> getBullets() {
        return bullets;
    }

    public void addBullet(Bullet bullet) {
        bullets.add(bullet);
    }

    // Metodo per aggiungere un EnemyBullet alla lista
    public void addEnemyBullet(EnemyBullet bullet) {
        enemyBullets.add(bullet);
    }

    public void addEnemyBomb(EnemyBomb bomb) {
        enemyBombs.add(bomb);
    }

    // Metodo per il tiro del giocatore
    public void shoot() {
        int bulletX = player.getX() + 17;
        int bulletY = player.getY();
        addBullet(new Bullet(bulletX, bulletY));
        // Sostituisco SoundManager.playShoot() con notifica
        if (soundListener != null) soundListener.onPlayerShoot();

    }
    //getter level
    public int getLevel() {
        return level;
    }

    // Setter per lo sparo del giocatore, usato dall'InputHandler
    public void setShooting(boolean shooting) {
        this.shooting = shooting;
    }

    public boolean isInVictory() {
        return inVictory;
    }
    
    public void setInVictory(boolean inVictory) {
        this.inVictory = inVictory;
    }

    public boolean isBonusReady() {
    return bonusReady;
    }

    public void setBonusReady(boolean bonusReady) {
        this.bonusReady = bonusReady;
    }

    public void startNextLevel() {
    level++;
    waveIndex = 0;
    waveInProgress = false;
    inVictory = false;

    player.setX(width / 2 - 20);
    player.setY(600 - 40 - 30); // Reset del giocatore alla posizione iniziale

    enemies.clear();
    bullets.clear();
    enemyBombs.clear();
    enemyBullets.clear();
    explosions.clear();

    // Aumenta la difficoltà
    greenCount += 1;
    blueCount += 1;
    if (level % 2 == 0) {
        yellowCount += 1;
    }

    spawnWave(); // Avvia la nuova ondata con i contatori aggiornati
    notifyModelChanged();
}

public void addListener(GameModelListener l) {
    listeners.add(l);
}

public void removeListener(GameModelListener l) {
    listeners.remove(l);
}

private void notifyModelChanged() {
    for (GameModelListener l : listeners) {
        l.onModelChanged();
    }
}

public List<Enemy> getEnemies() {
    return enemies;
}

public List<EnemyBomb> getEnemyBombs() {
    return enemyBombs;
}

public List<EnemyBullet> getEnemyBullets() {
    return enemyBullets;
}

public List<Explosion> getExplosions() {
    return explosions;
}

}