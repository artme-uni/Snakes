package ru.nsu.g.akononov.snakesGame.transfer.msgHandlers;

import me.ippolitov.fit.snakes.SnakesProto;

public interface ErrorMsgHandler {
    void handle(SnakesProto.GameMessage.ErrorMsg newMessage);
}
