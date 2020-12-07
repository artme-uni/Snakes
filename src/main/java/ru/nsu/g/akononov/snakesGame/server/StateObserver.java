package ru.nsu.g.akononov.snakesGame.server;

import me.ippolitov.fit.snakes.SnakesProto;

public interface StateObserver {
    void updateState(SnakesProto.GameState state);
}
