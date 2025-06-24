package JHeliFire.controller;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class InputHandler extends KeyAdapter {
    private final GameController controller;

    public InputHandler(GameController controller) {
        this.controller = controller;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        switch (key) {
            case KeyEvent.VK_LEFT:
                controller.moveLeftPressed();
                break;
            case KeyEvent.VK_RIGHT:
                controller.moveRightPressed();
                break;
            case KeyEvent.VK_UP:
                controller.moveUpPressed();
                break;
            case KeyEvent.VK_DOWN:
                controller.moveDownPressed();
                break;
            case KeyEvent.VK_SPACE:
                controller.shootPressed();
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        switch (key) {
            case KeyEvent.VK_LEFT:
                controller.moveLeftReleased();
                break;
            case KeyEvent.VK_RIGHT:
                controller.moveRightReleased();
                break;
            case KeyEvent.VK_UP:
                controller.moveUpReleased();
                break;
            case KeyEvent.VK_DOWN:
                controller.moveDownReleased();
                break;
            case KeyEvent.VK_SPACE:
                controller.shootReleased();
                break;
        }
    }
}