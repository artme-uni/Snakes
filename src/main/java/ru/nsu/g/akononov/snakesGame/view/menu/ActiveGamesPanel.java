package ru.nsu.g.akononov.snakesGame.view.menu;

import ru.nsu.g.akononov.snakesGame.view.ColorTheme;
import ru.nsu.g.akononov.utils.swing.ColoredButton;
import ru.nsu.g.akononov.utils.swing.StripLayoutPanel;
import ru.nsu.g.akononov.utils.swing.TablePanel;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ActiveGamesPanel extends StripLayoutPanel {
    private final static ColorTheme colorTheme = ColorTheme.getInstance();

    ColoredButton joiningButton = new ColoredButton("JOIN");
    TablePanel activeGames = new TablePanel(new String[]{"HOST"}, 55);
    JScrollPane scrollTable = new JScrollPane();


    JTextField textField = new JTextField("Player #" + (int) (Math.random() * 100000));

    HashMap<String, Integer> rowsIndexes = new LinkedHashMap<>();
    int nextRowIndex = 0;

    public ActiveGamesPanel() {
        super(true);

        scrollTable.setColumnHeaderView(activeGames);
        scrollTable.setViewportView(activeGames);
        scrollTable.getViewport().setBackground(colorTheme.mainElements);

        activeGames.setColoration(colorTheme.otherElements, colorTheme.mainElements);
        setBackground(colorTheme.background);
        getJoiningButton().setColoration(colorTheme.mainElements);

        textField.setBackground(colorTheme.background);
        textField.setHorizontalAlignment(SwingConstants.CENTER);

        addEmptyPanel(1);
        addPanel(1, textField);
        addEmptyPanel(1);
        addPanel(5, scrollTable);
        addEmptyPanel(1);
        addPanel(1, joiningButton);
        addEmptyPanel(1);
    }

    public ColoredButton getJoiningButton() {
        return joiningButton;
    }

    public void clearTable(){
        activeGames.clearTable();
    }

    public void addActiveGame(String host, String playersCount){
        activeGames.addRecord(host);
        rowsIndexes.put(host, nextRowIndex);
        nextRowIndex++;
    }

    public void removeGameRecord(String host){
        int indexForRemove = rowsIndexes.get(host);

        if(indexForRemove >= rowsIndexes.entrySet().size()){
            throw new IllegalArgumentException();
        }
        for(Map.Entry<String, Integer> entry : rowsIndexes.entrySet()){
            int currentIndex = entry.getValue();

            if(currentIndex > indexForRemove){
                entry.setValue(currentIndex - 1);
            }
        }
        nextRowIndex--;

        activeGames.removeRecord(indexForRemove);
    }

    public String getUsername(){
        return textField.getText();
    }

}
