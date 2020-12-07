package ru.nsu.g.akononov.snakesGame.transfer.msgHandlers;

import me.ippolitov.fit.snakes.SnakesProto;

public interface PingMsgHandler {
    void handle(SnakesProto.GameMessage.PingMsg newMessage);
}
