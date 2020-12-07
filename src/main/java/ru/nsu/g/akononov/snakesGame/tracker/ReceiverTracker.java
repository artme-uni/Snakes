package ru.nsu.g.akononov.snakesGame.tracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ReceiverTracker implements InactiveNodesTracker {
    private static final Logger logger = LoggerFactory.getLogger(ReceiverTracker.class);

    private long nodeTimeout = 2000;
    private final ConcurrentHashMap<InetSocketAddress, Date> lastReceivedMsgTime;

    public ReceiverTracker(ConcurrentHashMap<InetSocketAddress, Date> lastReceivedMsgTime) {
        this.lastReceivedMsgTime = lastReceivedMsgTime;

        Thread thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(nodeTimeout / 2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                refreshInactiveNode();
            }
        });

        thread.start();
    }

    public void setNodeTimeout(long nodeTimeout) {
        this.nodeTimeout = nodeTimeout;
    }

    public void refreshInactiveNode() {
        lastReceivedMsgTime.keySet().stream().filter(this::isNodeInactive).
                collect(Collectors.toList()).forEach(this::addInactiveNode);
    }

    private boolean isNodeInactive(InetSocketAddress node) {
        Date messageTime = lastReceivedMsgTime.get(node);
        if (messageTime == null) {
            Date now = new Date();
            lastReceivedMsgTime.put(node, now);
            messageTime = now;
        }

        long inactivePeriod = new Date().getTime() - messageTime.getTime();

        return inactivePeriod > nodeTimeout;
    }

    private void addInactiveNode(InetSocketAddress node) {
        logger.debug("Cannot connect to " + node);
        lastReceivedMsgTime.remove(node);
        notifyAboutInactiveNode(node);
    }
}
