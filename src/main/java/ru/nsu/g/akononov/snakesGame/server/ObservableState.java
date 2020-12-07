package ru.nsu.g.akononov.snakesGame.server;

import me.ippolitov.fit.snakes.SnakesProto;

public interface ObservableState {
    void registerObserver(StateObserver observer);
    void sendNewState(SnakesProto.GameState state);
}
