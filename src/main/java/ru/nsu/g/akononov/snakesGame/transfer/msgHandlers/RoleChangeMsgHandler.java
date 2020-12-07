package ru.nsu.g.akononov.snakesGame.transfer.msgHandlers;

import me.ippolitov.fit.snakes.SnakesProto;

public interface RoleChangeMsgHandler {
    void handle(SnakesProto.GameMessage.RoleChangeMsg newMessage);
}
