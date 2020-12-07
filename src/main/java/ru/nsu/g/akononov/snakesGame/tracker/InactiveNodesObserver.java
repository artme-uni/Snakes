package ru.nsu.g.akononov.snakesGame.tracker;

import java.net.InetSocketAddress;

public interface InactiveNodesObserver {
    void handleInactiveNode(InetSocketAddress nodeAddress);
}
