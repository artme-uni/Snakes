package ru.nsu.g.akononov.snakes.model;

import me.ippolitov.fit.snakes.SnakesProto;

public interface ObservableState {
    void registerObserver(StateHandler observer);
    void sendNewState(SnakesProto.GameState state);
}
