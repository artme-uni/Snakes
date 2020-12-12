package ru.nsu.g.akononov.snakesGame.replacement;

import me.ippolitov.fit.snakes.SnakesProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.g.akononov.snakesGame.node.Node;
import ru.nsu.g.akononov.snakesGame.server.Server;
import ru.nsu.g.akononov.snakesGame.transfer.MessageSender;
import ru.nsu.g.akononov.snakesGame.transfer.msgHandlers.RoleChangeMsgHandler;
import ru.nsu.g.akononov.snakesGame.transfer.msgHandlers.StateMsgHandler;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import static me.ippolitov.fit.snakes.SnakesProto.GameMessage;
import static me.ippolitov.fit.snakes.SnakesProto.NodeRole;

public class Replacement implements RoleChangeMsgHandler, StateMsgHandler {
    private static final Logger logger = LoggerFactory.getLogger(Replacement.class.getSimpleName());

    private NodeRole currentRole;

    private InetSocketAddress masterAddress;
    private InetSocketAddress deputyAddress;

    private SnakesProto.GameState lastState;

    private final MessageSender sender;
    private final Node node;
    private Server server;


    private List<InetSocketAddress> neighbors = new ArrayList<>();

    public Replacement(MessageSender sender, Node node, Server server) {
        this.sender = sender;
        this.node = node;
        this.server = server;
    }

    public void setNodeRole(NodeRole role) {
        this.currentRole = role;
        logger.debug("Became {}", role);
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public void setMasterAddress(InetSocketAddress masterAddress) {
        if (masterAddress == null) {
            return;
        }
        this.masterAddress = masterAddress;
    }

    public void addNode(InetSocketAddress address) {
        if (currentRole == NodeRole.MASTER && neighbors.size() == 0 && deputyAddress == null) {
            setNewDeputy(address);
            logger.debug("Choose new DEPUTY ({})", deputyAddress);
        }
        neighbors.add(address);
    }

    public void setNewDeputy(InetSocketAddress address) {
        deputyAddress = address;
        int playerID = -1;
        //int playerID = server.getPlayerID(address);
        sender.sendReceiverRoleMsg(NodeRole.DEPUTY, playerID, address);
        server.setNewDeputy(address);
    }

    public void removeCurrentDeputy(){
        deputyAddress = null;
        logger.debug("Delete current DEPUTY");
    }

    public int getPlayerID(InetSocketAddress serverAddress) {
        for (SnakesProto.GamePlayer player : lastState.getPlayers().getPlayersList()) {
            if (player.getIpAddress().equals(serverAddress.getHostName())
                    && player.getPort() == serverAddress.getPort()) {
                return player.getId();
            }
        }
        return -1;
    }

    public void removeNode(InetSocketAddress address) {
        if (currentRole == null) {
            return;
        }

        if (currentRole.equals(NodeRole.NORMAL) && masterAddress.equals(address)) {
            node.changeServerAddress(deputyAddress);
            logger.debug("Make DEPUTY a new MASTER ({})", deputyAddress);
            deputyAddress = null;
            return;
        }

        if (currentRole == NodeRole.MASTER && deputyAddress.equals(address)) {
            neighbors.remove(address);
            removeCurrentDeputy();

            if (neighbors.size() != 0) {
                setNewDeputy(neighbors.get(0));
                logger.debug("Choose new DEPUTY ({})", deputyAddress);
            }

            return;
        }

        if (currentRole == NodeRole.DEPUTY && masterAddress.toString().equals(address.toString())) {
            neighbors.remove(address);
            setNodeRole(NodeRole.MASTER);
            node.replaceGame(lastState);
            removeCurrentDeputy();

            for (int i = 0; i < lastState.getPlayers().getPlayersCount(); i++) {
                SnakesProto.GamePlayer player = lastState.getPlayers().getPlayers(i);
                if (player.getId() == node.getCurrentPlayerID()
                        || player.getId() == node.getServerPlayerID()
                        ||player.getId() == getPlayerID(masterAddress)) {
                    continue;
                }
                InetSocketAddress socketAddress = new InetSocketAddress(player.getIpAddress(), player.getPort());
                addNode(socketAddress);
            }

            server.setZombie(new InetSocketAddress("", 0));
            server.setZombie(address);

            node.addTransferDestination(neighbors);
            logger.debug("Became a new MASTER instead of {}", address);
            return;
        }

        neighbors.remove(address);
    }

    public void sendExit() {
        if (currentRole == NodeRole.NORMAL || currentRole == NodeRole.DEPUTY) {
            int ownID = node.getCurrentPlayerID();
            sender.sendOwnRoleMsg(NodeRole.VIEWER, ownID, masterAddress);
            currentRole = null;
            logger.debug("Send goodbye to SERVER ({})", masterAddress);
        }

        currentRole = null;
    }

    @Override
    public void handle(GameMessage newMessage, InetSocketAddress source) {
        GameMessage.RoleChangeMsg msg;
        if (newMessage.hasRoleChange()) {
            msg = newMessage.getRoleChange();
        } else {
            return;
        }

        if (msg.hasSenderRole() && msg.getSenderRole() == NodeRole.VIEWER) {
            node.removePlayer(source);
            logger.debug("Receive EXIT from {}", source);
        }

        if (msg.hasReceiverRole()) {
            setNodeRole(msg.getReceiverRole());
        }
    }

    @Override
    public void handle(GameMessage.StateMsg newMessage) {
        lastState = newMessage.getState();
        if (currentRole == null) {
            return;
        }

        if (currentRole == NodeRole.NORMAL) {
            for (int i = 0; i < lastState.getPlayers().getPlayersCount(); i++) {
                SnakesProto.GamePlayer player = lastState.getPlayers().getPlayers(i);
                if (player.getRole() == NodeRole.DEPUTY) {
                    InetSocketAddress newDeputyAddress = new InetSocketAddress(player.getIpAddress(), player.getPort());
                    if (deputyAddress == null || !deputyAddress.toString().equals(newDeputyAddress.toString())) {
                        deputyAddress = new InetSocketAddress(player.getIpAddress(), player.getPort());
                        logger.debug("Update DEPUTY address to {}", newDeputyAddress);
                    }
                }
            }
        }
    }

    @Override
    public void gameEnd() {
    }

    @Override
    public void setPlayerID(int playerID) {
    }
}
