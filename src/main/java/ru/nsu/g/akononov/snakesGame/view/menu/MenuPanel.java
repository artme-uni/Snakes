package ru.nsu.g.akononov.snakesGame.view.menu;

import ru.nsu.g.akononov.snakesGame.presenter.CreationPanelView;
import ru.nsu.g.akononov.snakesGame.presenter.JoiningPanelView;
import ru.nsu.g.akononov.snakesGame.view.ColorTheme;
import ru.nsu.g.akononov.utils.swing.StripLayoutPanel;

import javax.swing.*;

public class MenuPanel extends StripLayoutPanel implements CreationPanelView, JoiningPanelView {

    private final GameCreationPanel creationPanel = new GameCreationPanel();
    private final ActiveGamesPanel joiningPanel = new ActiveGamesPanel();

    public MenuPanel() {
        super(false);

        addEmptyPanel(1);

        addPanel(1, creationPanel);

        addEmptyPanel(1);

        addPanel(1, joiningPanel);

        addEmptyPanel(1);
        setColoration();
    }

    public GameCreationPanel getCreationPanel() {
        return creationPanel;
    }

    public ActiveGamesPanel getJoiningPanel() {
        return joiningPanel;
    }

    private void setColoration() {
        ColorTheme coloration = ColorTheme.getInstance();
        if (coloration.background != null) {
            setBackground(coloration.background);
        }
    }

    public JButton getJoiningButton(){
        return joiningPanel.getJoiningButton();
    }

    public JButton getCreatingButton(){
        return creationPanel.getCreationButton();
    }

    public String getSelectedHost(){
        int index = joiningPanel.activeGames.getSelectedRow();
        if(index < 0) {
            throw new RuntimeException();
        }

        return joiningPanel.activeGames.getRecord(index, 0);
    }


    @Override
    public void addGame(String[] game) {
        joiningPanel.addActiveGame(game[0], game[1]);
    }

    @Override
    public void removeGame(String host) {
        joiningPanel.removeGameRecord(host);
    }
}
