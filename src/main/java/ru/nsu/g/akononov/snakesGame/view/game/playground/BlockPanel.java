package ru.nsu.g.akononov.snakesGame.view.game.playground;

import ru.nsu.g.akononov.snakesGame.view.ColorTheme;

import javax.swing.*;
import java.awt.*;

public class BlockPanel extends JPanel {
    private BlockType type;
    ColorTheme coloration = ColorTheme.getInstance();

    public BlockPanel(int i, int j) {
        setEmpty(i, j);
    }

    public void setType(BlockType type) {
        this.type = type;

        Color currentColor = coloration.impairBlock;
        switch (type) {
            case OWN_BODY:
                currentColor = coloration.ownBody;
                break;
            case OWN_HEAD:
                currentColor = coloration.ownHead;
                break;
            case OTHER_BODY:
                currentColor = coloration.otherBody;
                break;
            case OTHER_HEAD:
                currentColor = coloration.otherHead;
                break;
            case FOOD:
                currentColor = coloration.food;
                break;
            case ZOMBIE_BODY:
                currentColor = coloration.zombieBody;
                break;
            case ZOMBIE_HEAD:
                currentColor = coloration.zombieHead;
                break;
        }
        if (currentColor != Color.BLACK) {
            setBackground(currentColor);
        }
    }

    public void setEmpty(int i, int j) {
        this.type = BlockType.EMPTY;
        Color currentColor = coloration.impairBlock;

        if (i % 2 == 0 && j % 2 == 0) {
            currentColor = coloration.pairBlock;
        } else if (i % 2 != 0 && j % 2 != 0) {
            currentColor = coloration.pairBlock;
        }

        if (currentColor != Color.BLACK) {
            setBackground(currentColor);
        }
    }

    public BlockType getType() {
        return type;
    }
}
