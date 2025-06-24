package JHeliFire.utility;

import java.io.*;
import java.util.*;

public class ScoreManager {

    private static final String FILE_PATH = "highscores.txt";
    private List<ScoreEntry> topScores = new ArrayList<>();

    public ScoreManager() {
        loadScores();
    }

    public static class ScoreEntry {
        public String name;
        public int score;

        public ScoreEntry(String name, int score) {
            this.name = name;
            this.score = score;
        }

        @Override
        public String toString() {
            return name + "," + score;
        }
    }

    public void loadScores() {
        topScores.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    topScores.add(new ScoreEntry(parts[0], Integer.parseInt(parts[1])));
                }
            }
            topScores.sort((a, b) -> b.score - a.score);
            while (topScores.size() > 3) topScores.remove(3);
        } catch (IOException e) {
            // File non esiste ancora, lo creeremo al primo salvataggio
        }
    }

    public void saveScores() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (ScoreEntry entry : topScores) {
                writer.write(entry.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isTop3(int score) {
        if (topScores.size() < 3) return true;
        return score > topScores.get(topScores.size() - 1).score;
    }

    public void addScore(String name, int score) {
        topScores.add(new ScoreEntry(name, score));
        topScores.sort((a, b) -> b.score - a.score);
        while (topScores.size() > 3) topScores.remove(3);
        saveScores();
    }

    public List<ScoreEntry> getTopScores() {
        return new ArrayList<>(topScores);
    }

    public ScoreEntry getHighScore() {
        return topScores.isEmpty() ? new ScoreEntry("---", 0) : topScores.get(0);
    }
}