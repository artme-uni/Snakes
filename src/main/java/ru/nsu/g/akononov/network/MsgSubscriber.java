package ru.nsu.g.akononov.network;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public interface MsgSubscriber {
    void handleMsg(byte[] messageData, SocketAddress socket);
}
