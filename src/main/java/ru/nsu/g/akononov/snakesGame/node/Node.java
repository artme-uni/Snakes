package ru.nsu.g.akononov.snakesGame.node;

import me.ippolitov.fit.snakes.SnakesProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.g.akononov.network.UdpMsgReceiver;
import ru.nsu.g.akononov.snakesGame.replacement.Replacement;
import ru.nsu.g.akononov.snakesGame.trackers.InactiveNodesObserver;
import ru.nsu.g.akononov.snakesGame.trackers.ReceiverTracker;
import ru.nsu.g.akononov.snakesGame.trackers.SenderTracker;
import ru.nsu.g.akononov.snakesGame.transfer.MessageSender;
import ru.nsu.g.akononov.snakesGame.acknowledgement.Acknowledgement;
import ru.nsu.g.akononov.snakesGame.announcement.Announcement;
import ru.nsu.g.akononov.snakesGame.presenter.Controller;
import ru.nsu.g.akononov.snakesGame.server.Server;
import ru.nsu.g.akononov.snakesGame.server.StateObserver;
import ru.nsu.g.akononov.snakesGame.transfer.MsgReceiver;
import ru.nsu.g.akononov.snakesGame.announcement.AnnouncementMsgHandler;
import ru.nsu.g.akononov.snakesGame.transfer.msgHandlers.ErrorMsgHandler;
import ru.nsu.g.akononov.snakesGame.transfer.msgHandlers.JoinMsgHandler;
import ru.nsu.g.akononov.snakesGame.transfer.msgHandlers.RoleChangeMsgHandler;
import ru.nsu.g.akononov.snakesGame.transfer.msgHandlers.StateMsgHandler;

import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Node implements StateObserver, Controller, JoinMsgHandler, ErrorMsgHandler, InactiveNodesObserver {
    private static final Logger logger = LoggerFactory.getLogger(Node.class.getSimpleName());

    private final static InetSocketAddress announcementAddr = new InetSocketAddress("239.192.0.4", 9192);
    private MulticastSocket multicastSocket;
    private DatagramSocket socket;

    NodeStateType nodeStateType = NodeStateType.FINDING;

    private boolean isConfigured = false;
    SnakesProto.GameConfig config;

    private int currentPlayerID;
    private int serverPlayerID;

    private final MessageSender transfer;
    private Server server = null;

    private StateMsgHandler stateMsgHandler;
    private final ReceiverTracker receiverTracker;
    private final SenderTracker senderTracker;

    private final MsgReceiver manager;
    private Announcement announcement;
    private final Acknowledgement acknowledgement;
    private final Replacement replacement;

    public Node() throws SocketException {
        ConcurrentHashMap<InetSocketAddress, Date> lastReceivedMsgTime = new ConcurrentHashMap<>();
        Date lastSendMsgDate = new Date();

        socket = new DatagramSocket();

        transfer = new MessageSender(socket, announcementAddr, lastSendMsgDate);
        acknowledgement = new Acknowledgement(this, transfer);
        transfer.setAcknowledgement(acknowledgement);

        manager = new MsgReceiver(nodeStateType, transfer, lastReceivedMsgTime);
        receiverTracker = new ReceiverTracker(lastReceivedMsgTime);
        receiverTracker.subscribe(this);

        senderTracker = new SenderTracker(lastSendMsgDate, transfer);

        try {
            multicastSocket = new MulticastSocket(announcementAddr.getPort());
            announcement = new Announcement(announcementAddr, transfer, multicastSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }

        UdpMsgReceiver receiver = new UdpMsgReceiver(socket);
        receiver.subscribe(manager);
        manager.registerMsgHandler(acknowledgement);

        replacement = new Replacement(transfer, this, server);
        manager.registerMsgHandler((RoleChangeMsgHandler) replacement);
        manager.registerMsgHandler((StateMsgHandler) replacement);
    }

    @Override
    public void createNewGame(String Username, int height, int width, float foodPerPlayer, int foodStatic,
                              float deadFoodProb, int stateDelayMs, int pingDelay, int nodeTimout) {

        this.nodeStateType = NodeStateType.CREATOR;
        manager.setStateType(nodeStateType);
        setServerAddress(null);

        server = new Server(SnakesProto.GameConfig.newBuilder()
                .setHeight(height)
                .setWidth(width)
                .setFoodPerPlayer(foodPerPlayer)
                .setFoodStatic(foodStatic)
                .setDeadFoodProb(deadFoodProb)
                .setStateDelayMs(stateDelayMs)
                .setPingDelayMs(pingDelay)
                .setNodeTimeoutMs(nodeTimout)
                .build());

        setConfig(server.getConfig());

        manager.registerMsgHandler(server);
        manager.registerMsgHandler((JoinMsgHandler) this);

        server.registerObserver(this);
        announcement.setConfig(server.getConfig());
        announcement.setPlayers(server.getPlayers().build());

        currentPlayerID = server.addPlayer(Username, "", 0, SnakesProto.PlayerType.HUMAN, SnakesProto.NodeRole.MASTER);
        stateMsgHandler.setPlayerID(currentPlayerID);

        replacement.setNodeRole(SnakesProto.NodeRole.MASTER);
        replacement.setServer(server);

        logger.info("Create new game");
    }

    public void replaceGame(SnakesProto.GameState initState){
        nodeStateType = NodeStateType.CREATOR;
        manager.setStateType(nodeStateType);
        setServerAddress(null);

        server = new Server(initState);
        setConfig(config);

        manager.registerMsgHandler(server);
        manager.registerMsgHandler((JoinMsgHandler) this);

        server.registerObserver(this);
        announcement.setConfig(server.getConfig());
        announcement.setPlayers(server.getPlayers().build());

        replacement.setServer(server);
        transfer.clearDestination();

        logger.info("Replace existing game");
    }

    public void addTransferDestination(List<InetSocketAddress> nodesAddresses){
        for (InetSocketAddress address : nodesAddresses){
            transfer.addPlayer(address);
        }
    }

    @Override
    public void tryToJoin(String host, String currentName) {
        if (host.equals("")) {
            return;
        }
        String[] socketAddr = host.split(":");
        String hostname = socketAddr[0].substring(1);


        InetSocketAddress serverAddress = new InetSocketAddress(hostname, Integer.parseInt(socketAddr[1]));
        logger.debug("Send join-request to SERVER ({})", serverAddress);

        SnakesProto.GameMessage msg = transfer.sendJoinMsg(serverAddress, currentName);
        acknowledgement.waitJoinAck(msg);
    }

    public void joinToServer(int playerID, int serverID, InetSocketAddress serverAddress) {
        this.nodeStateType = NodeStateType.JOINED;
        manager.setStateType(nodeStateType);
        setServerAddress(serverAddress);

        this.currentPlayerID = playerID;
        this.serverPlayerID = serverID;
        stateMsgHandler.setPlayerID(currentPlayerID);

        transfer.addPlayer(serverAddress);
        replacement.setNodeRole(SnakesProto.NodeRole.NORMAL);

        logger.info("Join to SERVER ({})", serverAddress);
    }

    public void setServerAddress(InetSocketAddress serverAddress) {
        transfer.setServerAddress(serverAddress);
        manager.setServerAddress(serverAddress);
        replacement.setMasterAddress(serverAddress);
    }

    public void changeServerAddress(InetSocketAddress serverAddress){
        setServerAddress(serverAddress);
        transfer.clearDestination();
        transfer.addPlayer(serverAddress);
    }

    public int getCurrentPlayerID() {
        return currentPlayerID;
    }

    public int getServerPlayerID() {
        return serverPlayerID;
    }

    @Override
    public void exitGame() {
        if (nodeStateType == NodeStateType.FINDING) {
            return;
        }

        if (nodeStateType == NodeStateType.CREATOR) {
            if (server != null) {
                server.stop();
                server = null;
            }
            manager.removeHandler((JoinMsgHandler) this);
            manager.removeHandler(server);

            announcement.setConfig(null);
            announcement.setPlayers(null);

            logger.debug("Finish current Game");
        }

        replacement.sendExit();
        if (nodeStateType == NodeStateType.JOINED) {
            logger.debug("Disconnect from Game");
        }


        transfer.clearDestination();
        stateMsgHandler.gameEnd();
        isConfigured = false;

        nodeStateType = NodeStateType.FINDING;
        manager.setStateType(nodeStateType);
        setServerAddress(null);

        logger.info("Exit room");
    }

    public void addPresenter(StateMsgHandler stateMsgHandler, AnnouncementMsgHandler announcementMsgHandler) {
        this.stateMsgHandler = stateMsgHandler;
        announcement.registerMsgHandler(announcementMsgHandler);
        manager.registerMsgHandler(stateMsgHandler);
    }


    public void setConfig(SnakesProto.GameConfig config) {
        if (!isConfigured) {
            this.config = config;
            acknowledgement.setWaitingAckTimeout(config.getPingDelayMs());
            receiverTracker.setNodeTimeout(config.getNodeTimeoutMs());
            senderTracker.setPingTimout(config.getPingDelayMs());
            isConfigured = true;
        }
    }

    @Override
    public void shutdown() {
        logger.info("Shutdown node");
        multicastSocket.close();
        socket.close();
    }

    @Override
    public void updateState(SnakesProto.GameState state) {
        transfer.sendStateMsg(state);
        stateMsgHandler.handle(SnakesProto.GameMessage.StateMsg.newBuilder().setState(state).build());
    }

    @Override
    public void moveSnakeLeft() {
        if (nodeStateType == NodeStateType.CREATOR) {
            server.setHeadDirection(SnakesProto.Direction.LEFT, currentPlayerID);
        }
        if (nodeStateType == NodeStateType.JOINED) {
            transfer.sendSteerMsg(SnakesProto.Direction.LEFT);
        }
    }

    @Override
    public void moveSnakeRight() {
        if (nodeStateType == NodeStateType.CREATOR) {
            server.setHeadDirection(SnakesProto.Direction.RIGHT, currentPlayerID);
        }
        if (nodeStateType == NodeStateType.JOINED) {
            transfer.sendSteerMsg(SnakesProto.Direction.RIGHT);
        }
    }

    @Override
    public void moveSnakeUp() {
        if (nodeStateType == NodeStateType.CREATOR) {
            server.setHeadDirection(SnakesProto.Direction.UP, currentPlayerID);
        }
        if (nodeStateType == NodeStateType.JOINED) {
            transfer.sendSteerMsg(SnakesProto.Direction.UP);
        }
    }

    @Override
    public void moveSnakeDown() {
        if (nodeStateType == NodeStateType.CREATOR) {
            server.setHeadDirection(SnakesProto.Direction.DOWN, currentPlayerID);
        }
        if (nodeStateType == NodeStateType.JOINED) {
            transfer.sendSteerMsg(SnakesProto.Direction.DOWN);
        }
    }

    @Override
    public void handle(SnakesProto.GameMessage newMessage, SocketAddress addr) {
        if (!newMessage.hasJoin() || nodeStateType != NodeStateType.CREATOR) {
            return;
        }
        SnakesProto.GameMessage.JoinMsg joinMsg = newMessage.getJoin();

        InetSocketAddress address = (InetSocketAddress) addr;

        if (!joinMsg.getOnlyView()) {
            try {
                logger.debug("Add to game new player {}", address);

                int newPlayerIndex = server.addPlayer(
                        joinMsg.getName(),
                        address.getHostName(),
                        address.getPort(),
                        joinMsg.getPlayerType(),
                        SnakesProto.NodeRole.NORMAL);

                transfer.addPlayer(address);
                transfer.sendJoinAckMsg(newMessage.getMsgSeq(), newPlayerIndex, currentPlayerID, address);
                replacement.addNode(address);

            } catch (RuntimeException exception) {
                exception.printStackTrace();
                transfer.sendErrorMsg(exception.getMessage(), address);
            }
        }
    }

    public void removePlayer(InetSocketAddress address){
        if (nodeStateType == NodeStateType.CREATOR) {
            server.setZombie(address);
            transfer.removePlayer(address);
        }
    }

    @Override
    public void handleInactiveNode(InetSocketAddress nodeAddress) {
        removePlayer(nodeAddress);
        replacement.removeNode(nodeAddress);
        acknowledgement.deleteMsgToBeConfirmed(nodeAddress);
    }

    @Override
    public void handle(SnakesProto.GameMessage.ErrorMsg newMessage) {
        System.err.println("Cannot join to server");
    }
}