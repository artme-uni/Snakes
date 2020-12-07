package ru.nsu.g.akononov.snakesGame.presenter;

public interface PlaygroundView {
    public void addSnake(boolean isOwn, boolean isZombie, int[][] coords);

    public void setFood(int x, int y);

    public void setPlaygroundSize(int width, int height);

    public void clearPlayground();
}
