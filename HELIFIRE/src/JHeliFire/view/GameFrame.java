package JHeliFire.view;

import javax.swing.JFrame;

public class GameFrame extends JFrame {
    public GameFrame(GamePanel panel) {
        setTitle("HeliFire");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        add(panel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
