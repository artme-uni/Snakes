package ru.nsu.g.akononov.snakes.view.menu;

import ru.nsu.g.akononov.snakes.view.ColorTheme;
import ru.nsu.g.akononov.utils.swing.ColoredButton;
import ru.nsu.g.akononov.utils.swing.StripLayoutPanel;
import ru.nsu.g.akononov.utils.swing.TablePanel;

import javax.swing.*;

public class ActiveGamesPanel extends StripLayoutPanel {
    private final static ColorTheme colorTheme = ColorTheme.getInstance();

    ColoredButton joiningButton = new ColoredButton("JOIN");
    TablePanel activeGames = new TablePanel(new String[]{"HOST", "PLAYERS COUNT"}, 55);
    JScrollPane scrollTable = new JScrollPane();

    public ActiveGamesPanel() {
        super(true);

        scrollTable.setColumnHeaderView(activeGames);
        scrollTable.setViewportView(activeGames);
        scrollTable.getViewport().setBackground(colorTheme.mainElements);

        activeGames.setColoration(colorTheme.otherElements, colorTheme.mainElements);
        setBackground(colorTheme.background);
        getJoiningButton().setColoration(colorTheme.mainElements);

        addEmptyPanel(1);
        addPanel(5, scrollTable);
        addEmptyPanel(1);
        addPanel(1, joiningButton);
        addEmptyPanel(1);
    }

    public ColoredButton getJoiningButton() {
        return joiningButton;
    }
}
