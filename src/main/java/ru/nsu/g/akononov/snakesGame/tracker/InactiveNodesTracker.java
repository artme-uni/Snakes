package ru.nsu.g.akononov.snakesGame.tracker;

import java.net.InetSocketAddress;
import java.util.ArrayList;

public interface InactiveNodesTracker {
    ArrayList<InactiveNodesObserver> subscribers = new ArrayList<>();

    default void subscribe(InactiveNodesObserver subscriber){
        if (subscriber == null) {
            throw new NullPointerException();
        }
        if (subscribers.contains(subscriber)) {
            throw new IllegalArgumentException("Repeated observer:" + subscriber);
        }
        subscribers.add(subscriber);
    }

    default void unsubscribe(InactiveNodesObserver subscriber){
        subscribers.remove(subscriber);
    }

    default void notifyAboutInactiveNode(InetSocketAddress nodeAddress) {
        for (InactiveNodesObserver subscriber : subscribers) {
            subscriber.handleInactiveNode(nodeAddress);
        }
    }
}
