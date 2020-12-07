package ru.nsu.g.akononov.snakesGame.view.game.gameInfo;

import ru.nsu.g.akononov.snakesGame.view.ColorTheme;
import ru.nsu.g.akononov.utils.swing.ColoredButton;
import ru.nsu.g.akononov.utils.swing.StripLayoutPanel;
import ru.nsu.g.akononov.utils.swing.TablePanel;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class GameInfoPanel extends StripLayoutPanel {
    private final static Border border = BorderFactory.createEmptyBorder(10, 10, 10, 10);

    private final TablePanel scoreboardPanel = new TablePanel(new String[]{"USERNAME", "SCORE"});
    private final TablePanel gameConfigPanel = new TablePanel(new String[]{"CONFIG", "VALUE"});

    private final HashMap<String, Integer> scores = new LinkedHashMap<>();

    private final ColoredButton exitButton = new ColoredButton("EXIT");

    JScrollPane scrollConfig = new JScrollPane();
    JScrollPane scrollScoreboard = new JScrollPane();

    public GameInfoPanel() {
        super(true);

        setBorder(border);
        setColoration();

        scoreboardPanel.setEnabled(false);
        gameConfigPanel.setEnabled(false);

        scrollConfig.setColumnHeaderView(gameConfigPanel);
        scrollConfig.setViewportView(gameConfigPanel);
        addPanel(10, scrollConfig);

        addEmptyPanel(1);

        scrollScoreboard.setColumnHeaderView(scoreboardPanel);
        scrollScoreboard.setViewportView(scoreboardPanel);
        addPanel(20, scrollScoreboard);

        addEmptyPanel(4);
        addPanel(4, exitButton);
    }

    private void setColoration() {
        ColorTheme coloration = ColorTheme.getInstance();
        if (coloration.background != null) {
            setBackground(coloration.background);
        }

        Color headerColor = null;
        Color cellColor = null;

        exitButton.setColoration(coloration.mainElements);

        if (coloration.mainElements != null) {
            cellColor = coloration.mainElements;
            scrollConfig.getViewport().setBackground(coloration.mainElements);
            scrollScoreboard.getViewport().setBackground(coloration.mainElements);
        }

        if (coloration.otherElements != null) {
            headerColor = coloration.otherElements;
        }

        scoreboardPanel.setColoration(headerColor, cellColor);
        gameConfigPanel.setColoration(headerColor, cellColor);
    }

    public void addResult(String username, Integer score) {
        scores.put(username, score);
    }

    public void refreshScoreboard() {
        ArrayList<Map.Entry<String, Integer>> scoresList = scores.entrySet().stream()
                .sorted(Map.Entry.comparingByValue((k1, k2) -> k2 - k1))
                .collect(Collectors.toCollection(ArrayList::new));

        scoreboardPanel.clearTable();
        for (Map.Entry<String, Integer> entry : scoresList) {
            scoreboardPanel.addRecord(entry.getKey(), String.valueOf(entry.getValue()));
        }

        scores.clear();
    }

    public void addGameConfig(String[][] configs){
        gameConfigPanel.clearTable();
        for (int i = 0; i < configs.length; i++) {
            gameConfigPanel.addRecord(configs[i][0], configs[i][1]);
        }
    }

    public JButton getExitButton() {
        return exitButton;
    }
}
