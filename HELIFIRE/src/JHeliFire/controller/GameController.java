package JHeliFire.controller;

import static JHeliFire.model.GameState.*;

import JHeliFire.model.GameModel;
import JHeliFire.view.GamePanel;

public class GameController implements JHeliFire.model.GameModel.GameSoundListener {
    // ======================================
    // Fields
    // ======================================
    private final GameModel model;
    private final GamePanel view;
    private int gameState;
    private int previousState;
    private String nameBuffer;
    private int pendingScore;
    private javax.swing.Timer gameOverTimer;
    private javax.swing.Timer victoryTimer;
    private javax.sound.sampled.Clip bonusClip;

    // ======================================
    // Constructor
    // ======================================
    public GameController(GameModel model, JHeliFire.view.GamePanel view) {
        this.model = model;
        this.view = view;
        this.gameState = START_SCREEN; // Stato iniziale
        this.model.setSoundListener(this); // Collega il listener sonoro
    }

    // ======================================
    // Core Game Loop Methods
    // ======================================
    public void onTick() {
        switch (gameState) {
            case GAME_PLAY:
                model.update();
                checkPlayerState();
                updateBackground();
                break;
            case BONUS_CUTSCENE:
                view.getBonusManager().update();
                updateBackground();
                if (view.getBonusManager().isComplete()) {
                    onBonusComplete();
                    view.getBonusManager().reset();
                    if (view.getBackgroundClip() != null && view.getBackgroundClip().isRunning()) {
                        view.getBackgroundClip().stop();
                        view.getBackgroundClip().close();
                    }
                    if (!JHeliFire.utility.SoundManager.isMuted()) {
                        view.setBackgroundClip(JHeliFire.utility.SoundManager.playLoop("/assets/sounds/background.wav"));
                    }
                    gameState = GAME_PLAY;
                }
                break;
        }
    }

    public void startGame() {
        model.init();
    }

    public void restartGame() {
        model.init();
    }

    public void returnToMenu() {
        model.updateHighScore();
        model.init();
    }

    // ======================================
    // Game State Management
    // ======================================
    public int getCurrentState() {
        return gameState;
    }

    public boolean shouldShowOptions() {
        return (gameState == GAME_PLAY || gameState == START_SCREEN || gameState == OPTIONS);
    }

    private void checkPlayerState() {
        if (!model.getPlayer().isAlive()) {
            handleGameOver();
        } else if (model.isBonusReady()) {
            model.setBonusReady(false);
            JHeliFire.utility.SoundManager.stopBackgroundLoop();
            if (!JHeliFire.utility.SoundManager.isMuted()) {
                bonusClip = JHeliFire.utility.SoundManager.playLoop("/assets/sounds/bonus.wav");
            }
            javax.swing.Timer bonusTimer = new javax.swing.Timer(1000, e -> {
                gameState = BONUS_CUTSCENE;
                ((javax.swing.Timer) e.getSource()).stop();
            });
            bonusTimer.setRepeats(false);
            bonusTimer.start();
        } else if (model.isInVictory()) {
            handleVictory();
        }
    }

    private void handleGameOver() {
        if (gameOverTimer == null) {
            JHeliFire.utility.SoundManager.playSound("/assets/sounds/gameover.wav");
            gameOverTimer = new javax.swing.Timer(1000, ae -> {
                int score = getScore();
                if (view.getScoreManager().isTop3(score)) {
                    pendingScore = score;
                    nameBuffer = "";
                    previousState = GAME_OVER;
                    gameState = ENTER_NAME_SCREEN;
                } else {
                    gameState = GAME_OVER;
                }
                gameOverTimer.stop();
                gameOverTimer = null;
                view.repaint();
            });
            gameOverTimer.setRepeats(false);
            gameOverTimer.start();
        }
    }

    private void handleVictory() {
        if (victoryTimer == null) {
            JHeliFire.utility.SoundManager.playSound("/assets/sounds/victory.wav");
            victoryTimer = new javax.swing.Timer(1000, e -> {
                int score = getScore();
                if (view.getScoreManager().isTop3(score)) {
                    pendingScore = score;
                    nameBuffer = "";
                    previousState = VICTORY;
                    gameState = ENTER_NAME_SCREEN;
                } else {
                    gameState = VICTORY;
                }
                victoryTimer.stop();
                victoryTimer = null;
                view.repaint();
            });
            victoryTimer.setRepeats(false);
            victoryTimer.start();
        }
    }

    // ======================================
    // Game Statistics & Info
    // ======================================
    public int getScore() {
        return model.getScore();
    }

    public int getLevel() {
        return model.getLevel();
    }

    public int getLives() {
        return model.getPlayer().getLives();
    }

    public String getNameBuffer() {
        return nameBuffer;
    }

    // ======================================
    // Input Handling
    // ======================================
    public void moveLeftPressed() {
        model.getPlayer().setLeftPressed(true);
    }

    public void moveLeftReleased() {
        model.getPlayer().setLeftPressed(false);
    }

    public void moveRightPressed() {
        model.getPlayer().setRightPressed(true);
    }

    public void moveRightReleased() {
        model.getPlayer().setRightPressed(false);
    }

    public void moveUpPressed() {
        model.getPlayer().setUpPressed(true);
    }

    public void moveUpReleased() {
        model.getPlayer().setUpPressed(false);
    }

    public void moveDownPressed() {
        model.getPlayer().setDownPressed(true);
    }

    public void moveDownReleased() {
        model.getPlayer().setDownPressed(false);
    }

    public void shootPressed() {
        if (model.getPlayer().isAlive()) {
            model.setShooting(true);
        }
    }

    public void shootReleased() {
        model.setShooting(false);
    }

    public void onMousePressed(java.awt.Point p) {
        boolean stateChanged = false;
        switch (gameState) {
            case START_SCREEN:
                if (view.getPlayButtonBounds().contains(p)) {
                    startGame();
                    gameState = GAME_PLAY;
                    stateChanged = true;
                }
                break;
            case OPTIONS:
                if (view.getMuteButtonArea().contains(p)) {
                    view.setMuted(!view.isMuted());
                    JHeliFire.utility.SoundManager.muted = view.isMuted();
                    if (view.isMuted()) {
                        JHeliFire.utility.SoundManager.stopBackgroundLoop();
                    } else {
                        view.setBackgroundClip(JHeliFire.utility.SoundManager.playLoop("/assets/sounds/background.wav"));
                    }
                    stateChanged = true;
                }
                break;
            case GAME_OVER:
                if (view.getRetryButtonBounds().contains(p)) {
                    restartGame();
                    gameState = GAME_PLAY;
                    stateChanged = true;
                } else if (view.getMenuButtonBounds().contains(p)) {
                    returnToMenu();
                    gameState = START_SCREEN;
                    stateChanged = true;
                }
                break;
            case VICTORY:
                if (view.getReplayVictoryBounds().contains(p)) {
                    restartGame();
                    gameState = GAME_PLAY;
                    stateChanged = true;
                } else if (view.getMenuVictoryBounds().contains(p)) {
                    returnToMenu();
                    gameState = START_SCREEN;
                    stateChanged = true;
                }
                break;
        }
        if (stateChanged) {
            view.requestFocusInWindow();
            view.repaint();
        }
    }

    public void onKeyEvent(java.awt.event.KeyEvent e) {
        if (gameState == ENTER_NAME_SCREEN && e.getID() == java.awt.event.KeyEvent.KEY_TYPED) {
            char c = e.getKeyChar();
            if ((Character.isLetterOrDigit(c) || c == ' ') && nameBuffer.length() < 12) {
                nameBuffer += c;
            } else if (c == '\b' && nameBuffer.length() > 0) {
                nameBuffer = nameBuffer.substring(0, nameBuffer.length() - 1);
            } else if (c == '\n') {
                String name = nameBuffer.trim().isEmpty() ? "Anonimo" : nameBuffer.trim();
                view.getScoreManager().addScore(name, pendingScore);
                nameBuffer = "";
                pendingScore = 0;
                gameState = previousState;
                view.repaint();
            }
        }
    }

    // ======================================
    // Event Callbacks
    // ======================================
    public void onBonusComplete() {
        model.addScore(500);
        model.startNextLevel();
        view.repaint();
    }

    public void onGameOver() {
        model.updateHighScore();
        model.init();
        view.repaint();
    }

    public void onOptionsButtonPressed() {
        if (gameState != OPTIONS) {
            previousState = gameState;
            gameState = OPTIONS;
            view.stopTimer();
            view.repaint();
        }
    }

    public void exitOptions() {
        if (gameState == OPTIONS) {
            gameState = previousState;
            view.startTimer();
            view.requestFocusInWindow();
            view.repaint();
        }
    }

    // GameSoundListener implementation
    @Override
    public void onPlayerHit() {
        JHeliFire.utility.SoundManager.playSound("/assets/sounds/lifeLost.wav");
    }

    @Override
    public void onEnemyDestroyed() {
        JHeliFire.utility.SoundManager.playExplosion();
    }

    @Override
    public void onPlayerShoot() {
        JHeliFire.utility.SoundManager.playShoot();
    }

    @Override
    public void onPlayerDeath() {
        JHeliFire.utility.SoundManager.playExplosion();
    }

    // ======================================
    // Private Utility Methods
    // ======================================
    private void updateBackground() {
        view.setBackgroundX(view.getBackgroundX() - view.getBackgroundSpeed());
        if (view.getBackgroundX() <= -view.getWidth()) {
            view.setBackgroundX(0);
        }
    }
}
