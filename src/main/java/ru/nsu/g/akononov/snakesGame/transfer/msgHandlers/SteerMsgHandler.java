package ru.nsu.g.akononov.snakesGame.transfer.msgHandlers;

import me.ippolitov.fit.snakes.SnakesProto;

import java.net.SocketAddress;

public interface SteerMsgHandler {
    void handle(SnakesProto.GameMessage.SteerMsg newMessage, SocketAddress source);
}
