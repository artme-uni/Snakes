package ru.nsu.g.akononov.snakesGame.view.game.playground;

import ru.nsu.g.akononov.snakesGame.view.ColorTheme;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class PlaygroundPanel extends JPanel {
    private final static Border border = BorderFactory.createEmptyBorder(10, 10, 10, 10);

    private final static int gapSize = ColorTheme.getInstance().gap;
    GridLayout layoutManager = new GridLayout();

    private BlockPanel[][] blocks;

    public PlaygroundPanel(int rowsCount, int colsCount) {
        layoutManager.setHgap(gapSize);
        layoutManager.setVgap(gapSize);
        setLayout(layoutManager);
        setBorder(border);

        setGridSize(rowsCount, colsCount);

        ColorTheme colorTheme = ColorTheme.getInstance();
        if (colorTheme.background != null) {
            setBackground(colorTheme.background);
        }
    }

    public void setGridSize(int rowsCount, int colsCount) {
        removeAll();

        layoutManager.setRows(rowsCount);
        layoutManager.setColumns(colsCount);
        setLayout(layoutManager);
        initBlocks();
    }

    public void initBlocks() {
        int rowsCount = layoutManager.getRows();
        int colsCount = layoutManager.getColumns();

        blocks = new BlockPanel[rowsCount][colsCount];

        for (int i = 0; i < rowsCount; i++) {
            for (int j = 0; j < colsCount; j++) {
                blocks[i][j] = new BlockPanel(i, j);
                add(blocks[i][j]);
            }
        }
    }

    private void setType(int i, int j, BlockType type) {
        blocks[j][i].setType(type);
    }

    public void setFood(int x, int y) {
        setType(x, y, BlockType.FOOD);
    }

    public void setEmpty(int x, int y) {
        setType(x, y, BlockType.EMPTY);
    }

    public void setSnake(boolean isOwn, boolean isZombie, boolean isBody, int x, int y) {
        BlockType blockType;

        if (isOwn) {
            blockType = isBody ? BlockType.OWN_BODY : BlockType.OWN_HEAD;
        } else {
            blockType = isBody ? BlockType.OTHER_BODY : BlockType.OTHER_HEAD;
        }

        if (isZombie) {
            blockType = isBody ? BlockType.ZOMBIE_BODY : BlockType.ZOMBIE_HEAD;
        }

        setType(x, y, blockType);
    }

    public void addSnake(boolean isOwn, boolean isZombie, int[]... offsets) {
        setSnake(isOwn, isZombie, false, offsets[0][0], offsets[0][1]);

        int[][] coords = new int[offsets.length][2];
        coords[0] = offsets[0];

        for (int j = 1; j < offsets.length; j++) {
            coords[j][0] = coords[j - 1][0] + offsets[j][0];
            coords[j][1] = coords[j - 1][1] + offsets[j][1];
        }

        for (int i = 0; i < coords.length - 1; i++) {
            paintStrip(coords[i][0], coords[i][1], offsets[i + 1][0], offsets[i + 1][1], isOwn, isZombie);
        }
    }

    private void paintStrip(int currentX, int currentY, int offsetX, int offsetY, boolean isOwn, boolean isZombie) {
        boolean isOffsetX = offsetX != 0;
        boolean isOffsetY = offsetY != 0;

        int height = layoutManager.getRows();
        int width = layoutManager.getColumns();

        if (isOffsetX && isOffsetY) {
            throw new RuntimeException(offsetX + "," + offsetY);
        }
        if (!isOffsetX && !isOffsetY) {
            return;
        }

        int offset = isOffsetX ? offsetX : offsetY;
        int sign = offset > 0 ? 1 : -1;

        for (int i = 1; i <= Math.abs(offset); i++) {
            int x = (currentX + sign * i * (isOffsetX ? 1 : 0)) % width;
            if (x < 0) {
                x = x + height;
            }
            int y = (currentY + sign * i * (isOffsetY ? 1 : 0)) % height;
            if (y < 0) {
                y = y + height;
            }

            setSnake(isOwn, isZombie, true, x, y);
        }
    }

    public void clear() {
        int rowsCount = layoutManager.getRows();
        int colsCount = layoutManager.getColumns();

        for (int i = 0; i < rowsCount; i++) {
            for (int j = 0; j < colsCount; j++) {
                blocks[i][j].setEmpty(i, j);
            }
        }
    }
}
