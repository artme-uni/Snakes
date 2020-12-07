package ru.nsu.g.akononov.network;

import java.net.SocketAddress;
import java.util.ArrayList;

public interface MsgReceiver {
    ArrayList<MsgSubscriber> subscribers = new ArrayList<>();

    default void subscribe(MsgSubscriber subscriber){
        if (subscriber == null) {
            throw new NullPointerException();
        }
        if (subscribers.contains(subscriber)) {
            throw new IllegalArgumentException("Repeated observer:" + subscriber);
        }
        subscribers.add(subscriber);
    }
    default void notifySubscribers(byte[] newMessage, SocketAddress source) {
        for (MsgSubscriber subscriber : subscribers) {
            subscriber.handleMsg(newMessage, source);
        }
    }
}
