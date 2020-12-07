package ru.nsu.g.akononov.snakesGame.server.playground;

import me.ippolitov.fit.snakes.SnakesProto;

import java.util.ArrayList;
import java.util.Random;

import static me.ippolitov.fit.snakes.SnakesProto.*;

public class PointEditor {
    Random random = new Random();

    private final int height;
    private final int width;

    public PointEditor(int height, int width) {
        this.height = height;
        this.width = width;
    }

    public Direction getReverseDirection(Direction direction) {
        switch (direction) {
            case UP:
                return SnakesProto.Direction.DOWN;
            case RIGHT:
                return SnakesProto.Direction.LEFT;
            case LEFT:
                return SnakesProto.Direction.RIGHT;
            default:
                return SnakesProto.Direction.UP;
        }
    }

    public GameState.Coord.Builder createOffset(Direction direction) {
        GameState.Coord zeroOffset = GameState.Coord.newBuilder().setX(0).setY(0).build();
        GameState.Coord.Builder next = GameState.Coord.newBuilder(zeroOffset);

        Direction reverse = getReverseDirection(direction);

        switch (reverse) {
            case UP:
                next.setY(-1);
                break;
            case DOWN:
                next.setY(1);
                break;
            case LEFT:
                next.setX(-1);
                break;
            case RIGHT:
                next.setX(1);
                break;
        }
        return next;
    }

    public Direction getDirection(GameState.Coord first, GameState.Coord second) {
        if (first.getX() != second.getX() && first.getY() != second.getY()) {
            throw new RuntimeException();
        }

        if (first.getX() == second.getX()) {
            if (first.getY() - second.getY() > 0) {
                return Direction.UP;
            } else {
                return Direction.DOWN;
            }
        } else {
            if (first.getX() - second.getX() > 0) {
                return Direction.LEFT;
            } else {
                return Direction.RIGHT;
            }
        }
    }

    public GameState.Coord.Builder movePoint(GameState.Coord point, int offset) {
        GameState.Coord.Builder pulled = GameState.Coord.newBuilder(point);

        if (point.getY() == 0) {
            int firstX = point.getX();
            pulled.setX(firstX + offset);
        } else {
            int firstY = point.getY();
            pulled.setY(firstY + offset);
        }

        return pulled;
    }

    public GameState.Coord.Builder getNextCoordinate(GameState.Coord current, Direction direction, int offset, int height, int width) {
        GameState.Coord.Builder next = getNextCoordinate(current, direction, offset);

        next.setX(getCoordinateModule(next.getX(), width));
        next.setY(getCoordinateModule(next.getY(), height));

        return next;
    }

    public GameState.Coord.Builder getNextCoordinate(GameState.Coord current, Direction direction, int offset) {
        GameState.Coord.Builder next = GameState.Coord.newBuilder(current);

        switch (direction) {
            case UP:
                next.setY(current.getY() - offset);
                break;
            case DOWN:
                next.setY(current.getY() + offset);
                break;
            case LEFT:
                next.setX(current.getX() - offset);
                break;
            case RIGHT:
                next.setX(current.getX() + offset);
                break;
        }
        return next;
    }

    public Direction getOffsetDirection(GameState.Coord offset) {
        if (offset.getX() > 0) {
            return Direction.RIGHT;
        }

        if (offset.getX() < 0) {
            return Direction.LEFT;
        }

        if (offset.getY() < 0) {
            return Direction.UP;
        }

        if (offset.getY() > 0) {
            return Direction.DOWN;
        }

        return Direction.DOWN;
    }

    public boolean isIntersect(GameState.Coord point, GameState.Snake.Builder snake, int height, int width) {

        GameState.Coord.Builder first = GameState.Coord.newBuilder(snake.getPoints(0));

        for (int i = 1; i < snake.getPointsCount(); i++) {
            GameState.Coord offset = snake.getPoints(i);

            GameState.Coord.Builder second = getNextCoordinate(first.build(), offset);

            boolean isToBeModuled = isToBeModuled(second.build(), height, width);
            if (isBetween(point, first.build(), second.build())) {
                return true;
            }

            if (isToBeModuled) {
                GameState.Coord.Builder reverseOffset = GameState.Coord.newBuilder()
                        .setX(offset.getX() * -1).setY(offset.getY() * -1);

                GameState.Coord.Builder moduledSecond = getCoordinateModule(second.build(), height, width);
                GameState.Coord.Builder notModuledFirst = getNextCoordinate(moduledSecond.build(), reverseOffset.build());

                if (isBetween(point, moduledSecond.build(), notModuledFirst.build())) {
                    return true;
                }

                first = moduledSecond;
            } else {
                first = second;
            }
        }
        return false;
    }

    private GameState.Coord.Builder getNextCoordinate(GameState.Coord current, GameState.Coord offset) {
        return GameState.Coord.newBuilder()
                .setX(current.getX() + offset.getX()).setY(current.getY() + offset.getY());
    }

    private boolean isToBeModuled(GameState.Coord coord, int height, int width) {
        return coord.getX() < 0 || coord.getX() >= width || coord.getY() < 0 || coord.getY() >= height;
    }


    public ArrayList<GameState.Coord> getCoordinates(GameState.Snake snake, int height, int width) {
        ArrayList<GameState.Coord> keyPoints = new ArrayList<>();
        ArrayList<GameState.Coord> snakeCoords = new ArrayList<>();

        GameState.Coord prev = snake.getPoints(0);
        keyPoints.add(prev);

        for (int j = 1; j < snake.getPointsCount(); j++) {
            GameState.Coord offset = snake.getPoints(j);
            GameState.Coord next = GameState.Coord.newBuilder().setX(prev.getX() + offset.getX()).setY(prev.getY() + offset.getY()).build();
            keyPoints.add(next);
            prev = next;
        }

        snakeCoords.add(snake.getPoints(0));

        for (int i = 0; i < keyPoints.size() - 1; i++) {
            snakeCoords.addAll(paintStrip(keyPoints.get(i), snake.getPoints(i + 1), height, width));
        }
        return snakeCoords;
    }

    private ArrayList<GameState.Coord> paintStrip(GameState.Coord start, GameState.Coord offsetCoord, int height, int width) {
        boolean isOffsetX = offsetCoord.getX() != 0;
        boolean isOffsetY = offsetCoord.getY() != 0;

        ArrayList<GameState.Coord> strip = new ArrayList<>();

        if (isOffsetX && isOffsetY) {
            throw new RuntimeException("Illegal offset : " + offsetCoord.getX() + "," + offsetCoord.getY());
        }
        if (!isOffsetX && !isOffsetY) {
            return strip;
        }

        int offset = isOffsetX ? offsetCoord.getX() : offsetCoord.getY();
        int sign = offset > 0 ? 1 : -1;

        for (int i = 1; i <= Math.abs(offset); i++) {
            int x = getCoordinateModule(start.getX() + sign * i * (isOffsetX ? 1 : 0), width);
            int y = getCoordinateModule(start.getY() + sign * i * (isOffsetY ? 1 : 0), height);

            strip.add(GameState.Coord.newBuilder().setX(x).setY(y).build());
        }

        return strip;
    }

    private GameState.Coord.Builder getCoordinateModule(GameState.Coord coordinate, int height, int width) {
        return GameState.Coord.newBuilder()
                .setX(getCoordinateModule(coordinate.getX(), width))
                .setY(getCoordinateModule(coordinate.getY(), height));
    }

    public int getCoordinateModule(int coordinate, int bound) {
        coordinate = coordinate % bound;
        if (coordinate < 0) {
            coordinate = coordinate + bound;
        }

        return coordinate;
    }


    public GameState.Coord getRandomCoordinate(int height, int width) {
        int y = random.nextInt(height);
        int x = random.nextInt(width);

        return GameState.Coord.newBuilder().setY(y).setX(x).build();
    }

    public Direction getRandomDirection() {
        int direction = random.nextInt(4) + 1;
        return Direction.forNumber(direction);
    }

    public boolean isBetween(GameState.Coord point, GameState.Coord firstBound, GameState.Coord secondBound) {
        if (point.getX() != firstBound.getX() && point.getY() != firstBound.getY()) {
            return false;
        }

        if (point.getX() == firstBound.getX()) {
            if (firstBound.getY() <= point.getY() && point.getY() <= secondBound.getY()) {
                return true;
            }
            if (secondBound.getY() <= point.getY() && point.getY() <= firstBound.getY()) {
                return true;
            }
        }

        if (point.getY() == firstBound.getY()) {
            if (firstBound.getX() <= point.getX() && point.getX() <= secondBound.getX()) {
                return true;
            }
            if (secondBound.getX() <= point.getX() && point.getX() <= firstBound.getX()) {
                return true;
            }
        }

        return false;
    }

    public ArrayList<GameState.Coord> getSurrounding(GameState.Coord coord) {
        ArrayList<GameState.Coord> surrounding = new ArrayList<>();
        for (int j = -1; j <= 1; j += 2) {
            for (int i = -1; i <= 1; i++) {
                surrounding.add(GameState.Coord.newBuilder()
                        .setY(coord.getY() + j)
                        .setX(coord.getX() + i)
                        .build());
            }

            surrounding.add(GameState.Coord.newBuilder()
                    .setY(coord.getY())
                    .setX(coord.getX() + j)
                    .build());
        }
        return surrounding;
    }
}