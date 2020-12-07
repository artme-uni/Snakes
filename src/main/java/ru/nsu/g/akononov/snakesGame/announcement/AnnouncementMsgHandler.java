package ru.nsu.g.akononov.snakesGame.announcement;

import me.ippolitov.fit.snakes.SnakesProto;

import java.net.SocketAddress;
import java.util.HashMap;

public interface AnnouncementMsgHandler {
    void add(SocketAddress address, SnakesProto.GameMessage.AnnouncementMsg announcements);
    void remove(SocketAddress address);
}
