package ru.nsu.g.akononov.snakesGame.trackers;

import java.net.InetSocketAddress;

public interface InactiveNodesObserver {
    void handleInactiveNode(InetSocketAddress nodeAddress);
}
