package JHeliFire.model;


// Stati del gioco centralizzati
public final class GameState {
    public static final int START_SCREEN = 0;
    public static final int GAME_PLAY    = 1;
    public static final int OPTIONS      = 2;
    public static final int GAME_OVER    = 3;
    public static final int VICTORY      = 4;
    public static final int ENTER_NAME_SCREEN = 5;
    public static final int BONUS_CUTSCENE = 6;

    private GameState() {}
}
