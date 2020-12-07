package ru.nsu.g.akononov.snakesGame.announcement;

import me.ippolitov.fit.snakes.SnakesProto;

import java.net.SocketAddress;
import java.util.ArrayList;

public interface ObservableAnnouncementsList {
    ArrayList<AnnouncementMsgHandler> announcementMsgHandlers = new ArrayList<>();

    default void registerMsgHandler(AnnouncementMsgHandler handler){
        if (handler == null) {
            throw new NullPointerException();
        }
        if (announcementMsgHandlers.contains(handler)) {
            throw new IllegalArgumentException("Repeated observer:" + handler);
        }
        announcementMsgHandlers.add(handler);
    }

    default void removeHandler(AnnouncementMsgHandler handler){
        announcementMsgHandlers.remove(handler);
    }

    default void notifyHandlersToAdd(SocketAddress address, SnakesProto.GameMessage.AnnouncementMsg msg){
        for (AnnouncementMsgHandler handler : announcementMsgHandlers) {
            handler.add(address, msg);
        }
    }

    default void notifyHandlersToRemove(SocketAddress address){
        for (AnnouncementMsgHandler handler : announcementMsgHandlers) {
            handler.remove(address);
        }
    }

}
