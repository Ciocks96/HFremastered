package JHeliFire.view;

import javax.swing.*;

import JHeliFire.controller.GameController;
import JHeliFire.model.BonusSceneManager;
import JHeliFire.model.Bullet;
import JHeliFire.model.Enemy;
import JHeliFire.model.EnemyBomb;
import JHeliFire.model.EnemyBullet;
import JHeliFire.model.Explosion;
import JHeliFire.model.GameModel;
import JHeliFire.model.GameModelListener;
import JHeliFire.model.Player;
import JHeliFire.utility.ScoreManager;
import JHeliFire.utility.SoundManager;

import javax.sound.sampled.Clip;

import static JHeliFire.model.GameState.*;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.List;

public class GamePanel extends JPanel implements ActionListener, MouseListener, GameModelListener {
    
public static final int WIDTH = 800;
public static final int HEIGHT = 600;
    
private Timer timer;
private GameModel model;
private Clip backgroundClip;
private Image muteIcon;
private Image unmuteIcon;
    
    // Campo per l'immagine di sfondo
    private Image backgroundImage;
    private int backgroundX = 0;
    private int backgroundSpeed = 1; // Puoi aumentare questo valore per uno scorrimento più veloce

    private BonusSceneManager bonusManager;

    /*  Gestione degli stati tramite variabili booleane
    private boolean inMenu = true;
    private boolean inGame = false;
    private boolean inGameOver = false;
    private boolean inOptions = false; */
   
    // --- 1. Stati del gioco via costanti intere ---
    // RIMOSSO: le costanti di stato duplicate (START_SCREEN, GAME_PLAY, ...)
    // public static final int START_SCREEN = 0;
    // public static final int GAME_PLAY    = 1;
    // public static final int OPTIONS      = 2;
    // public static final int GAME_OVER    = 3;
    // public static final int VICTORY      = 4;
    // public static final int ENTER_NAME_SCREEN = 5;
    // public static final int BONUS_CUTSCENE = 6;


    // Definizione dei "pulsanti" tramite rettangoli
    // Schermata Menu: pulsante PLAY
private Rectangle playButtonBounds = new Rectangle(350, 300, 100, 40);
    // Schermata Game Over: pulsante RETRY e MENU
private Rectangle retryButtonBounds = new Rectangle(300, 350, 100, 40);
private Rectangle menuButtonBounds = new Rectangle(420, 350, 100, 40);
    // Schermata Victory: pulsante REPLAY e MENU
private Rectangle replayVictoryBounds = new Rectangle(300, 350, 100, 40);
    private Rectangle menuVictoryBounds = new Rectangle(420, 350, 100, 40);
    
    // Campo per il font personalizzato
private Font arcadeFont;

    // Flag per il mute
private boolean isMuted = false;

     // Dichiarazione della variabile a livello di classe
    // Gestione del punteggio
private ScoreManager scoreManager = new ScoreManager(); 

    
    // Area per "Mute/Unmute" nell'overlay opzioni (coordinate fisse)
    private Rectangle muteButtonArea = new Rectangle();



    private GameController controller;

    // --- Pulsante options (ingranaggio) ---
    private JButton optionsButton;

    public GamePanel(GameModel model, GameController controller) {
        this.model = model;
        this.controller = controller;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setLayout(null);
        setDoubleBuffered(true);
        enableEvents(AWTEvent.KEY_EVENT_MASK);
        SoundManager.preloadSounds();
        backgroundClip = SoundManager.playLoop("/assets/sounds/background.wav");
        // Caricamento immagine di sfondo
        URL backgroundUrl = getClass().getResource("/assets/figure/background.png");
        if (backgroundUrl == null) {
            System.err.println("Immagine non trovata: /assets/figure/background.png");
        } else {
            backgroundImage = new ImageIcon(backgroundUrl).getImage();
        }

        // Caricamento immagine mute
        URL muteUrl = getClass().getResource("/assets/figure/mute.png");
        if (muteUrl == null) {
            System.err.println("Immagine non trovata: /assets/figure/mute.png");
        } else {
            muteIcon = new ImageIcon(muteUrl).getImage();
        }

        // Caricamento immagine unmute
        URL unmuteUrl = getClass().getResource("/assets/figure/unmute.png");
        if (unmuteUrl == null) {
            System.err.println("Immagine non trovata: /assets/figure/unmute.png");
        } else {
            unmuteIcon = new ImageIcon(unmuteUrl).getImage();
        }
        model.addListener(this);
        try {
            arcadeFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getClassLoader().getResourceAsStream("assets/font/PixelFont.ttf"))
                            .deriveFont(Font.PLAIN, 20);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(arcadeFont);
        } catch (Exception e) {
            System.out.println("Errore: Font non caricato, userò quello di default.");
            arcadeFont = new Font("Arial", Font.BOLD, 20);
        }
        bonusManager = new BonusSceneManager();
        setFocusable(true);
        addMouseListener(this);
        timer = new Timer(16, this);
        timer.start();
        // --- Pulsante options (ingranaggio) ---
        URL optionsUrl = getClass().getResource("/assets/figure/options.png");
        if (optionsUrl == null) {
            System.err.println("Immagine non trovata: /assets/figure/options.png");
        } else {
            optionsButton = new JButton(new ImageIcon(optionsUrl));
            optionsButton.setFocusPainted(false);
            optionsButton.setFocusable(false);
            int btnWidth = 32, btnHeight = 32, margin = 10;
            optionsButton.setBounds(WIDTH - btnWidth - margin, margin, btnWidth, btnHeight);
            add(optionsButton);
        }
        
    }
    
@Override
public void actionPerformed(ActionEvent e) {
    controller.onTick(); // Il controller gestisce update, transizioni e timer
    repaint();
}

@Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    optionsButton.setVisible(true);
    optionsButton.setEnabled(true);
    if (backgroundImage != null) {
        g.drawImage(backgroundImage, backgroundX, 0, getWidth(), getHeight(), this);
        g.drawImage(backgroundImage, backgroundX + getWidth(), 0, getWidth(), getHeight(), this);
    }
    switch (controller.getCurrentState()) {
        case START_SCREEN:
            drawMenu(g);
            break;
        case GAME_PLAY:
            drawEntities(g);
            drawHUD(g);
            break;
        case OPTIONS:
            drawOptions(g);
            break;
        case GAME_OVER:
            drawGameOver(g);
            break;
        case VICTORY:
            drawVictory(g);
            break;
        case ENTER_NAME_SCREEN:
            drawEnterNameScreen(g);
            break;
        case BONUS_CUTSCENE:
            
            g.drawImage(backgroundImage, backgroundX, 0, getWidth(), getHeight(), this);
            g.drawImage(backgroundImage, backgroundX + getWidth(), 0, getWidth(), getHeight(), this);
            drawHUD(g);
            drawBonusScene(g);
            break;
        default:
            System.out.println("Stato non gestito: " + controller.getCurrentState());
            break;
    }
}

private void drawEntities(Graphics g) {
    // --- Disegna player con blink se invulnerabile ---
    Player player = model.getPlayer();
    if (player.isAlive()) {
        if (player.isInvulnerable()) {
            int t = player.getInvulnerabilityTimer();
            if ((t / 10) % 2 == 0) {
                drawPlayer(g, player);
            }
        } else {
            drawPlayer(g, player);
        }
    }
    // --- Disegna tutte le entità ---
    for (Enemy enemy : model.getEnemies()) drawEnemy(g, enemy);
    for (Bullet b : model.getBullets()) drawBullet(g, b);
    for (EnemyBullet eb : model.getEnemyBullets()) drawEnemyBullet(g, eb);
    for (EnemyBomb bomb : model.getEnemyBombs()) drawEnemyBomb(g, bomb);
    for (Explosion ex : model.getExplosions()) drawExplosion(g, ex);
}

private void drawPlayer(Graphics g, Player player) {
    Image playerImg = new ImageIcon(getClass().getResource("/assets/figure/player.png")).getImage();
    g.drawImage(playerImg, player.getX(), player.getY(), player.getWidth(), player.getHeight(), this);
}

private void drawEnemy(Graphics g, Enemy enemy) {
    if (enemy instanceof JHeliFire.model.BlueHeli blue) {
        int idx = blue.getAnimIndex();
        Image img = new ImageIcon(getClass().getResource("/assets/figure/blueheli" + idx + ".png")).getImage();
        g.drawImage(img, blue.getX(), blue.getY(), blue.getWidth(), blue.getHeight(), this);
    } else if (enemy instanceof JHeliFire.model.GreenHeli green) {
        int idx = green.getAnimIndex();
        Image img = new ImageIcon(getClass().getResource("/assets/figure/greenheli" + idx + ".png")).getImage();
        g.drawImage(img, green.getX(), green.getY(), green.getWidth(), green.getHeight(), this);
    } else if (enemy instanceof JHeliFire.model.YellowHeli yellow) {
        int idx = yellow.getAnimIndex();
        boolean facingLeft = yellow.isFacingLeft();
        Image img = new ImageIcon(getClass().getResource("/assets/figure/yellowheli" + idx + ".png")).getImage();
        Graphics2D g2d = (Graphics2D) g.create();
        int x = yellow.getX(), y = yellow.getY(), w = yellow.getWidth(), h = yellow.getHeight();
        if (!facingLeft) {
            g2d.translate(x + w, y);
            g2d.scale(-1, 1);
            g2d.drawImage(img, 0, 0, w, h, this);
        } else {
            g2d.drawImage(img, x, y, w, h, this);
        }
        g2d.dispose();
    } else if (enemy instanceof JHeliFire.model.ShipDestroyer ship) {
        Image img = new ImageIcon(getClass().getResource("/assets/figure/shipdestroyer.png")).getImage();
        g.drawImage(img, ship.getX(), ship.getY(), ship.getWidth(), ship.getHeight(), this);
    }
}

private void drawBullet(Graphics g, Bullet b) {
    Image img = new ImageIcon(getClass().getResource("/assets/figure/bullet.png")).getImage();
    g.drawImage(img, b.getX(), b.getY(), b.getWidth(), b.getHeight(), this);
}

private void drawEnemyBullet(Graphics g, EnemyBullet eb) {
    Image img = new ImageIcon(getClass().getResource("/assets/figure/enemyBullet.png")).getImage();
    g.drawImage(img, eb.getX(), eb.getY(), eb.getWidth(), eb.getHeight(), this);
}

private void drawEnemyBomb(Graphics g, EnemyBomb bomb) {
    Image img = new ImageIcon(getClass().getResource("/assets/figure/enemyBomb.png")).getImage();
    g.drawImage(img, bomb.getX(), bomb.getY(), bomb.getWidth(), bomb.getHeight(), this);
}

private void drawExplosion(Graphics g, Explosion ex) {
    int idx = ex.getAnimIndex();
    Image img = new ImageIcon(getClass().getResource("/assets/figure/explosion" + idx + ".png")).getImage();
    g.drawImage(img, ex.getX(), ex.getY(), ex.getWidth(), ex.getHeight(), this);
}

private void drawBonusScene(Graphics g) {
    BonusSceneManager bonus = bonusManager;
    int frame = (bonus.getFrameCounter() / 20) % 3;
    Image islandImg;
    if (!bonus.isWomanVisible()) {
        islandImg = new ImageIcon(getClass().getResource("/assets/figure/island3.png")).getImage();
    } else {
        islandImg = new ImageIcon(getClass().getResource("/assets/figure/island" + frame + ".png")).getImage();
    }
    g.drawImage(islandImg, bonus.getIslandX(), bonus.getIslandY(), 64, 44, this);
    Image subImg = new ImageIcon(getClass().getResource("/assets/figure/player.png")).getImage();
    g.drawImage(subImg, bonus.getSubX(), bonus.getSubY(), 64, 37, this);
    if (bonus.getPhase() == BonusSceneManager.Phase.SHOW_POINTS) {
        Font fontToUse = arcadeFont != null ? arcadeFont.deriveFont(Font.BOLD, 40) : new Font("Arial", Font.BOLD, 40);
        g.setFont(fontToUse);
        
        String text;
        if (bonus.isPointBonus()) {
            g.setColor(Color.YELLOW);
            text = "BONUS 500";
        } else {
            g.setColor(Color.GREEN);
            text = "MORE LIFE";
        }
        
        FontMetrics fm = g.getFontMetrics();
        int x = (800 - fm.stringWidth(text)) / 2;
        int y = 150;
        g.drawString(text, x, y);
    }
}

    // Estrai il disegno dell’HUD in un metodo a parte
private void drawHUD(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    // Disegna il punteggio e il livello in alto a sinistra
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    Font infoFont = arcadeFont.deriveFont(Font.BOLD, 24);
    g2.setFont(infoFont);
    g2.setColor(Color.WHITE);
    String info = "Score: " + controller.getScore() + "   Level: " + controller.getLevel();
    g2.drawString(info, 20, 30);

    // Vite al centro
    int iconW = 30, iconH = 30;
    Image lifeIcon = new ImageIcon(getClass().getResource("/assets/figure/player.png")).getImage();
    int lives = controller.getLives();
    int totalW = lives * iconW;
    int startX = (getWidth() - totalW) / 2;
    for (int i = 0; i < lives; i++) {
        g2.drawImage(lifeIcon, startX + i * iconW, 10, iconW, iconH, this);
    }
}


    
    private void drawMenu(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        // Attiva l'antialiasing per un testo più levigato
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // 1. Disegna il titolo "SPACE ALIEN" e "INVADERS" con arcadeFont in grassetto, 60pt
        Font titleFont = arcadeFont.deriveFont(Font.BOLD, 60);
        g2.setFont(titleFont);
        FontMetrics fm = g2.getFontMetrics();
        String titleLine1 = "HELIFIRE";
        String titleLine2 = "REMASTERED";
        int titleX1 = (getWidth() - fm.stringWidth(titleLine1)) / 2;
        int titleY1 = 250;
        int titleX2 = (getWidth() - fm.stringWidth(titleLine2)) / 2;
        int titleY2 = 320;
        
        g2.setColor(Color.GREEN);
        g2.drawString(titleLine1, titleX1, titleY1);
        g2.setColor(Color.RED);
        g2.drawString(titleLine2, titleX2, titleY2);
        
        // 2. Disegna il testo "High Score: ..." con arcadeFont in stile Plain, 30pt
        Font scoreFont = arcadeFont.deriveFont(Font.PLAIN, 30);
        g2.setFont(scoreFont);
        fm = g2.getFontMetrics();
        ScoreManager.ScoreEntry top = scoreManager.getHighScore();
        String scoreText = "High Score: " + top.name + " - " + top.score;
        int scoreX = (getWidth() - fm.stringWidth(scoreText)) / 2;
        int scoreY = 400;
        g2.setColor(Color.WHITE);
        g2.drawString(scoreText, scoreX, scoreY);
        
        // 3. Disegna il pulsante "PLAY" con un rettangolo e il testo centrato al suo interno
        int buttonWidth = 300;
        int buttonHeight = 80;
        int buttonX = (getWidth() - buttonWidth) / 2;
        int buttonY = 500;
        g2.setColor(Color.GREEN);
        g2.drawRect(buttonX, buttonY, buttonWidth, buttonHeight);
        
        // Aggiorna i bounds del pulsante se necessario
        playButtonBounds.setBounds(buttonX, buttonY, buttonWidth, buttonHeight);
        
        // Disegna il testo "PLAY" con arcadeFont Bold, 40pt, centrato nel rettangolo
        Font playFont = arcadeFont.deriveFont(Font.BOLD, 40);
        g2.setFont(playFont);
        fm = g2.getFontMetrics();
        String playText = "PLAY";
        int playX = buttonX + (buttonWidth - fm.stringWidth(playText)) / 2;
        int playY = buttonY + ((buttonHeight - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(playText, playX, playY);

    }
    
    
    
    private void drawGameOver(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        // Attiva l'antialiasing per il testo
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Crea un overlay semitrasparente per oscurare lo sfondo
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, WIDTH, HEIGHT);
        
        // Disegna il testo "GAME OVER" centrato in alto
        Font gameOverFont = arcadeFont.deriveFont(Font.BOLD, 72);
        g2.setFont(gameOverFont);
        g2.setColor(Color.RED);
        String gameOverText = "GAME OVER";
        FontMetrics fmGameOver = g2.getFontMetrics();
        int gameOverX = (WIDTH - fmGameOver.stringWidth(gameOverText)) / 2;
        int gameOverY = 200;
        g2.drawString(gameOverText, gameOverX, gameOverY);
        
        // Disegna lo score finale con un font più grande
        Font scoreFont = arcadeFont.deriveFont(Font.PLAIN, 36);
        g2.setFont(scoreFont);
        g2.setColor(Color.WHITE);
        String scoreText = "Score: " + controller.getScore();
        FontMetrics fmScore = g2.getFontMetrics();
        int scoreX = (WIDTH - fmScore.stringWidth(scoreText)) / 2;
        int scoreY = gameOverY + 60;
        g2.drawString(scoreText, scoreX, scoreY);
        
        // Configura i pulsanti "Retry" e "Menu"
        int buttonWidth = 200;
        int buttonHeight = 60;
        int spacing = 40; // spazio tra i due pulsanti
        int totalButtonsWidth = 2 * buttonWidth + spacing;
        int startX = (WIDTH - totalButtonsWidth) / 2;
        int buttonsY = scoreY + 80;
        
        // Pulsante RETRY
        retryButtonBounds.setBounds(startX, buttonsY, buttonWidth, buttonHeight);
        g2.setColor(Color.GREEN);
        g2.drawRect(startX, buttonsY, buttonWidth, buttonHeight);
        
        Font buttonFont = arcadeFont.deriveFont(Font.BOLD, 32);
        g2.setFont(buttonFont);
        FontMetrics fmButton = g2.getFontMetrics();
        String retryText = "Retry";
        int retryX = startX + (buttonWidth - fmButton.stringWidth(retryText)) / 2;
        int retryY = buttonsY + ((buttonHeight - fmButton.getHeight()) / 2) + fmButton.getAscent();
        g2.drawString(retryText, retryX, retryY);
        
        // Pulsante MENU
        int menuX = startX + buttonWidth + spacing;
        menuButtonBounds.setBounds(menuX, buttonsY, buttonWidth, buttonHeight);
        g2.setColor(Color.GREEN);
        g2.drawRect(menuX, buttonsY, buttonWidth, buttonHeight);
        
        String menuText = "Menu";
        int menuTextX = menuX + (buttonWidth - fmButton.stringWidth(menuText)) / 2;
        int menuTextY = buttonsY + ((buttonHeight - fmButton.getHeight()) / 2) + fmButton.getAscent();
        g2.drawString(menuText, menuTextX, menuTextY);
    }

private void drawOptions(Graphics g) {
    // Sfondo semitrasparente
    g.setColor(new Color(0, 0, 0, 150));
    g.fillRect(0, 0, getWidth(), getHeight());

    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    // === TITOLO CENTRATO ===
    g2.setFont(arcadeFont.deriveFont(Font.BOLD, 32));
    g2.setColor(Color.GREEN);
    String title = "Opzioni";
    int titleX = (getWidth() - g2.getFontMetrics().stringWidth(title)) / 2;
    g2.drawString(title, titleX, 50);

    // === ISTRUZIONI A SINISTRA ===
    g2.setFont(arcadeFont.deriveFont(Font.BOLD, 18));
    g2.setColor(Color.RED);
    g2.drawString("Istruzioni di Gioco:", 60, 100);

    g2.setFont(arcadeFont.deriveFont(Font.PLAIN, 16));
    g2.setColor(Color.WHITE);

    String[] instructions = {
        "Controlli:",
        "- Usa le frecce per muovere il sottomarino.",
        "- Usa la barra spaziatrice per sparare.",
        "",
        "Nemici:",
        "- Evita gli spari e le bombe degli elicotteri in aria.",
        "- Schiva le navi che provengono dal mare.",
        "",
        "Buon divertimento!"
    };

    int instrY = 130;
    for (String line : instructions) {
        g2.drawString(line, 60, instrY);
        instrY += g2.getFontMetrics().getHeight();
    }

    // === CLASSIFICA A DESTRA ===
    g2.setFont(arcadeFont.deriveFont(Font.PLAIN, 22));
    g2.setColor(Color.RED);
    g2.drawString("Classifica Top 3:", 480, 100);

List<ScoreManager.ScoreEntry> topScores = scoreManager.getTopScores();
int y = 140;
int position = 1;

for (ScoreManager.ScoreEntry entry : topScores) {
    String line = position + ". " + entry.name + " - " + entry.score;

    // Cambia colore in base alla posizione
    switch (position) {
        case 1:
            g2.setColor(new Color(255, 215, 0)); // Oro
            break;
        case 2:
            g2.setColor(new Color(192, 192, 192)); // Argento
            break;
        case 3:
            g2.setColor(new Color(205, 127, 50)); // Bronzo
            break;
        default:
            g2.setColor(Color.WHITE); // fallback
            break;
    }

    g2.drawString(line, 480, y);
    y += g2.getFontMetrics().getHeight();
    position++;
}

    // === MUTE/UNMUTE ===
Image muteToDraw = SoundManager.isMuted() ? unmuteIcon : muteIcon;

int iconWidth = 32;
int iconHeight = 32;
int iconX = (getWidth() - iconWidth) / 2;
int iconY = getHeight() - 100;

int clickableAreaSize = 25;
int clickableX = iconX + (iconWidth - clickableAreaSize) / 2;
int clickableY = iconY + (iconHeight - clickableAreaSize) / 2;

// Disegna l'icona
g.drawImage(muteToDraw, iconX, iconY, iconWidth, iconHeight, this);

// Centra l'area cliccabile sulla parte interna dell’icona
muteButtonArea.setBounds(clickableX, clickableY, clickableAreaSize, clickableAreaSize);
}
    
    private void drawVictory(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Disegna un overlay semi-trasparente simile a quello di game over
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, WIDTH, HEIGHT);
        
        // Testo "YOU WIN!"
        Font victoryFont = arcadeFont.deriveFont(Font.BOLD, 72);
        g2.setFont(victoryFont);
        g2.setColor(Color.YELLOW);
        String victoryText = "YOU WIN!";
        FontMetrics fm = g2.getFontMetrics();
        int x = (WIDTH - fm.stringWidth(victoryText)) / 2;
        int y = HEIGHT / 2 - 50;
        g2.drawString(victoryText, x, y);
        
        // Score finale
        Font scoreFont = arcadeFont.deriveFont(Font.PLAIN, 36);
        g2.setFont(scoreFont);
        g2.setColor(Color.WHITE);
        String scoreText = "Score: " + controller.getScore();
        int scoreX = (WIDTH - g2.getFontMetrics().stringWidth(scoreText)) / 2;
        int scoreY = y + 60;
        g2.drawString(scoreText, scoreX, scoreY);
        
        // Configura i pulsanti "Replay" e "Menu"
        int buttonWidth = 200;
        int buttonHeight = 60;
        int spacing = 40;
        int totalButtonsWidth = 2 * buttonWidth + spacing;
        int startX = (WIDTH - totalButtonsWidth) / 2;
        int buttonsY = scoreY + 80;
        
        // Pulsante "Replay"
        replayVictoryBounds.setBounds(startX, buttonsY, buttonWidth, buttonHeight);
        g2.setColor(Color.GREEN);
        g2.drawRect(replayVictoryBounds.x, replayVictoryBounds.y, replayVictoryBounds.width, replayVictoryBounds.height);
        
        Font buttonFont = arcadeFont.deriveFont(Font.BOLD, 32);
        g2.setFont(buttonFont);
        FontMetrics fmButton = g2.getFontMetrics();
        String replayText = "Replay";
        int replayTextX = startX + (buttonWidth - fmButton.stringWidth(replayText)) / 2;
        int replayTextY = buttonsY + ((buttonHeight - fmButton.getHeight()) / 2) + fmButton.getAscent();
        g2.drawString(replayText, replayTextX, replayTextY);
        
        // Pulsante "Menu"
        int menuX = startX + buttonWidth + spacing;
        menuVictoryBounds.setBounds(menuX, buttonsY, buttonWidth, buttonHeight);
        g2.setColor(Color.GREEN);
        g2.drawRect(menuVictoryBounds.x, menuVictoryBounds.y, menuVictoryBounds.width, menuVictoryBounds.height);
        
        String menuText = "Menu";
        int menuTextX = menuX + (buttonWidth - fmButton.stringWidth(menuText)) / 2;
        int menuTextY = buttonsY + ((buttonHeight - fmButton.getHeight()) / 2) + fmButton.getAscent();
        g2.drawString(menuText, menuTextX, menuTextY);
    }

    private void drawEnterNameScreen(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    g2.setFont(arcadeFont.deriveFont(Font.BOLD, 36));
    g2.setColor(Color.WHITE);

    String title = "CLASSIFICA TOP 3!";
    int titleX = (WIDTH - g2.getFontMetrics().stringWidth(title)) / 2;
    g2.drawString(title, titleX, 200);

    g2.setFont(arcadeFont.deriveFont(Font.PLAIN, 28));
    String input = "Inserisci il tuo nome: " + controller.getNameBuffer() + "_";
    int inputX = (WIDTH - g2.getFontMetrics().stringWidth(input)) / 2;
    g2.drawString(input, inputX, 300);

    g2.setFont(arcadeFont.deriveFont(Font.PLAIN, 20));
    String hint = "(Premi INVIO per confermare)";
    int hintX = (WIDTH - g2.getFontMetrics().stringWidth(hint)) / 2;
    g2.drawString(hint, hintX, 360);
}
    
    @Override
public void mousePressed(MouseEvent e) {
    controller.onMousePressed(e.getPoint());
}

    @Override
    protected void processKeyEvent(KeyEvent e) {
        controller.onKeyEvent(e);
        super.processKeyEvent(e);
    }
   
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    public GameModel getModel() {
        return model;
    }
    public void setController(GameController controller) {
        this.controller = controller;
        // Rimuovi tutti i KeyListener precedenti
        for (KeyListener kl : getKeyListeners()) {
            removeKeyListener(kl);
        }
        // Aggiungi ora l'InputHandler corretto
        addKeyListener(new JHeliFire.controller.InputHandler(controller));
        // Aggiungi ora l'ActionListener al pulsante options
        if (optionsButton != null) {
            for (ActionListener al : optionsButton.getActionListeners()) {
                optionsButton.removeActionListener(al);
            }
            optionsButton.addActionListener(e -> {
                if (this.controller != null) {
                    if (controller.getCurrentState() == OPTIONS) {
                        controller.exitOptions();
                    } else {
                        controller.onOptionsButtonPressed();
                    }
                }
            });
        }
    }

    @Override
    public void onModelChanged() {
        repaint();
    }

    public BonusSceneManager getBonusManager() { return bonusManager; }
    public javax.sound.sampled.Clip getBackgroundClip() { return backgroundClip; }
    public void setBackgroundClip(javax.sound.sampled.Clip clip) { this.backgroundClip = clip; }
    public int getBackgroundX() { return backgroundX; }
    public void setBackgroundX(int x) { this.backgroundX = x; }
    public int getBackgroundSpeed() { return backgroundSpeed; }
    public ScoreManager getScoreManager() { return scoreManager; }
    public Rectangle getPlayButtonBounds() { return playButtonBounds; }
    public Rectangle getMuteButtonArea() { return muteButtonArea; }
    public boolean isMuted() { return isMuted; }
    public void setMuted(boolean muted) { this.isMuted = muted; }
    public Rectangle getRetryButtonBounds() { return retryButtonBounds; }
    public Rectangle getMenuButtonBounds() { return menuButtonBounds; }
    public Rectangle getReplayVictoryBounds() { return replayVictoryBounds; }
    public Rectangle getMenuVictoryBounds() { return menuVictoryBounds; }

    /**
     * Ferma il timer del gioco (usato per mettere in pausa quando si apre il menu opzioni)
     */
    public void stopTimer() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
    }
    /**
     * Avvia o riavvia il timer del gioco (usato quando si esce dal menu opzioni)
     */
    public void startTimer() {
        if (timer != null && !timer.isRunning()) {
            timer.start();
        }
    }
}
