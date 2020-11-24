package ru.nsu.g.akononov.utils.swing;

import javax.swing.*;
import java.awt.*;

public class ColoredButton extends JButton {

    public ColoredButton(String text) {
        super(text);
    }

    public void setColoration(Color color){
        setBorderPainted(true);
        if (color != null) {
            setOpaque(true);
            setBorderPainted(false);
            setBackground(color);
        }
    }
}
