package ru.nsu.g.akononov.snakesGame.server.playground;

import java.util.List;
import java.util.Map;

import static me.ippolitov.fit.snakes.SnakesProto.Direction;
import static me.ippolitov.fit.snakes.SnakesProto.GameState;

public class SnakeEditor {

    private final PointEditor pointEditor;

    private final int height;
    private final int width;

    public SnakeEditor(int height, int width) {
        this.height = height;
        this.width = width;

        pointEditor = new PointEditor(height, width);
    }

    public void updateHeadPosition(GameState.Snake.Builder snake, Map<Integer, Direction> nextHeadDirection) {
        int playerID = snake.getPlayerId();

        Direction nextDirection = nextHeadDirection.get(playerID);
        Direction currentDirection = snake.getHeadDirection();

        if (currentDirection == pointEditor.getReverseDirection(nextDirection)) {
            updateHead(snake, true);
            nextHeadDirection.put(playerID, currentDirection);
            return;
        }

        if (currentDirection == nextDirection) {
            updateHead(snake, true);
            return;
        }

        snake.setHeadDirection(nextDirection);
        updateHead(snake, true);
        createFold(snake);
    }

    private void updateHead(GameState.Snake.Builder snake, boolean forward) {
        GameState.Coord.Builder updatedHead;

        int offset = 1;
        if (!forward) {
            offset = -1;
        }

        updatedHead = pointEditor.getNextCoordinate(snake.getPoints(0), snake.getHeadDirection(), offset, height, width);
        snake.setPoints(0, updatedHead);
    }

    public void deleteHead(GameState.Snake.Builder snake) {
        updateHead(snake, false);
        updateFold(snake, false);
    }

    private void updateFold(GameState.Snake.Builder snake, boolean forward) {
        Direction direction = snake.getHeadDirection();
        GameState.Coord secondPoint = snake.getPoints(1);

        int offset = -1;
        if (!forward) {
            offset = 1;
        }

        GameState.Coord.Builder updatedSecond = pointEditor.getNextCoordinate(secondPoint, direction, offset);
        snake.setPoints(1, updatedSecond);
    }

    public void updateFirstFold(GameState.Snake.Builder snake) {
        updateFold(snake, true);
    }

    public void cutTail(GameState.Snake.Builder snake) {
        GameState.Coord last = snake.getPoints(snake.getPointsCount() - 1);

        Direction tailDirection = pointEditor.getOffsetDirection(last);

        GameState.Coord.Builder updatedLast = pointEditor.getNextCoordinate(last, tailDirection, -1);

        snake.removePoints(snake.getPointsCount() - 1);
        if (updatedLast.getX() != 0 || updatedLast.getY() != 0) {
            snake.addPoints(updatedLast);
        }
    }

    public void addTail(GameState.Snake.Builder snake) {
        GameState.Coord last = snake.getPoints(snake.getPointsCount() - 1);
        Direction tailDirection = pointEditor.getOffsetDirection(last);
        GameState.Coord.Builder updatedLast = pointEditor.getNextCoordinate(last, tailDirection, +1);

        snake.setPoints(snake.getPointsCount() - 1, updatedLast);
    }


    public void createFold(GameState.Snake.Builder snake) {
        GameState.Coord.Builder foldOffset = GameState.Coord.newBuilder().setX(0).setY(0);

        List<GameState.Coord> previousSnake = snake.getPointsList();

        snake.clearPoints();
        snake.addPoints(previousSnake.get(0));
        snake.addPoints(foldOffset);
        for (int i = 1; i < previousSnake.size(); i++) {
            snake.addPoints(previousSnake.get(i));
        }
    }
}