package JHeliFire;

import JHeliFire.controller.GameController;
import JHeliFire.model.GameModel;
import JHeliFire.view.GameFrame;
import JHeliFire.view.GamePanel;

public class Main {
    public static void main(String[] args) {
        GameModel model = new GameModel(GamePanel.WIDTH, GamePanel.HEIGHT);
        GamePanel panel = new GamePanel(model, null); // Passa il model, controller sarà impostato dopo
        GameController controller = new GameController(model, panel);
        panel.setController(controller);
        panel.setEnabled(true);
        panel.setFocusable(true);
        new GameFrame(panel);
        panel.requestFocusInWindow();
    }
}

