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
    private static final Logger logger = LoggerFactory.getLogger(MessageSender.class);

    private final DatagramSocket socket;
    private final MsgSender sender;

    private final InetSocketAddress announcementMulticastAddr;
    private Acknowledgement acknowledgement;

    private final Date lastSendTime;
    private InetSocketAddress server;

    private int messageSequence = 0;

    public MessageSender(DatagramSocket socket, InetSocketAddress announcementMulticastAddr, Date lastSendTime) {
        this.socket = socket;
        this.sender = new UdpMsgSender(socket);
        this.lastSendTime = lastSendTime;

        this.announcementMulticastAddr = announcementMulticastAddr;
    }

    public void setAcknowledgement(Acknowledgement acknowledgement){
        this.acknowledgement = acknowledgement;
    }

    public void setMessageSequence(int messageSequence) {
        this.messageSequence = messageSequence;
    }

    public void sendAckMsg(long messageSequence, int receiverID){
        GameMessage message = GameMessage.newBuilder()
                .setAck(GameMessage.AckMsg.newBuilder().build())
                .setMsgSeq(messageSequence)
                .setReceiverId(receiverID)
                .build();

        sendBroadcast(message);
        logger.debug("Send ACC #{}", messageSequence);
    }

    public void sendAckMsg(long messageSequence, InetSocketAddress inetSocketAddress){
        GameMessage message = GameMessage.newBuilder()
                .setAck(GameMessage.AckMsg.newBuilder().build())
                .setMsgSeq(messageSequence)
                .build();

        sendUnicast(message, inetSocketAddress);
        logger.debug("Send ACC #{}", messageSequence);
    }

    public void addPlayer(InetSocketAddress inetSocketAddress){
        sender.addDestination(inetSocketAddress);
    }

    public void removePlayer(InetSocketAddress inetSocketAddress){
        sender.removeDestination(inetSocketAddress);
    }

    public void clearDestination(){
        sender.clearDestinations();
    }

    public void sendErrorMsg(){

    }

    public GameMessage sendJoinMsg(InetSocketAddress destination, String name){
        GameMessage.JoinMsg joinMsg= GameMessage.JoinMsg.newBuilder()
                .setPlayerType(PlayerType.HUMAN)
                .setOnlyView(false)
                .setName(name)
                .build();

        GameMessage message = GameMessage.newBuilder()
                .setMsgSeq(++messageSequence)
                .setJoin(joinMsg)
                .build();

        sendUnicast(message, destination);
        logger.debug("Send JOIN #{} to {}", messageSequence, destination);

        return message;
    }

    public void sendPingMsg(){
        if(server != null) {
            GameMessage message = GameMessage.newBuilder()
                    .setMsgSeq(++messageSequence)
                    .setPing(GameMessage.PingMsg.newBuilder().build())
                    .build();

            sendUnicast(message, server);
        }
    }

    public void sendRoleMsg(){

    }

    public void sendStateMsg(SnakesProto.GameState state){
        GameMessage message = GameMessage.newBuilder()
                .setMsgSeq(++messageSequence)
                .setState(GameMessage.StateMsg.newBuilder().setState(state).build())
                .build();

        sendBroadcast(message);
        logger.debug("Send STATE #{} to BROAD", messageSequence);
    }

    public void sendSteerMsg(Direction direction){
        GameMessage message = GameMessage.newBuilder()
                .setMsgSeq(++messageSequence)
                .setSteer(GameMessage.SteerMsg.newBuilder().setDirection(direction).build())
                .build();

        sendBroadcast(message);
        logger.debug("Send STEER #{} to BROAD", messageSequence);
    }

    public void sendAnnouncementMsg(boolean canJoin, GamePlayers players, GameConfig config){
        GameMessage.AnnouncementMsg announcementMsg= GameMessage.AnnouncementMsg.newBuilder()
                .setCanJoin(canJoin)
                .setPlayers(players)
                .setConfig(config)
                .build();

        GameMessage gameMessage = GameMessage.newBuilder()
                .setMsgSeq(++messageSequence)
                .setAnnouncement(announcementMsg)
                .build();

        sendUnicast(gameMessage, announcementMulticastAddr);
        logger.debug("Send ANNOUNCEMENT #{} to {}", messageSequence, announcementMulticastAddr);
    }

    private void sendBroadcast(GameMessage message){
        sender.sendBroadcast(message.toByteArray());

        if(hasToBeConfirmed(message)) {
            for (InetSocketAddress destination : sender.getDestinations()) {
                setLastSendTime(destination);
                acknowledgement.addMessageToBeConfirmed(message, destination);
            }
        }
    }

    private void sendUnicast(GameMessage message, InetSocketAddress socketAddress){
        setLastSendTime(socketAddress);
        sender.sendUnicast(message.toByteArray(), socketAddress);

        if(hasToBeConfirmed(message)) {
            acknowledgement.addMessageToBeConfirmed(message, socketAddress);
        }
    }

    private void setLastSendTime(InetSocketAddress socketAddress){
        if(server != null){
            if(socketAddress.toString().equals(server.toString())){
                synchronized (lastSendTime) {
                    lastSendTime.setTime(System.currentTimeMillis());
                }
            }
        }
    }

    private boolean hasToBeConfirmed(GameMessage message){
        return message.getTypeCase() != GameMessage.TypeCase.ACK && message.getTypeCase() != GameMessage.TypeCase.ANNOUNCEMENT;
    }

    public void send(GameMessage message, InetSocketAddress destination){
        sender.sendUnicast(message.toByteArray(), destination);
    }
}
