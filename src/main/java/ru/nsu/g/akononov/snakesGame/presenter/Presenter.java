package ru.nsu.g.akononov.snakesGame.presenter;

import ru.nsu.g.akononov.snakesGame.announcement.AnnouncementMsgHandler;
import ru.nsu.g.akononov.snakesGame.transfer.msgHandlers.StateMsgHandler;

import java.net.SocketAddress;
import java.util.List;

import static me.ippolitov.fit.snakes.SnakesProto.*;

public class Presenter implements StateMsgHandler, AnnouncementMsgHandler {
    private final Controller controller;
    private boolean isConfigured = false;
    private int playerID = 1;

    private final GameInfoView gameInfoView;
    private final PlaygroundView playgroundView;
    private final CreationPanelView creationPanelView;
    private final JoiningPanelView joiningPanelView;

    public Presenter(Controller controller,
                     GameInfoView gameInfoView,
                     PlaygroundView playgroundView,
                     CreationPanelView creationPanelView,
                     JoiningPanelView joiningPanelView) {

        this.controller = controller;

        this.gameInfoView = gameInfoView;
        this.playgroundView = playgroundView;
        this.creationPanelView = creationPanelView;
        this.joiningPanelView = joiningPanelView;
    }

    public void handleState(GameState state) {
        if(!isConfigured) {
            setConfig(state);
        }
        controller.setConfig(state.getConfig());
        setPlayground(state);
        setScoreboard(state);
    }

    private void setConfig(GameState state){
        GameConfig config = state.getConfig();
        playgroundView.setPlaygroundSize(config.getWidth(), config.getHeight());

        String[][] configs = new String[8][2];
        configs[0] = getPair("Width", config.getWidth());
        configs[1] = getPair("Height", config.getHeight());
        configs[2] = getPair("Food static", config.getFoodStatic());
        configs[3] = getPair("Food per player", config.getFoodPerPlayer());
        configs[4] = getPair("State delay (ms)", config.getStateDelayMs());
        configs[5] = getPair("Dead food prob", config.getDeadFoodProb());
        configs[6] = getPair("Ping delay (ms)", config.getPingDelayMs());
        configs[7] = getPair("Node timeout (ms)", config.getNodeTimeoutMs());
        gameInfoView.setGameConfigData(configs);

        isConfigured = true;
    }

    private void setScoreboard(GameState state){
        List<GamePlayer> playerList = state.getPlayers().getPlayersList();
        String[][] players = new String[playerList.size()][2];
        int index = 0;
        for (GamePlayer player : playerList){
            players[index++] = new String[]{player.getName(), String.valueOf(player.getScore())};
        }
        gameInfoView.setScoreboardData(players);
    }

    private void setPlayground(GameState state){
        playgroundView.clearPlayground();

        List<GameState.Coord> foodList = state.getFoodsList();
        for (GameState.Coord coordinate: foodList) {
            playgroundView.setFood(coordinate.getX(), coordinate.getY());
        }

        List<GameState.Snake> snakeList = state.getSnakesList();
        for (GameState.Snake snake : snakeList){
            boolean isOwn = snake.getPlayerId() == playerID;
            List<GameState.Coord> pointsList = snake.getPointsList();
            int[][] points = new int[pointsList.size()][2];
            for (int i = 0; i < pointsList.size(); i++) {
                points[i][0] = pointsList.get(i).getX();
                points[i][1] = pointsList.get(i).getY();
            }

            boolean isZombie = false;
            if(snake.getState() == GameState.Snake.SnakeState.ZOMBIE){
                isZombie = true;
            }

            playgroundView.addSnake(isOwn, isZombie, points);
        }
    }

    private String[] getPair(String key, int value){
        return new String[]{key, String.valueOf(value)};
    }

    private String[] getPair(String key, double value){
        return new String[]{key, String.format("%.1f", value)};
    }

    @Override
    public void handle(GameMessage.StateMsg newMessage) {
        if(!newMessage.hasState()){
            throw new RuntimeException("Has not a GameState");
        }

        handleState(newMessage.getState());
    }

    @Override
    public void gameEnd() {
        isConfigured = false;
    }

    @Override
    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }

    @Override
    public void add(SocketAddress address, GameMessage.AnnouncementMsg announcements) {
        joiningPanelView.addGame(getPair(address.toString(), announcements.getPlayers().getPlayersCount()));
    }

    @Override
    public void remove(SocketAddress address) {
        joiningPanelView.removeGame(address.toString());
    }
}