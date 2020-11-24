package ru.nsu.g.akononov.snakes.view.game.playground;

import ru.nsu.g.akononov.snakes.view.ColorTheme;

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

    public void setGridSize(int rowsCount, int colsCount){
        layoutManager.setRows(rowsCount);
        layoutManager.setColumns(colsCount);
        initBlocks();
    }

    public void initBlocks() {
        removeAll();

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

    public void setFood(int x, int y){
        setType(x, y, BlockType.FOOD);
    }

    public void setEmpty(int x, int y){
        setType(x, y, BlockType.EMPTY);
    }

    public void setSnake(boolean isOwn, boolean isBody, int x, int y) {
        BlockType blockType;

        if (isOwn) {
            blockType = isBody ? BlockType.OWN_BODY : BlockType.OWN_HEAD;
        } else {
            blockType = isBody ? BlockType.OTHER_BODY : BlockType.OTHER_HEAD;
        }

        setType(x, y, blockType);
    }

    public void addSnake(boolean isOwn, int[]... offsets) {
        setSnake(isOwn, false, offsets[0][0], offsets[0][1]);

        int[][] coords = new int[offsets.length][2];
        coords[0] = offsets[0];

        for (int j = 1; j < offsets.length; j++) {
            coords[j][0] = coords[j-1][0] + offsets[j][0];
            coords[j][1] = coords[j-1][1] + offsets[j][1];
        }

        for (int i = 0; i < coords.length - 1; i++) {
            paintStrip(coords[i][0], coords[i][1], coords[i + 1][0], coords[i + 1][1], isOwn);
        }
    }

    private void paintStrip(int currentX, int currentY, int nextX, int nextY, boolean isOwn){
        if (currentX != nextX && currentY != nextY) {
            throw new IllegalArgumentException();
        }
        int dist;
        int isOffsetX = 0;
        int isOffsetY = 0;

        if (currentX == nextX) {
            dist = nextY - currentY;
            isOffsetY = 1;
        } else {
            dist = nextX - currentX;
            isOffsetX = 1;
        }

        int sign = dist > 0 ? 1 : -1;

        for (int j = 1; j <= Math.abs(dist); j++) {
            setSnake(isOwn, true, currentX + sign * j * isOffsetX, currentY + sign * j * isOffsetY);
        }
    }

    public void clear(){
        int rowsCount = layoutManager.getRows();
        int colsCount = layoutManager.getColumns();

        for (int i = 0; i < rowsCount; i++) {
            for (int j = 0; j < colsCount; j++) {
                blocks[i][j].setEmpty(i , j);
                add(blocks[i][j]);
            }
        }
    }
}
