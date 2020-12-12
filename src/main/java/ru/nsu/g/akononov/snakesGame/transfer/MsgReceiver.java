package ru.nsu.g.akononov.snakesGame.transfer;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.g.akononov.network.MsgSubscriber;
import ru.nsu.g.akononov.snakesGame.node.NodeStateType;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import static me.ippolitov.fit.snakes.SnakesProto.GameMessage;

public class MsgReceiver implements MsgSubscriber, MsgManager {
    private static final Logger logger = LoggerFactory.getLogger(MsgReceiver.class.getSimpleName());

    InetSocketAddress server;
    private NodeStateType stateType;
    private final MessageSender transfer;

    private final ConcurrentHashMap<InetSocketAddress, Date> lastReceivedMsgTime;

    public MsgReceiver(NodeStateType stateType, MessageSender transfer,
                       ConcurrentHashMap<InetSocketAddress, Date> lastReceivedMsgTime) {
        this.transfer = transfer;
        this.stateType = stateType;

        this.lastReceivedMsgTime = lastReceivedMsgTime;
    }

    public void setStateType(NodeStateType stateType) {
        this.stateType = stateType;
    }

    public void setServerAddress(InetSocketAddress serverAddress) {
        this.server = serverAddress;
    }

    public void manageNewMsg(byte[] message, SocketAddress source) {
        try {
            GameMessage gameMsg = GameMessage.parseFrom(message);
            GameMessage.TypeCase msgType = gameMsg.getTypeCase();

            InetSocketAddress inetSource = (InetSocketAddress) source;

            if (stateType == NodeStateType.JOINED && !source.toString().equals(server.toString())) {
                return;
            }
            if (msgType != GameMessage.TypeCase.ACK) {
                if (stateType == NodeStateType.FINDING) {
                    return;
                }
                if (stateType == NodeStateType.CREATOR && (msgType == GameMessage.TypeCase.STATE)) {
                    return;
                }
            }

            if (msgType != GameMessage.TypeCase.ANNOUNCEMENT) {
                lastReceivedMsgTime.put(inetSource, new Date());
            }
            if (hasToBeConfirmed(gameMsg)) {
                transfer.sendJoinAckMsg(gameMsg.getMsgSeq(), inetSource);
            }

            switch (msgType) {
                case ACK:
                case JOIN:
                case ROLE_CHANGE:
                    notifyHandlers(gameMsg, source);
                    break;
                case ERROR:
                    notifyHandlers(gameMsg.getError());
                    break;
                case PING:
                    notifyHandlers(gameMsg.getPing());
                    break;
                case STATE:
                    notifyHandlers(gameMsg.getState());
                    break;
                case STEER:
                    notifyHandlers(gameMsg.getSteer(), source);
                    break;
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }


    private boolean hasToBeConfirmed(GameMessage message) {
        return message.getTypeCase() != GameMessage.TypeCase.ACK && message.getTypeCase() != GameMessage.TypeCase.ANNOUNCEMENT;
    }

    @Override
    public void handleMsg(byte[] messageData, SocketAddress source) {
        manageNewMsg(messageData, source);
    }
}
