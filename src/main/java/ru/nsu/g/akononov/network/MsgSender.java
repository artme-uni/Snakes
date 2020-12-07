package ru.nsu.g.akononov.network;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;

public interface MsgSender {
    void addDestination(SocketAddress destination);
    void removeDestination(SocketAddress destination);
    void clearDestinations();
    void sendBroadcast(byte[] message);
    void sendUnicast(byte[] message, SocketAddress unicastDestination);
    ArrayList<InetSocketAddress> getDestinations();
}
