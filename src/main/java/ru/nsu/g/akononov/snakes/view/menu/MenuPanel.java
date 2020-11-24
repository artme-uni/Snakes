package ru.nsu.g.akononov.snakes.view.menu;

import ru.nsu.g.akononov.snakes.view.ColorTheme;
import ru.nsu.g.akononov.utils.swing.StripLayoutPanel;

import javax.swing.*;
import java.awt.*;

public class MenuPanel extends StripLayoutPanel {

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
}
