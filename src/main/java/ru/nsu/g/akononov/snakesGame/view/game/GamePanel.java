package ru.nsu.g.akononov.snakesGame.view.game;

import ru.nsu.g.akononov.snakesGame.presenter.GameInfoView;
import ru.nsu.g.akononov.snakesGame.presenter.PlaygroundView;
import ru.nsu.g.akononov.snakesGame.view.ColorTheme;
import ru.nsu.g.akononov.snakesGame.view.game.gameInfo.GameInfoPanel;
import ru.nsu.g.akononov.snakesGame.view.game.playground.PlaygroundPanel;
import ru.nsu.g.akononov.utils.swing.StripLayoutPanel;

import javax.swing.*;

public class GamePanel extends StripLayoutPanel implements PlaygroundView, GameInfoView {
    private final static int defaultGridWidth = 40;
    private final static int defaultGridHeight = 30;

    GameInfoPanel gameInfoPanel = new GameInfoPanel();
    PlaygroundPanel playgroundPanel = new PlaygroundPanel(defaultGridHeight, defaultGridWidth);

    public GamePanel() {
        super(false);

        ColorTheme coloration = ColorTheme.getInstance();
        if (coloration.background != null) {
            setBackground(coloration.background);
        }
        addPanel(4, playgroundPanel);
        addPanel(1, gameInfoPanel);
    }

    public JButton getExitButton() {
        return gameInfoPanel.getExitButton();
    }

    @Override
    public void setGameConfigData(String[][] configs) {
        SwingUtilities.invokeLater(() -> {
            gameInfoPanel.addGameConfig(configs);
        });
    }

    @Override
    public void setScoreboardData(String[][] configs) {
        SwingUtilities.invokeLater(() -> {
            for (String[] config : configs) {
                gameInfoPanel.addResult(config[0], Integer.parseInt(config[1]));
            }
            gameInfoPanel.refreshScoreboard();
        });
    }

    @Override
    public void addSnake(boolean isOwn , boolean isZombie, int[][] coords) {
        SwingUtilities.invokeLater(() -> {
            playgroundPanel.setVisible(false);
            playgroundPanel.addSnake(isOwn, isZombie, coords);
            playgroundPanel.setVisible(true);
        });
    }

    @Override
    public void setFood(int x, int y) {
        SwingUtilities.invokeLater(() -> {
            playgroundPanel.setVisible(false);
            playgroundPanel.setFood(x, y);
            playgroundPanel.setVisible(true);
        });
    }

    @Override
    public void setPlaygroundSize(int width, int height) {
        SwingUtilities.invokeLater(() -> {
            playgroundPanel.setGridSize(height, width);
        });
    }

    @Override
    public void clearPlayground() {
        SwingUtilities.invokeLater(() -> {
            playgroundPanel.clear();
        });
    }
}
