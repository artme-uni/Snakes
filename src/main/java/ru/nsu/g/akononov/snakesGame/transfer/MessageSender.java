package ru.nsu.g.akononov.snakesGame.transfer;

import me.ippolitov.fit.snakes.SnakesProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.g.akononov.network.MsgSender;
import ru.nsu.g.akononov.network.UdpMsgSender;
import ru.nsu.g.akononov.snakesGame.acknowledgement.Acknowledgement;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Date;

import static me.ippolitov.fit.snakes.SnakesProto.*;

public class MessageSender {
    private static final Logger logger = LoggerFactory.getLogger(MessageSender.class.getSimpleName());

    private final MsgSender sender;

    private final InetSocketAddress announcementMulticastAddr;
    private Acknowledgement acknowledgement;

    private final Date lastSendTime;
    private InetSocketAddress server;

    private int messageSequence = 0;

    public MessageSender(DatagramSocket socket, InetSocketAddress announcementMulticastAddr, Date lastSendTime) {
        this.sender = new UdpMsgSender(socket);
        this.lastSendTime = lastSendTime;

        this.announcementMulticastAddr = announcementMulticastAddr;
    }

    public void setServerAddress(InetSocketAddress serverAddress) {
        this.server = serverAddress;
    }

    public void setAcknowledgement(Acknowledgement acknowledgement) {
        this.acknowledgement = acknowledgement;
    }

    public void setMessageSequence(int messageSequence) {
        this.messageSequence = messageSequence;
    }

    public void sendJoinAckMsg(long messageSequence, int receiverID, int senderID, InetSocketAddress inetSocketAddress) {
        GameMessage message = GameMessage.newBuilder()
                .setAck(GameMessage.AckMsg.newBuilder().build())
                .setMsgSeq(messageSequence)
                .setSenderId(senderID)
                .setReceiverId(receiverID)
                .build();

        sendUnicast(message, inetSocketAddress);
        logger.trace("Send join ACK #{} to {}", messageSequence, inetSocketAddress);
    }

    public void sendJoinAckMsg(long messageSequence, InetSocketAddress inetSocketAddress) {
        GameMessage message = GameMessage.newBuilder()
                .setAck(GameMessage.AckMsg.newBuilder().build())
                .setMsgSeq(messageSequence)
                .build();

        sendUnicast(message, inetSocketAddress);
        logger.trace("Send ACK #{} to {}", messageSequence, inetSocketAddress);
    }

    public void addPlayer(InetSocketAddress inetSocketAddress) {
        sender.addDestination(inetSocketAddress);
    }

    public void removePlayer(InetSocketAddress inetSocketAddress) {
        sender.removeDestination(inetSocketAddress);
    }

    public void clearDestination() {
        sender.clearDestinations();
    }

    public void sendErrorMsg(String errorMsg, InetSocketAddress address) {
        GameMessage.ErrorMsg errMsg = GameMessage.ErrorMsg.newBuilder()
                .setErrorMessage(errorMsg)
                .build();

        sender.sendUnicast(errMsg.toByteArray(), address);
        logger.debug("Send ERROR #{} to {}", messageSequence, address);
    }

    public GameMessage sendJoinMsg(InetSocketAddress destination, String name) {
        GameMessage.JoinMsg joinMsg = GameMessage.JoinMsg.newBuilder()
                .setPlayerType(PlayerType.HUMAN)
                .setOnlyView(false)
                .setName(name)
                .build();

        GameMessage message = GameMessage.newBuilder()
                .setMsgSeq(++messageSequence)
                .setJoin(joinMsg)
                .build();

        sendUnicast(message, destination);
        logger.trace("Send JOIN #{} to {}", messageSequence, destination);

        return message;
    }

    public void sendPingMsg() {
        if (server != null) {
            GameMessage message = GameMessage.newBuilder()
                    .setMsgSeq(++messageSequence)
                    .setPing(GameMessage.PingMsg.newBuilder().build())
                    .build();

            sendUnicast(message, server);
            logger.trace("Send PING #{} to {}", messageSequence, server);
        }
    }

    public void sendOwnRoleMsg(NodeRole ownRole, Integer senderID, InetSocketAddress receiverAddress) {
        GameMessage.RoleChangeMsg roleChangeMsg = GameMessage.RoleChangeMsg.newBuilder()
                .setSenderRole(ownRole)
                .build();

        GameMessage message = GameMessage.newBuilder()
                .setMsgSeq(++messageSequence)
                .setRoleChange(roleChangeMsg)
                .setSenderId(senderID)
                .build();

        sendUnicast(message, receiverAddress);
        logger.trace("Send msg #{} with OWN-ROLE ({}) to {}", messageSequence, ownRole, receiverAddress);
    }

    public void sendReceiverRoleMsg(NodeRole receiverRole, Integer receiverID, InetSocketAddress receiverAddress) {
        GameMessage.RoleChangeMsg roleChangeMsg = GameMessage.RoleChangeMsg.newBuilder()
                .setReceiverRole(receiverRole)
                .build();

        GameMessage message = GameMessage.newBuilder()
                .setMsgSeq(++messageSequence)
                .setRoleChange(roleChangeMsg)
                .setReceiverId(receiverID)
                .build();

        sendUnicast(message, receiverAddress);
        logger.trace("Send msg #{} with ROLE ({}) to {}", messageSequence, receiverRole, receiverAddress);
    }


    public void sendStateMsg(SnakesProto.GameState state) {
        GameMessage message = GameMessage.newBuilder()
                .setMsgSeq(++messageSequence)
                .setState(GameMessage.StateMsg.newBuilder().setState(state).build())
                .build();

        sendBroadcast(message);
        logger.trace("Send STATE №{} to BROAD", state.getStateOrder());
    }

    public void sendSteerMsg(Direction direction) {
        GameMessage message = GameMessage.newBuilder()
                .setMsgSeq(++messageSequence)
                .setSteer(GameMessage.SteerMsg.newBuilder().setDirection(direction).build())
                .build();

        sendBroadcast(message);
        logger.trace("Send STEER #{}", messageSequence);
    }

    public void sendAnnouncementMsg(boolean canJoin, GamePlayers players, GameConfig config) {
        GameMessage.AnnouncementMsg announcementMsg = GameMessage.AnnouncementMsg.newBuilder()
                .setCanJoin(canJoin)
                .setPlayers(players)
                .setConfig(config)
                .build();

        GameMessage gameMessage = GameMessage.newBuilder()
                .setMsgSeq(++messageSequence)
                .setAnnouncement(announcementMsg)
                .build();

        sendUnicast(gameMessage, announcementMulticastAddr);
        logger.trace("Send ANNOUNCEMENT #{} to {}", messageSequence, announcementMulticastAddr);
    }

    private void sendBroadcast(GameMessage message) {
        sender.sendBroadcast(message.toByteArray());

        if (hasToBeConfirmed(message)) {
            for (InetSocketAddress destination : sender.getDestinations()) {
                setLastSendTime(destination);
                acknowledgement.addMessageToBeConfirmed(message, destination);
            }
        }
    }

    private void sendUnicast(GameMessage message, InetSocketAddress socketAddress) {
        setLastSendTime(socketAddress);
        sender.sendUnicast(message.toByteArray(), socketAddress);

        if (hasToBeConfirmed(message)) {
            acknowledgement.addMessageToBeConfirmed(message, socketAddress);
        }
    }

    private void setLastSendTime(InetSocketAddress socketAddress) {
        if (server != null) {
            if (socketAddress.toString().equals(server.toString())) {
                synchronized (lastSendTime) {
                    lastSendTime.setTime(System.currentTimeMillis());
                }
            }
        }
    }

    private boolean hasToBeConfirmed(GameMessage message) {
        return message.getTypeCase() != GameMessage.TypeCase.ACK && message.getTypeCase() != GameMessage.TypeCase.ANNOUNCEMENT;
    }

    public void send(GameMessage message, InetSocketAddress destination) {
        sender.sendUnicast(message.toByteArray(), destination);
    }
}
