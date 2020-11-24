package ru.nsu.g.akononov.utils.swing;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class MainFrame extends JFrame {
    final private CardLayout cardLayout = new CardLayout();
    final private JPanel mainPanel = new JPanel(cardLayout);
    final private Border frameBorders = BorderFactory.createEmptyBorder(5, 5, 5, 5);

    public MainFrame(String frameName, double widthProportion, double heightProportion, double scale){
        setSize(widthProportion, heightProportion, scale);
        mainPanel.setBorder(frameBorders);
        add(mainPanel);
        pack();
        configureFrame(frameName);
    }

    private void configureFrame(String frameName) {
        setTitle(frameName);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setResizable(true);
        setFocusable(true);
        setLocationRelativeTo(null);
    }

    public void setSize(double widthProportion, double heightProportion, double scale) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double proportions = widthProportion/heightProportion;

        int width = (int) (screenSize.height * scale * proportions);
        int height = (int) (screenSize.height * scale);
        mainPanel.setPreferredSize(new Dimension(width, height));
    }

    public void switchPanel(){
        cardLayout.next(mainPanel);
    }

    public void addPanel(JPanel panel, String cardName){
        mainPanel.add(panel, cardName);
        setVisible(true);
    }

    public void setBackgroundColoration(Color color){
        mainPanel.setBackground(color);
    }

    public void ShowPanel(String cardName){
        cardLayout.show(mainPanel, cardName);
    }
}
