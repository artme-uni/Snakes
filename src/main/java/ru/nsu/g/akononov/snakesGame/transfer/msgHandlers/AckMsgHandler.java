package ru.nsu.g.akononov.snakesGame.transfer.msgHandlers;

import me.ippolitov.fit.snakes.SnakesProto;

import java.net.InetSocketAddress;

public interface AckMsgHandler {
    void handle(SnakesProto.GameMessage newMessage, InetSocketAddress source);
}
