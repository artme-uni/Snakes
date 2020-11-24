package ru.nsu.g.akononov.snakes.view;

import ru.nsu.g.akononov.snakes.view.game.GamePanel;
import ru.nsu.g.akononov.snakes.view.menu.MenuPanel;
import ru.nsu.g.akononov.utils.swing.MainFrame;

import java.awt.*;

public class GUI {
    private final MainFrame frame;
    private final GamePanel gamePanel = new GamePanel();
    private final MenuPanel menuPanel = new MenuPanel();

    public GUI() {
        frame = new MainFrame("Snake", 3, 2, 3.0/4);

        ColorTheme coloration = ColorTheme.getInstance();
        if(coloration.background != Color.BLACK){
            frame.setBackgroundColoration(coloration.background);
        }

        frame.addPanel(gamePanel, "game");
        frame.addPanel(menuPanel, "menu");

        addButtonListeners();
    }

    public GamePanel getGamePanel() {
        return gamePanel;
    }

    public void addButtonListeners(){
        gamePanel.getExitButton().addActionListener(e -> {
            frame.switchPanel();
        });

        menuPanel.getCreatingButton().addActionListener(e -> {
            frame.switchPanel();
        });

        menuPanel.getJoiningButton().addActionListener(e -> {
            frame.switchPanel();
        });
    }
}
