package ru.nsu.g.akononov.snakesGame.presenter;

import me.ippolitov.fit.snakes.SnakesProto;

public interface Controller {
    void moveSnakeLeft();
    void moveSnakeRight();
    void moveSnakeUp();
    void moveSnakeDown();

    void createNewGame(String currentName, int height, int width, float foodPerPlayer,
                       int foodStatic, float deadFoodProb, int stateDelayMs, int pingDelay, int nodeTimout);
    void tryToJoin(String host, String currentName);

    void exitGame();
    void setConfig(SnakesProto.GameConfig config);
}
