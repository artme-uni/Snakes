package ru.nsu.g.akononov.snakesGame.acknowledgement;

import me.ippolitov.fit.snakes.SnakesProto;

import java.net.InetSocketAddress;
import java.util.Date;

public class MsgMetaData {
    private final SnakesProto.GameMessage message;
    private final InetSocketAddress firstPoint;
    private final Date date;

    public MsgMetaData(SnakesProto.GameMessage message, InetSocketAddress source, Date time) {
        this.message = message;
        this.firstPoint = source;
        this.date = time;
    }

    public SnakesProto.GameMessage getMessage() {
        return message;
    }

    public InetSocketAddress getSecondPoint() {
        return firstPoint;
    }

    public long getTime() {
        return date.getTime();
    }
}
