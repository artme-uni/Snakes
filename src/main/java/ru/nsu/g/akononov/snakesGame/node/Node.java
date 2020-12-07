package ru.nsu.g.akononov.snakesGame.node;

import me.ippolitov.fit.snakes.SnakesProto;
import ru.nsu.g.akononov.network.UdpMsgReceiver;
import ru.nsu.g.akononov.snakesGame.tracker.InactiveNodesObserver;
import ru.nsu.g.akononov.snakesGame.tracker.ReceiverTracker;
import ru.nsu.g.akononov.snakesGame.tracker.SenderTracker;
import ru.nsu.g.akononov.snakesGame.transfer.MessageSender;
import ru.nsu.g.akononov.snakesGame.acknowledgement.Acknowledgement;
import ru.nsu.g.akononov.snakesGame.announcement.Announcement;
import ru.nsu.g.akononov.snakesGame.presenter.Controller;
import ru.nsu.g.akononov.snakesGame.server.Server;
import ru.nsu.g.akononov.snakesGame.server.StateObserver;
import ru.nsu.g.akononov.snakesGame.transfer.MsgReceiver;
import ru.nsu.g.akononov.snakesGame.announcement.AnnouncementMsgHandler;
import ru.nsu.g.akononov.snakesGame.transfer.msgHandlers.JoinMsgHandler;
import ru.nsu.g.akononov.snakesGame.transfer.msgHandlers.StateMsgHandler;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class Node implements StateObserver, Controller, JoinMsgHandler, InactiveNodesObserver {
    private final static InetSocketAddress announcementAddr = new InetSocketAddress("239.192.0.4", 9192);

    SnakesProto.NodeRole currentRole;

    NodeStateType nodeStateType = NodeStateType.FINDING;

    private boolean isConfigured = false;
    SnakesProto.GameConfig config;

    InetSocketAddress serverAddress = null;

    private int currentPlayerID;

    private final MessageSender transfer;
    private Server server = null;

    private StateMsgHandler stateMsgHandler;
    private final ReceiverTracker receiverTracker;
    private final SenderTracker senderTracker;

    private final MsgReceiver manager;
    private Announcement announcement;
    private final Acknowledgement acknowledgement;

    public Node() throws SocketException {

        ConcurrentHashMap<InetSocketAddress, Date> lastReceivedMsgTime = new ConcurrentHashMap<>();
        Date lastSendMsgDate = new Date();

        DatagramSocket socket = new DatagramSocket();
        transfer = new MessageSender(socket, announcementAddr, lastSendMsgDate);
        acknowledgement = new Acknowledgement(this, transfer);
        transfer.setAcknowledgement(acknowledgement);

        manager = new MsgReceiver(nodeStateType, transfer, lastReceivedMsgTime);
        receiverTracker = new ReceiverTracker(lastReceivedMsgTime);
        receiverTracker.subscribe(this);

        senderTracker = new SenderTracker(lastSendMsgDate, transfer);

        try {
            announcement = new Announcement(announcementAddr, transfer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        UdpMsgReceiver receiver = new UdpMsgReceiver(socket);
        receiver.subscribe(manager);
        manager.registerMsgHandler(acknowledgement);
    }
    @Override
    public void createNewGame(String Username, int height, int width, float foodPerPlayer, int foodStatic,
                              float deadFoodProb, int stateDelayMs, int pingDelay, int nodeTimout) {
        nodeStateType = NodeStateType.CREATOR;
        manager.setStateType(nodeStateType);

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
        manager.registerMsgHandler(this);

        server.registerObserver(this);
        announcement.setConfig(server.getConfig());
        announcement.setPlayers(server.getPlayers().build());

        currentPlayerID = server.addPlayer(Username, "", 0, SnakesProto.PlayerType.HUMAN, SnakesProto.NodeRole.MASTER);
        stateMsgHandler.setPlayerID(currentPlayerID);
    }


    @Override
    public void tryToJoin(String host, String currentName) {
        if(host.equals("")){
            return;
        }

        String[] socketAddr = host.split(":");
        String hostname = socketAddr[0].substring(1);

        SnakesProto.GameMessage msg = transfer.sendJoinMsg(
                new InetSocketAddress(hostname, Integer.parseInt(socketAddr[1])),
                currentName);

        acknowledgement.waitJoinAck(msg);
    }

    public void joinToServer(int playerID, InetSocketAddress serverAddress){
        nodeStateType = NodeStateType.JOINED;
        manager.setStateType(nodeStateType);

        this.currentPlayerID = playerID;
        stateMsgHandler.setPlayerID(currentPlayerID);

        this.serverAddress = serverAddress;
        transfer.addPlayer(serverAddress);
        manager.setServer(serverAddress);
    }

    @Override
    public void exitGame() {
        if(nodeStateType == NodeStateType.FINDING){
            return;
        }

        if(nodeStateType == NodeStateType.CREATOR){
            if(server != null) {
                server.stop();
                server = null;
            }

            manager.removeHandler(this);
            manager.removeHandler(server);

            announcement.setConfig(null);
            announcement.setPlayers(null);
        }

        if(nodeStateType == NodeStateType.JOINED){
            transfer.removePlayer(serverAddress);
            manager.setServer(null);
            serverAddress = null;
        }

        transfer.clearDestination();
        stateMsgHandler.gameEnd();
        isConfigured = false;

        nodeStateType = NodeStateType.FINDING;
        manager.setStateType(nodeStateType);
    }

    public void addPresenter(StateMsgHandler stateMsgHandler, AnnouncementMsgHandler announcementMsgHandler){
        this.stateMsgHandler = stateMsgHandler;
        announcement.registerMsgHandler(announcementMsgHandler);
        manager.registerMsgHandler(stateMsgHandler);
    }


    public void setConfig(SnakesProto.GameConfig config){
        if(!isConfigured) {
            this.config = config;
            acknowledgement.setWaitingAckTimeout(config.getPingDelayMs());
            receiverTracker.setNodeTimeout(config.getNodeTimeoutMs());
            senderTracker.setPingTimout(config.getPingDelayMs());
            isConfigured = true;
        }
    }

    @Override
    public void updateState(SnakesProto.GameState state) {
        transfer.sendStateMsg(state);
        stateMsgHandler.handle(SnakesProto.GameMessage.StateMsg.newBuilder().setState(state).build());
    }

    @Override
    public void moveSnakeLeft() {
        if(nodeStateType == NodeStateType.CREATOR){
            server.setHeadDirection(SnakesProto.Direction.LEFT, currentPlayerID);
        }
        if(nodeStateType == NodeStateType.JOINED){
            transfer.sendSteerMsg(SnakesProto.Direction.LEFT);
        }
    }

    @Override
    public void moveSnakeRight() {
        if(nodeStateType == NodeStateType.CREATOR){
            server.setHeadDirection(SnakesProto.Direction.RIGHT, currentPlayerID);
        }
        if(nodeStateType == NodeStateType.JOINED){
            transfer.sendSteerMsg(SnakesProto.Direction.RIGHT);
        }
    }

    @Override
    public void moveSnakeUp() {
        if(nodeStateType == NodeStateType.CREATOR){
            server.setHeadDirection(SnakesProto.Direction.UP, currentPlayerID);
        }
        if(nodeStateType == NodeStateType.JOINED){
            transfer.sendSteerMsg(SnakesProto.Direction.UP);
        }
    }

    @Override
    public void moveSnakeDown() {
        if(nodeStateType == NodeStateType.CREATOR){
            server.setHeadDirection(SnakesProto.Direction.DOWN, currentPlayerID);
        }
        if(nodeStateType == NodeStateType.JOINED){
            transfer.sendSteerMsg(SnakesProto.Direction.DOWN);
        }
    }

    @Override
    public void handle(SnakesProto.GameMessage newMessage, SocketAddress addr) {
        if(!newMessage.hasJoin()){
            return;
        }
        SnakesProto.GameMessage.JoinMsg joinMsg = newMessage.getJoin();

        InetSocketAddress address = (InetSocketAddress) addr;


        if(!joinMsg.getOnlyView()) {
            int newPlayerIndex = server.addPlayer(
                    joinMsg.getName(),
                    address.getHostName(),
                    address.getPort(),
                    joinMsg.getPlayerType(),
                    SnakesProto.NodeRole.NORMAL);

            transfer.addPlayer(address);
            transfer.sendAckMsg(newMessage.getMsgSeq(), newPlayerIndex);
        }
    }

    @Override
    public void handleInactiveNode(InetSocketAddress nodeAddress) {
        if(nodeStateType == NodeStateType.CREATOR) {
            server.setZombie(nodeAddress);
        }
    }
}