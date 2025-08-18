package JHeliFire.utility;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class SoundManager {

    public static boolean muted = false;
    // Riferimento al clip in loop di background (statico per gestirlo globalmente)
    private static Clip backgroundClip = null;

     // === NUOVI clip precaricati ===
    private static Clip shootClip;
    private static final int MAX_SIMULTANEOUS_EXPLOSIONS = 4;
    private static Clip[] explosionClips = new Clip[MAX_SIMULTANEOUS_EXPLOSIONS];
    private static int currentExplosionIndex = 0;

    // === PRELOAD suoni da usare spesso ===
    public static void preloadSounds() {
        shootClip = loadClip("/assets/sounds/playershoot.wav");
        // Preallochiamo più clip per le esplosioni
        for (int i = 0; i < MAX_SIMULTANEOUS_EXPLOSIONS; i++) {
            explosionClips[i] = loadClip("/assets/sounds/explosion.wav");
        }
    }
    private static Clip loadClip(String soundPath) {
        try {
            URL url = SoundManager.class.getResource(soundPath);
            if (url == null) {
                System.err.println("File audio non trovato: " + soundPath + ". Verifica il percorso e la presenza del file.");
                return null;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            return clip;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void playShoot() {
        if (!muted && shootClip != null) {
            shootClip.stop();
            shootClip.setFramePosition(0);
            shootClip.start();
        }
    }

    public static void playExplosion() {
        if (!muted) {
            // Usa il prossimo clip disponibile in modo circolare
            Clip currentClip = explosionClips[currentExplosionIndex];
            if (currentClip != null && !currentClip.isRunning()) {
                currentClip.setFramePosition(0);
                currentClip.start();
            }
            // Passa al prossimo clip per la prossima esplosione
            currentExplosionIndex = (currentExplosionIndex + 1) % MAX_SIMULTANEOUS_EXPLOSIONS;
        }
    }

    // Riproduce un effetto sonoro una volta
    public static void playSound(String soundPath) {
        if (muted) {
            return;
        }
        try {
            URL url = SoundManager.class.getResource(soundPath);
            if (url == null) {
                System.err.println("Sound file not found: " + soundPath);
                return;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            // Aggiunge un listener per chiudere il clip alla fine
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    event.getLine().close();
                }
            });
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Errore durante la riproduzione del suono: " + soundPath + ". Dettagli: " + e.getMessage());
        }
    }

    // Gestisce il playLoop della musica di sottofondo
    public static Clip playLoop(String soundPath) {
        if (muted) {
            return null;
        }
        // Se esiste già un backgroundClip, fermalo e chiudilo prima di crearne uno nuovo
        if (backgroundClip != null && backgroundClip.isRunning()) {
            backgroundClip.stop();
            backgroundClip.close();
            backgroundClip = null;
        }
        try {
            URL url = SoundManager.class.getResource(soundPath);
            if (url == null) {
                System.err.println("Sound file not found: " + soundPath);
                return null;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundClip = clip; // Salva il riferimento
            return clip;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Errore durante la riproduzione del suono: " + soundPath + ". Dettagli: " + e.getMessage());
        }
        return null;
    }

    public static void stopBackgroundLoop() {
        if (backgroundClip != null && backgroundClip.isRunning()) {
            backgroundClip.stop();
            backgroundClip.close();
            backgroundClip = null;
        }
    }

    public static boolean isMuted() {
        return muted;
    }

    public static void setMuted(boolean m) {
        muted = m;
        if (muted) {
            stopBackgroundLoop();
        }
    }
}