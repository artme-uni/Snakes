package ru.nsu.g.akononov.snakesGame.view;

import ru.nsu.g.akononov.snakesGame.presenter.Controller;
import ru.nsu.g.akononov.snakesGame.view.game.GamePanel;
import ru.nsu.g.akononov.snakesGame.view.menu.GameCreationPanel;
import ru.nsu.g.akononov.snakesGame.view.menu.MenuPanel;
import ru.nsu.g.akononov.utils.swing.MainFrame;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GUI {
    private Controller controller;

    private MainFrame frame;
    private final GamePanel gamePanel = new GamePanel();
    private final MenuPanel menuPanel = new MenuPanel();

    public GUI() {
        java.awt.EventQueue.invokeLater(() -> {
            frame = new MainFrame("Snake", 3, 2, 3.0/4);

            ColorTheme coloration = ColorTheme.getInstance();
            if(coloration.background != Color.BLACK){
                frame.setBackgroundColoration(coloration.background);
            }

            frame.addPanel(gamePanel, "game");
            frame.addPanel(menuPanel, "menu");

            frame.switchPanel();

            frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent event) {
                    controller.shutdown();
                }
            });
        });
    }

    public GamePanel getGamePanel() {
        return gamePanel;
    }

    public MenuPanel getMenuPanel() {
        return menuPanel;
    }

    public void addButtonListeners(){
        gamePanel.getExitButton().addActionListener(e -> {
            controller.exitGame();
            frame.switchPanel();
        });

        menuPanel.getCreatingButton().addActionListener(e -> {
            GameCreationPanel panel = menuPanel.getCreationPanel();
            controller.createNewGame(
                    menuPanel.getJoiningPanel().getUsername(),
                    (int) panel.getPlaygroundHeight(),
                    (int) panel.getPlaygroundWidth(),
                    (float) panel.getFoodPerPlayer(),
                    (int) panel.getFoodStatic(),
                    (float) panel.getDeadFood(),
                    (int) panel.getStateDelay(),
                    (int) panel.getPingDelay(),
                    (int) panel.getNodeTimeout());
            frame.switchPanel();
        });

        menuPanel.getJoiningButton().addActionListener(e -> {
            try {
                controller.tryToJoin(menuPanel.getSelectedHost(), menuPanel.getJoiningPanel().getUsername());
            }catch (RuntimeException ignored){ return;}

            frame.switchPanel();
        });
    }

    public void setController(Controller controller){
        java.awt.EventQueue.invokeLater(() -> {
            this.controller = controller;
            addButtonListeners();
            addKeyListeners();
        });
    }

    public void addKeyListeners(){
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
                    controller.moveSnakeLeft();
                }

                if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
                    controller.moveSnakeRight();
                }

                if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
                    controller.moveSnakeUp();
                }

                if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) {
                    controller.moveSnakeDown();
                }

            }
        });
    }
}