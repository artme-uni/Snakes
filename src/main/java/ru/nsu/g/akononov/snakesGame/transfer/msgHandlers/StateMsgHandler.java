package ru.nsu.g.akononov.snakesGame.transfer.msgHandlers;

import me.ippolitov.fit.snakes.SnakesProto;

public interface StateMsgHandler {
    void handle(SnakesProto.GameMessage.StateMsg newMessage);
    void gameEnd();
    void setPlayerID(int playerID);
}
