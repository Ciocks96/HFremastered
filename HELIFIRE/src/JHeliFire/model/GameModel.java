package JHeliFire.model;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;



import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameModel {
    private int width;
    private Player player;
    private List<EnemyBullet> enemyBullets;
    private List<Bullet> bullets;
    private List<Enemy> enemies;
    private List<EnemyBomb> enemyBombs;
    private List<Explosion> explosions;
    private int score;
    private int highScore;

   
    private int waveIndex = 0;  // 0 = green, 1 = blue, 2 = yellow
    private boolean waveInProgress = false;
    private int level = 1;     

   
    private int greenCount = 5;
    private int blueCount = 3;
    private int yellowCount = 1;

    
    private int shipDestroyerSpawnCooldown = 0;
    private static final int SHIP_DESTROYER_SPAWN_DELAY = 160; // in frame

    private boolean inVictory = false;
    private boolean bonusReady = false;

    private boolean shooting = false;
    private int shootCooldown = 0;
    private static final int SHOOT_DELAY = 20;

    private final java.util.List<GameModelListener> listeners = new CopyOnWriteArrayList<>();
    
    // Listener per eventi sonori
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
        highScore = 0;  
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
        if (e instanceof ShipDestroyer && (e.getX() + e.getWidth() < 0)) {
            enemies.remove(i);
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
            if (b.getHitBox().intersects(enemy.getHitBox())) {
                enemiesToRemove.add(enemy);
                bulletsToRemove.add(b);
                explosions.add(new Explosion(
                        enemy.getX() + enemy.getWidth() / 2, 
                        enemy.getY() + enemy.getHeight() / 2
                    ));
               
                if (soundListener != null) soundListener.onEnemyDestroyed();
                addScore(enemy.getScoreValue()); 
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
                explosions.add(new Explosion(
                    eb.getX() + eb.getWidth() / 2, 
                    eb.getY() + eb.getHeight() / 2, 0.5f
                ));
                if (player.getLives() > 1) {
                    player.loseLife();
                    if (soundListener != null) soundListener.onPlayerHit();
                    // player.setInvulnerabilityTimer(120); // opzionale
                } else {
                    player.loseLife();
                    explosions.add(new Explosion(
                        player.getX() + player.getWidth() / 2, 
                        player.getY() + player.getHeight() / 2
                    ));
                    if (soundListener != null) soundListener.onPlayerDeath();
                }
                enemyBulletsToRemove.add(eb);
                break; 
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
                    if (soundListener != null) soundListener.onPlayerHit();

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
                   if (soundListener != null) soundListener.onPlayerDeath();
                }
                enemies.remove(i);
                i--;
                break;
            }
        }
    }
}
    private void handleEnemyBombPlayerCollisions() {
    List<EnemyBomb> enemyBombsToRemove = new ArrayList<>();
    if (player.isAlive() && player.getInvulnerabilityTimer() == 0) {
        for (EnemyBomb bomb : enemyBombs) {
            if (bomb.getHitBox().intersects(player.getHitBox())) {
                explosions.add(new Explosion(
                    bomb.getX() + bomb.getWidth() / 2,
                    bomb.getY() + bomb.getHeight() / 2, 0.6f 
                ));
                if (player.getLives() > 1) {
                    player.loseLife();
                    if (soundListener != null) soundListener.onPlayerHit();

                } else {
                    player.loseLife(); 
                    explosions.add(new Explosion(
                        player.getX() + player.getWidth() / 2,
                        player.getY() + player.getHeight() / 2
                    ));
                    if (soundListener != null) soundListener.onPlayerDeath();
                }
                enemyBombsToRemove.add(bomb);
                break; 
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


    public int getScore() {
        return score;
    }

    public int getHighScore() {
        return highScore;
    }

    private void spawnWave() {
        waveInProgress = true;
        switch (waveIndex) {
            case 0:
                spawnGreenWave();
                break;
            case 1:
                spawnBlueWave();
                break;
            case 2:
                spawnYellowWave();
                break;
        }
    }

    private boolean waveEnemiesCleared() {
        for (Enemy enemy : enemies) {
            if (!(enemy instanceof ShipDestroyer)) {
                return false;
            }
        }
        return true;
    }
    
    private void spawnGreenWave() {
        for (int i = 0; i < greenCount; i++) {
            if (enemies.size() > 10) {
                break;
            }
            int randomY = 50 + (int)(Math.random() * 200); 
            int spawnX = width + i * 40; 
            enemies.add(new GreenHeli(spawnX, randomY, level, this)); 
        }
    }
    
    private void spawnBlueWave() {
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
        for (int i = 0; i < yellowCount; i++) {
            if (enemies.size() > 8) {
                break;
            }
            int randomY = 175 + (int)(Math.random() * 100);
            int spawnX = width + i * 50;
            enemies.add(new YellowHeli(spawnX, randomY, level, this));
        }
            
        }


    private void spawnShipDestroyer() {
        int seaTop = 300;
        int seaBottom = 600; 
        int randomY = seaTop + (int)(Math.random() * (seaBottom - seaTop - 50));
        int spawnX = width + 50;
        
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

    private int getMaxShipDestroyers() {
        // Ad esempio: al livello 1 o 2, max = 1; al livello 3 o 4, max = 2; 
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

   
    public void addEnemyBullet(EnemyBullet bullet) {
        enemyBullets.add(bullet);
    }

    public void addEnemyBomb(EnemyBomb bomb) {
        enemyBombs.add(bomb);
    }

    public void shoot() {
        int bulletX = player.getX() + 17;
        int bulletY = player.getY();
        addBullet(new Bullet(bulletX, bulletY));
        // Sostituisco SoundManager.playShoot() con notifica
        if (soundListener != null) soundListener.onPlayerShoot();

    }

    public int getLevel() {
        return level;
    }

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
    player.setY(600 - 40 - 30); 

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