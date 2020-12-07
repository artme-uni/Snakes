package ru.nsu.g.akononov.snakesGame.server;

import me.ippolitov.fit.snakes.SnakesProto;
import ru.nsu.g.akononov.snakesGame.server.playground.PlaygroundEditor;
import ru.nsu.g.akononov.snakesGame.transfer.msgHandlers.SteerMsgHandler;

import static me.ippolitov.fit.snakes.SnakesProto.*;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;

public class Server implements ObservableState, SteerMsgHandler {
    private ArrayList<StateObserver> observers = new ArrayList<>();

    private final GameConfig config;
    private final PlaygroundEditor playgroundEditor;

    private final GamePlayers.Builder players = GamePlayers.newBuilder();

    private Integer stateID = 0;
    private int lastPlayerID = 0;

    private final Timer timer = new Timer();

    private final HashMap<SocketAddress, Integer> playersID = new LinkedHashMap<>();

    public Server(GameConfig gameConfig) {
        this.config = gameConfig;
        playgroundEditor = new PlaygroundEditor(config, players);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateState();
            }
        }, 0, config.getStateDelayMs());
    }

    public GameConfig getConfig() {
        return config;
    }

    public GamePlayers.Builder getPlayers() {
        return players;
    }

    private GameState getState() {
        GameState.Builder stateBuilder = GameState.newBuilder().setConfig(config);

        playgroundEditor.editState(stateBuilder);

        stateBuilder.setPlayers(players);
        stateBuilder.setStateOrder(stateID);

        return stateBuilder.build();
    }

    private void updateState() {
        synchronized (Server.class) {
            stateID++;

            playgroundEditor.updateState();

            sendNewState(getState());
        }
    }

    public void setHeadDirection(SnakesProto.Direction direction, int playerID) {
        playgroundEditor.setHeadDirection(direction, playerID);
    }

    public int addPlayer(String name, String ip, int port, PlayerType type, NodeRole role) {
        GamePlayer.Builder newPlayer = GamePlayer.newBuilder();

        newPlayer.setId(++lastPlayerID)
                .setRole(role)
                .setType(type)
                .setName(name)
                .setIpAddress(ip)
                .setPort(port)
                .setScore(0);

        playersID.put(new InetSocketAddress(ip, port), lastPlayerID);

        players.addPlayers(newPlayer);

        try {
            playgroundEditor.addSnake(lastPlayerID);
        }catch (RuntimeException runtimeException){
            runtimeException.printStackTrace();
        }

        return lastPlayerID;
    }

    public void setZombie(InetSocketAddress address){
        int playerID = playersID.get(address);

        synchronized (players) {
            int playerForDeleteIndex = -1;
            for (int i = 0; i < players.getPlayersCount(); i++) {
                if (players.getPlayers(i).getId() == playerID) {
                    playerForDeleteIndex = i;
                }
            }
            if (playerForDeleteIndex != -1) {
                players.removePlayers(playerForDeleteIndex);
            }
        }

        playgroundEditor.setSnakeZombie(playerID);
    }

    public void stop(){
        timer.cancel();
    }

    @Override
    public void registerObserver(StateObserver observer) {
        if (observer == null) {
            throw new NullPointerException();
        }
        if (observers.contains(observer)) {
            throw new IllegalArgumentException("Repeated observer:" + observer);
        }
        observers.add(observer);
    }

    @Override
    public void sendNewState(GameState state) {
        for (StateObserver observer : observers) {
            observer.updateState(state);
        }
    }

    @Override
    public void handle(GameMessage.SteerMsg newMessage, SocketAddress source) {
        Integer playerID = playersID.get(source);
        if(playerID != null){
            setHeadDirection(newMessage.getDirection(), playerID);
        }
    }
}