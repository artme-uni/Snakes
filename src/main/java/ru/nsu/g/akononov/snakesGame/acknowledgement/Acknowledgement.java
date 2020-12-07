package ru.nsu.g.akononov.snakesGame.acknowledgement;

import me.ippolitov.fit.snakes.SnakesProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.g.akononov.snakesGame.node.Node;
import ru.nsu.g.akononov.snakesGame.transfer.MessageSender;
import ru.nsu.g.akononov.snakesGame.transfer.msgHandlers.AckMsgHandler;

import static me.ippolitov.fit.snakes.SnakesProto.*;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

public class Acknowledgement implements AckMsgHandler {
    private static final Logger logger = LoggerFactory.getLogger(Acknowledgement.class);

    private int WaitingAckTimeout = 200;
    private SnakesProto.GameMessage join = null;

    private final Node logic;
    private final MessageSender transfer;

    private final CopyOnWriteArrayList<MsgMetaData> messagesToBeConfirmed = new CopyOnWriteArrayList<>();

    public Acknowledgement(Node logic, MessageSender transfer) {
        this.logic = logic;
        this.transfer = transfer;

        Thread thread = new Thread(()->{
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(WaitingAckTimeout / 2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                checkNotConfirmedMessages();
            }
        });

        thread.start();
    }

    public void waitJoinAck(GameMessage message) {
        join = message;
    }

    public void setWaitingAckTimeout(int waitingAckTimeout) {
        WaitingAckTimeout = waitingAckTimeout;
    }

    private void checkNotConfirmedMessages() {
        for (MsgMetaData messageToBeConfirmed : messagesToBeConfirmed) {

            long sendTime = messageToBeConfirmed.getTime();
            long waitingAcknowledgeTime = System.currentTimeMillis() - sendTime;

            if (waitingAcknowledgeTime > WaitingAckTimeout) {
                resendMessage(messageToBeConfirmed);
            }
        }
    }

    private void resendMessage(MsgMetaData message) {
        logger.debug("RESEND {} to {}", message.getMessage().getMsgSeq(), message.getSecondPoint());
        transfer.send(message.getMessage(), message.getSecondPoint());
    }

    public void addMessageToBeConfirmed(GameMessage message, InetSocketAddress destination) {
        messagesToBeConfirmed.add(new MsgMetaData(message, destination, new Date()));
    }

    @Override
    public void handle(GameMessage newMessage, InetSocketAddress source) {
        if (!newMessage.hasAck()) {
            return;
        }

        if (join != null && newMessage.getMsgSeq() == join.getMsgSeq()) {
            logic.joinToServer(newMessage.getReceiverId(), source);
        }

        messagesToBeConfirmed.removeIf(metaData -> metaData.getMessage().getMsgSeq() == newMessage.getMsgSeq());
    }
}
