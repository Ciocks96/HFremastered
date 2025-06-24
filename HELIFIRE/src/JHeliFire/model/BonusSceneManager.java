package JHeliFire.model;

public class BonusSceneManager {
    public enum Phase {
        ISLAND_ENTRANCE,
        SUB_UP,
        SUB_TO_ISLAND,
        PICKUP,
        RETURN,
        SHOW_POINTS,
        COMPLETE
    }
    private Phase phase = Phase.ISLAND_ENTRANCE;
    private int islandX = -100;
    private int islandY = 300;
    private int islandSpeed = 2;
    private int subX = 400;
    private int subY = 700;
    private int subSpeed = 2;
    private boolean womanVisible = true;
    private int timer = 0;
    private int frameCounter = 0;
    private boolean isPointBonus = false; // true se il bonus darà punti, false se darà una vita

    public BonusSceneManager() {
        // Costruttore senza parametri
    }

    public void update() {
        frameCounter++;

        switch (phase) {
            case ISLAND_ENTRANCE:
                islandX += islandSpeed;
                if (islandX >= 700) phase = Phase.SUB_UP;
                break;

            case SUB_UP:
                subY -= subSpeed;
                if (subY <= islandY + 10) phase = Phase.SUB_TO_ISLAND;
                break;

            case SUB_TO_ISLAND:
                subX += subSpeed;
                if (subX >= islandX - 60) {
                    phase = Phase.PICKUP;
                    timer = 0;
                }
                break;

            case PICKUP:
                timer++;
                if (timer == 30) {
                    womanVisible = false;
                    phase = Phase.RETURN;
                }
                break;

            case RETURN:
                if (subX > 400) subX -= subSpeed;
                else if (subY < 700) subY += subSpeed;
                else {
                    phase = Phase.SHOW_POINTS;
                    timer = 0;
                }
                break;

            case SHOW_POINTS:
                timer++;
                if (timer == 100) phase = Phase.COMPLETE;
                break;

            default:
                break;
        }
    }

    // Getter per stato logico
    public int getIslandX() { return islandX; }
    public int getIslandY() { return islandY; }
    public int getSubX() { return subX; }
    public int getSubY() { return subY; }
    public boolean isWomanVisible() { return womanVisible; }
    public int getFrameCounter() { return frameCounter; }
    public Phase getPhase() { return phase; }
    public boolean isComplete() { return phase == Phase.COMPLETE; }
    public boolean isPointBonus() {
        return isPointBonus;
    }

    public void setPointBonus(boolean isPointBonus) {
        this.isPointBonus = isPointBonus;
    }

    public void reset() {
        phase = Phase.ISLAND_ENTRANCE;
        islandX = -100;
        subX = 400;
        subY = 700;
        womanVisible = true;
        timer = 0;
        frameCounter = 0;
    }
}
