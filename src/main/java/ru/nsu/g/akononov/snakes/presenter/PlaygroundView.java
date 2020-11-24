package ru.nsu.g.akononov.snakes.presenter;

public interface PlaygroundView {
    public void addSnake(boolean isOwn, int[][] coords);

    public void setFood(int x, int y);

    public void setPlaygroundSize(int width, int height);

    public void clearPlayground();
}
