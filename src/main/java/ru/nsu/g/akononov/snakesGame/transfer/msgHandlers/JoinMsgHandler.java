package ru.nsu.g.akononov.snakesGame.transfer.msgHandlers;

import me.ippolitov.fit.snakes.SnakesProto;

import java.net.SocketAddress;

public interface JoinMsgHandler {
    void handle(SnakesProto.GameMessage newMessage, SocketAddress address);
}
