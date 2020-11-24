package ru.nsu.g.akononov.snakes.model;

import me.ippolitov.fit.snakes.SnakesProto;

public interface StateHandler {

    public void handleState(SnakesProto.GameState state);
}
