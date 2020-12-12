package ru.nsu.g.akononov.snakesGame.server.playground;

import java.util.*;

import static me.ippolitov.fit.snakes.SnakesProto.*;

public class PlaygroundEditor {
    Random random = new Random();

    int height;
    int width;

    HashMap<Integer, Direction> nextHeadDirection = new LinkedHashMap<>();
    private final GamePlayers.Builder players;

    private final GameConfig config;

    private final List<GameState.Snake.Builder> snakes = new ArrayList<>();
    private final List<GameState.Coord> foods = new ArrayList<>();

    private final List<GameState.Coord> foodForRemove = new ArrayList<>();
    private final List<GameState.Snake.Builder> snakeForRemove = new ArrayList<>();
    private final List<GameState.Coord> foodForAdd = new ArrayList<>();

    private final PointEditor pointEditor;
    private final SnakeEditor snakeEditor;

    public PlaygroundEditor(GameConfig gameConfig, GamePlayers.Builder players) {
        this.config = gameConfig;
        this.players = players;

        height = config.getHeight();
        width = config.getWidth();

        snakeEditor = new SnakeEditor(height, width);
        pointEditor = new PointEditor(height, width);
    }

    public void setPlaygroundElements(List<GameState.Coord> foods, List<GameState.Snake> snakes){
        this.foods.addAll(foods);

        for (GameState.Snake snake : snakes) {
            this.snakes.add(GameState.Snake.newBuilder(snake));
        }
        initNextHeadDirection();
    }

    public void initNextHeadDirection(){
        for (GameState.Snake.Builder snake : snakes) {
            GameState.Coord head = snake.getPoints(0);
            GameState.Coord offset = snake.getPoints(1);

            GameState.Coord.Builder secondCoord = GameState.Coord.newBuilder(head);
            secondCoord.setX(head.getX() + offset.getX()).setY(head.getY() + offset.getY());

            Direction snakeHeadDirection = pointEditor.getDirection(head, secondCoord.build());
            nextHeadDirection.put(snake.getPlayerId(), snakeHeadDirection);
        }
    }

    public void setHeadDirection(Direction direction, int playerID) {
        nextHeadDirection.put(playerID, direction);
    }

    public void addSnake(int playerID) {
        GameState.Snake.Builder snake = getRandomSnake(playerID);
        snakes.add(snake);
    }

    public void setSnakeZombie(int playerID){
        for (GameState.Snake.Builder snake : snakes) {
            if (snake.getPlayerId() == playerID) {
                snake.setState(GameState.Snake.SnakeState.ZOMBIE);
            }
        }
    }

    public void updateState() {
        for (GameState.Snake.Builder snake : snakes) {
            updatedSnakeCoords(snake);
            checkIntersections();
        }

        snakes.removeAll(snakeForRemove);
        checkFood();
        foods.removeAll(foodForRemove);
        foods.addAll(foodForAdd);

        snakeForRemove.clear();
        foodForRemove.clear();
        foodForAdd.clear();
    }

    public void editState(GameState.Builder stateBuilder) {
        for (GameState.Snake.Builder snake : snakes) {
            stateBuilder.addSnakes(snake);
        }

        for (GameState.Coord food : foods) {
            stateBuilder.addFoods(food);
        }
    }

    private GameState.Snake.Builder getRandomSnake(int playerID) {
        Direction headDirection = pointEditor.getRandomDirection();
        nextHeadDirection.put(playerID, headDirection);

        GameState.Coord.Builder start = GameState.Coord.newBuilder(findEmptySquare(5));
        makeSquareCenter(start);

        return GameState.Snake.newBuilder()
                .addPoints(start)
                .addPoints(pointEditor.createOffset(headDirection))
                .setState(GameState.Snake.SnakeState.ALIVE)
                .setHeadDirection(headDirection)
                .setPlayerId(playerID);
    }

    private void makeSquareCenter(GameState.Coord.Builder start){
        start.setX(pointEditor.getCoordinateModule(start.getX() + 2, width))
                .setY(pointEditor.getCoordinateModule(start.getY() + 2, height));
    }

    private GameState.Coord findEmptySquare(int size) {
        GameState.Coord start = getNotSnakeCoord();
        int iterationCount = 1;

        while (!isEmptySquare(start, size)) {
            if (iterationCount++ == 1000) {
                throw new RuntimeException("Can't find empty square " + size + "x" + size + "to add new snake");
            }

            start = getNotSnakeCoord();
        }

        return start;
    }

    private boolean isEmptySquare(GameState.Coord coord, int size) {
        GameState.Coord.Builder builder = GameState.Coord.newBuilder();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {

                if (isSnake(builder
                        .setX(pointEditor.getCoordinateModule(coord.getX() + i, width))
                        .setY(pointEditor.getCoordinateModule(coord.getY() + j, height))
                        .build())) {
                    return false;
                }
            }
        }

        return true;
    }

    private Integer getPlayerIndex(int playerID) {
        Integer index = null;
        synchronized (players) {
            for (int i = 0; i < players.getPlayersCount(); i++) {
                if (players.getPlayers(i).getId() == playerID) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    private void addPointToPlayer(GameState.Snake snake){
        Integer index = getPlayerIndex(snake.getPlayerId());
        if(index != null) {
            synchronized (players) {
                GamePlayer.Builder player = players.getPlayers(index).toBuilder();
                player.setScore(player.getScore() + 1);
                players.setPlayers(index, player);
            }
        }
    }

    private void updatedSnakeCoords(GameState.Snake.Builder snake) {
        snakeEditor.updateHeadPosition(snake, nextHeadDirection);
        boolean isGrow = isGrow(snake.build());
        if (isGrow) {
            addPointToPlayer(snake.build());
        }

        if (snake.getPointsCount() == 2) {
            if (isGrow) {
                snakeEditor.addTail(snake);
            }
            return;
        }

        snakeEditor.updateFirstFold(snake);
        if (!isGrow) {
            snakeEditor.cutTail(snake);
        }
    }

    private boolean isIntersect(GameState.Coord coordinate) {
        if(isSnake(coordinate)){
            return true;
        }

        for (GameState.Coord food : foods) {
            if (coordinate.getX() == food.getX() && coordinate.getY() == food.getY()) {
                return true;
            }
        }

        for (GameState.Coord food : foodForAdd) {
            if (coordinate.getX() == food.getX() && coordinate.getY() == food.getY()) {
                return true;
            }
        }

        return false;
    }

    private boolean isSnake(GameState.Coord coordinate) {
        for (GameState.Snake.Builder snake : snakes) {
            if (pointEditor.isIntersect(coordinate, snake, height, width)) {
                return true;
            }
        }

        return false;
    }

    private GameState.Coord getEmptyCoord() {
        GameState.Coord current = pointEditor.getRandomCoordinate(height, width);
        while (isIntersect(current)) {
            current = pointEditor.getRandomCoordinate(height, width);
        }

        return current;
    }

    private GameState.Coord getNotSnakeCoord() {
        GameState.Coord current = pointEditor.getRandomCoordinate(height, width);
        while (isSnake(current)) {
            current = pointEditor.getRandomCoordinate(height, width);
        }

        return current;
    }

    private void checkIntersections() {
        for (GameState.Snake.Builder snake : snakes) {
            for (GameState.Snake.Builder otherSnake : snakes) {
                GameState.Snake.Builder other = GameState.Snake.newBuilder(otherSnake.build());
                if (snake.getPlayerId() == otherSnake.getPlayerId()) {
                    snakeEditor.deleteHead(other);
                }

                if (pointEditor.isIntersect(snake.getPoints(0), other, height, width)) {
                    ArrayList<GameState.Coord> coords = pointEditor.getCoordinates(snake.build(), height, width);
                    for (GameState.Coord coordinate : coords) {
                        if (randomValue(config.getDeadFoodProb())) {
                            foodForAdd.add(coordinate);
                        }
                    }
                    snakeForRemove.add(snake);
                    addPointToPlayer(otherSnake.build());

                    break;
                }
            }
        }
    }

    private boolean isGrow(GameState.Snake snake) {
        GameState.Coord head = snake.getPoints(0);

        for (GameState.Coord food : foods) {
            if (head.getX() == food.getX() && head.getY() == food.getY()) {
                foodForRemove.add(food);
                return true;
            }
        }
        return false;
    }

    private void checkFood() {
        int difference = (int) (foods.size() + foodForAdd.size() - config.getFoodPerPlayer() * snakes.size() - config.getFoodStatic());

        if (difference < 0) {
            for (int i = difference; i != 0; i++) {
                foodForAdd.add(getEmptyCoord());
            }
        }
    }

    private boolean randomValue(float trueChance) {
        double value = random.nextDouble();
        return value < trueChance;
    }
}
