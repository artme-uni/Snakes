package ru.nsu.g.akononov.snakes.view.game;

import ru.nsu.g.akononov.snakes.presenter.GameInfoView;
import ru.nsu.g.akononov.snakes.presenter.PlaygroundView;
import ru.nsu.g.akononov.snakes.view.ColorTheme;
import ru.nsu.g.akononov.snakes.view.game.gameInfo.GameInfoPanel;
import ru.nsu.g.akononov.snakes.view.game.playground.PlaygroundPanel;
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

    public JButton getExitButton(){
        return gameInfoPanel.getExitButton();
    }

    @Override
    public void setGameConfigData(String[][] configs) {
        gameInfoPanel.addGameConfig(configs);
        revalidate();
    }

    @Override
    public void setScoreboardData(String[][] configs) {
        for (String[] config : configs) {
            gameInfoPanel.addResult(config[0], Integer.parseInt(config[1]));
        }
        gameInfoPanel.refreshScoreboard();
        revalidate();
    }

    @Override
    public void addSnake(boolean isOwn, int[][] coords) {
        playgroundPanel.addSnake(isOwn, coords);
        revalidate();
    }

    @Override
    public void setFood(int x, int y){
        playgroundPanel.setFood(x, y);
        revalidate();
    }

    @Override
    public void setPlaygroundSize(int width, int height){
        playgroundPanel.setGridSize(height, width);
        revalidate();
    }

    @Override
    public void clearPlayground(){
        playgroundPanel.clear();
        revalidate();
    }
}
