package ru.nsu.g.akononov.snakesGame.announcement;

import com.google.protobuf.InvalidProtocolBufferException;
import me.ippolitov.fit.snakes.SnakesProto;
import ru.nsu.g.akononov.network.MsgSubscriber;
import ru.nsu.g.akononov.snakesGame.transfer.MessageSender;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

import static me.ippolitov.fit.snakes.SnakesProto.*;

public class Announcement implements MsgSubscriber, Runnable, ObservableAnnouncementsList {
    private final static int TTL = 1200;
    private final static int SEND_PERIOD = 1000;
    private final MessageSender transfer;

    private long lastSendTime;

    private GamePlayers players = null;
    private GameConfig config = null;

    private HashMap<SocketAddress, GameMessage.AnnouncementMsg> announcements = new LinkedHashMap<>();
    private HashMap<SocketAddress, Date> receivedTime = new LinkedHashMap<>();


    public Announcement(InetSocketAddress groupAddress, MessageSender transfer, MulticastSocket socket) throws IOException {
        this.transfer = transfer;

        AnnouncementRadar radar = new AnnouncementRadar(groupAddress, socket);
        radar.subscribe(this);
        Thread receivingThread = new Thread(radar);
        receivingThread.start();

        Thread sendingThread = new Thread(this);
        sendingThread.start();
    }

    public void setPlayers(GamePlayers players) {
        this.players = players;
    }

    public void setConfig(GameConfig config) {
        this.config = config;
    }

    public void sendAnnounce() {
        if (players == null || config == null) {
            return;
        }
        transfer.sendAnnouncementMsg(true, players, config);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(100);
                long currentTime = System.currentTimeMillis();
                if((currentTime - lastSendTime) > SEND_PERIOD){
                    sendAnnounce();
                    lastSendTime = currentTime;
                }

                filterReceivedAnnouncements();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void filterReceivedAnnouncements() {
        long currentTime = new Date().getTime();

        List<SocketAddress> inactiveGames = receivedTime.entrySet().stream()
                .filter(e -> ((currentTime - e.getValue().getTime()) >= TTL))
                .map(Map.Entry::getKey).collect(Collectors.toList());

        if (!inactiveGames.isEmpty()) {
            announcements.keySet().removeAll(inactiveGames);
            receivedTime.keySet().removeAll(inactiveGames);
        }

        for (SocketAddress address : inactiveGames) {
            notifyHandlersToRemove(address);
        }
    }

    @Override
    public void handleMsg(byte[] messageData, SocketAddress address) {
        try {
            SnakesProto.GameMessage gameMsg = SnakesProto.GameMessage.parseFrom(messageData);
            if (gameMsg.hasAnnouncement()) {
                GameMessage.AnnouncementMsg newMsg = gameMsg.getAnnouncement();

                if (!announcements.containsKey(address)) {
                    notifyHandlersToAdd(address, newMsg);
                }
                announcements.put(address, newMsg);
                receivedTime.put(address, new Date());
            }

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }


}
