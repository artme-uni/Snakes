package ru.nsu.g.akononov.snakes.presenter;

import ru.nsu.g.akononov.snakes.model.StateHandler;
import ru.nsu.g.akononov.snakes.model.Node;
import static me.ippolitov.fit.snakes.SnakesProto.*;

import java.util.List;

public class Presenter implements StateHandler {
    private final Node model;
    private final GameInfoView gameInfoView;
    private final PlaygroundView playgroundView;

    public Presenter(Node model, GameInfoView gameInfoView, PlaygroundView playgroundView) {
        this.model = model;
        this.gameInfoView = gameInfoView;
        this.playgroundView = playgroundView;
    }

    @Override
    public void handleState(GameState state) {
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

        List<GameState.Coord> foodList = state.getFoodsList();
        for (GameState.Coord coordinate: foodList) {
            playgroundView.setFood(coordinate.getX(), coordinate.getY());
        }

        List<GameState.Snake> snakeList = state.getSnakesList();
        for (GameState.Snake snake : snakeList){
            boolean isOwn = snake.getPlayerId() == 1;
            List<GameState.Coord> pointsList = snake.getPointsList();
            int[][] points = new int[pointsList.size()][2];
            for (int i = 0; i < pointsList.size(); i++) {
                points[i][0] = pointsList.get(i).getX();
                points[i][1] = pointsList.get(i).getY();
            }
            playgroundView.addSnake(isOwn, points);
        }

        List<GamePlayer> playerList = state.getPlayers().getPlayersList();
        String[][] players = new String[playerList.size()][2];
        int index = 0;
        for (GamePlayer player : playerList){
            players[index++] = new String[]{player.getName(), String.valueOf(player.getScore())};
        }
        gameInfoView.setScoreboardData(players);
    }

    private String[] getPair(String key, int value){
        return new String[]{key, String.valueOf(value)};
    }

    private String[] getPair(String key, double value){
        return new String[]{key, String.format("%.1f", value)};
    }

}
