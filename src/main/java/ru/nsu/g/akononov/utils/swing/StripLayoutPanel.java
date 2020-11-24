package ru.nsu.g.akononov.utils.swing;

import javax.swing.*;
import java.awt.*;

public class StripLayoutPanel extends JPanel {
    private final GridBagConstraints constraints = new GridBagConstraints();
    private final boolean isVerticalStrip;
    private int panelCount = 0;

    public StripLayoutPanel(boolean isVerticalStrip) {
        this.isVerticalStrip = isVerticalStrip;

        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;

        constraints.gridx = 0;
        constraints.gridy = 0;
    }

    public void addPanel(int weight, JComponent panel){
        constraints.weighty = weight;
        constraints.weightx = weight;

        if(isVerticalStrip){
            constraints.gridy = panelCount++;
        } else {
            constraints.gridx = panelCount++;
        }
        add(panel, constraints);
        setVisible(true);
    }

    public void addEmptyPanel(int weight){
        addPanel(weight, new JLabel(""));
    }
}
