package ru.nsu.g.akononov.snakes.model;

import me.ippolitov.fit.snakes.SnakesProto.GameState.Snake;

import java.util.ArrayList;
import java.util.List;

import static me.ippolitov.fit.snakes.SnakesProto.*;

public class Server implements Runnable, ObservableState{
    private ArrayList<StateHandler> observers = new ArrayList<>();

    private GameConfig.Builder config;
    private List<Snake.Builder> snakes = new ArrayList<>();

    private Integer stateID = 0;

    public void Server(GameConfig gameConfig) {
        this.config = GameConfig.newBuilder(gameConfig);

        GameState.Coord coord = GameState.Coord.newBuilder()
                .setX((int)(Math.random()*10)%10)
                .setY((int)(Math.random()*10)%10)
                .build();

        int direction = (int)(Math.random()*10)%4;

        addSnake(coord, Direction.forNumber(direction), 1);
    }

    private void updatedSnake(Snake.Builder snake){
        GameState.Coord.Builder nextHead = getNextCoordinate(snake.getPoints(0), snake.getHeadDirection());
        snake.setPoints(0, nextHead);
        cutTail(snake);
    }

    private GameState.Coord.Builder getNextCoordinate(GameState.Coord current, Direction direction){
        GameState.Coord.Builder next = GameState.Coord.newBuilder(current);

        switch (direction){
            case UP:
                next.setY(current.getY() - 1);
                break;
            case DOWN:
                next.setY(current.getY() + 1);
                break;
            case LEFT:
                next.setX(current.getX() - 1);
                break;
            case RIGHT:
                next.setX(current.getX() + 1);
                break;
        }
        return next;
    }

    private void cutTail(Snake.Builder snake){
        int pointsCount = snake.getPointsCount();

        GameState.Coord last = snake.getPoints(pointsCount - 1);
        GameState.Coord penultimate = snake.getPoints(pointsCount - 2);

        Direction tailDirection = getDirection(last, penultimate);
        GameState.Coord nextTailCoordinate = getNextCoordinate(last, tailDirection).build();

        snake.removePoints(pointsCount -1);

        if(nextTailCoordinate.getX() != 0 || nextTailCoordinate.getY() != 0){
            snake.addPoints(nextTailCoordinate);
        }
    }

    private void addSnake(GameState.Coord head, Direction headDirection, int playerID){
        Snake.Builder snake = Snake.newBuilder()
                .addPoints(head)
                .addPoints(getNextCoordinate(head, getReverseDirection(headDirection)))
                .setHeadDirection(headDirection)
                .setPlayerId(playerID);

        snakes.add(snake);
    }

    private Direction getReverseDirection(Direction direction){
        switch (direction){
            case UP:
                return Direction.DOWN;
            case RIGHT:
                return Direction.LEFT;
            case LEFT:
                return Direction.RIGHT;
            default:
                return Direction.UP;
        }
    }

    private Direction getDirection(GameState.Coord first, GameState.Coord second){
        if(first.getX() != second.getX() && first.getY() != second.getY()){
            throw new RuntimeException();
        }

        if(first.getX() == second.getX()){
            if(first.getY() - second.getY() > 0){
                return Direction.UP;
            } else{
                return Direction.DOWN;
            }
        } else {
            if(first.getX() - second.getX() > 0){
                return Direction.LEFT;
            } else{
                return Direction.RIGHT;
            }
        }
    }


    @Override
    public void run() {
        for(Snake.Builder snake : snakes){
            updatedSnake(snake);
        }

        //sendNewState();
    }

    @Override
    public void registerObserver(StateHandler observer) {
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
        for (StateHandler observer : observers) {
            observer.handleState(state);
        }
    }
}