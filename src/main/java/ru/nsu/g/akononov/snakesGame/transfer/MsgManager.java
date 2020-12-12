package ru.nsu.g.akononov.snakesGame.transfer;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;

import me.ippolitov.fit.snakes.SnakesProto;
import ru.nsu.g.akononov.snakesGame.transfer.msgHandlers.*;

import static me.ippolitov.fit.snakes.SnakesProto.GameMessage.*;

public interface MsgManager {

    ArrayList<AckMsgHandler> ackMsgHandlers = new ArrayList<>();
    ArrayList<ErrorMsgHandler> errorMsgHandlers = new ArrayList<>();
    ArrayList<JoinMsgHandler> joinMsgHandlers = new ArrayList<>();
    ArrayList<PingMsgHandler> pingMsgHandlers = new ArrayList<>();
    ArrayList<RoleChangeMsgHandler> roleChangeMsgHandlers = new ArrayList<>();
    ArrayList<StateMsgHandler> stateMsgHandlers = new ArrayList<>();
    ArrayList<SteerMsgHandler> steerMsgHandlers = new ArrayList<>();


    default void registerMsgHandler(AckMsgHandler handler){
        if (handler == null) {
            throw new NullPointerException();
        }
        if (ackMsgHandlers.contains(handler)) {
            return;
        }
        ackMsgHandlers.add(handler);
    }

    default void removeHandler(AckMsgHandler handler){
        ackMsgHandlers.remove(handler);
    }

    default void registerMsgHandler(ErrorMsgHandler handler){
        if (handler == null) {
            throw new NullPointerException();
        }
        if (errorMsgHandlers.contains(handler)) {
            return;
        }
        errorMsgHandlers.add(handler);
    }

    default void removeHandler(ErrorMsgHandler handler){
        errorMsgHandlers.remove(handler);
    }

    default void notifyHandlers(ErrorMsg newMessage){
        for (ErrorMsgHandler handler : errorMsgHandlers) {
            handler.handle(newMessage);
        }
    }

    default void registerMsgHandler(JoinMsgHandler handler){
        if (handler == null) {
            throw new NullPointerException();
        }
        if (joinMsgHandlers.contains(handler)) {
            return;
        }
        joinMsgHandlers.add(handler);
    }

    default void removeHandler(JoinMsgHandler handler){
        joinMsgHandlers.remove(handler);
    }

    default void notifyHandlers(SnakesProto.GameMessage newMessage, SocketAddress address){
        InetSocketAddress socketAddress = (InetSocketAddress) address;

        SnakesProto.GameMessage.TypeCase msgType = newMessage.getTypeCase();
        switch (msgType){
            case JOIN:
                for (JoinMsgHandler handler : joinMsgHandlers) {
                    handler.handle(newMessage, socketAddress);
                }
                break;
            case ACK:
                for (AckMsgHandler handler : ackMsgHandlers) {
                    handler.handle(newMessage, socketAddress);
                }
                break;
            case ROLE_CHANGE:
                for (RoleChangeMsgHandler handler : roleChangeMsgHandlers) {
                    handler.handle(newMessage, socketAddress);
                }
                break;
            default:
                throw new RuntimeException();
        }
    }

    default void registerMsgHandler(PingMsgHandler handler){
        if (handler == null) {
            throw new NullPointerException();
        }
        if (pingMsgHandlers.contains(handler)) {
            return;
        }
        pingMsgHandlers.add(handler);
    }

    default void removeHandler(PingMsgHandler handler){
        pingMsgHandlers.remove(handler);
    }

    default void notifyHandlers(PingMsg newMessage){
        for (PingMsgHandler handler : pingMsgHandlers) {
            handler.handle(newMessage);
        }
    }

    default void registerMsgHandler(RoleChangeMsgHandler handler){
        if (handler == null) {
            throw new NullPointerException();
        }
        if (roleChangeMsgHandlers.contains(handler)) {
            return;
        }
        roleChangeMsgHandlers.add(handler);
    }

    default void removeHandler(RoleChangeMsgHandler handler){
        roleChangeMsgHandlers.remove(handler);
    }


    default void registerMsgHandler(StateMsgHandler handler){
        if (handler == null) {
            throw new NullPointerException();
        }
        if (stateMsgHandlers.contains(handler)) {
            return;
        }
        stateMsgHandlers.add(handler);
    }

    default void removeHandler(StateMsgHandler handler){
        stateMsgHandlers.remove(handler);
    }

    default void notifyHandlers(StateMsg newMessage){
        for (StateMsgHandler handler : stateMsgHandlers) {
            handler.handle(newMessage);
        }
    }

    default void registerMsgHandler(SteerMsgHandler handler){
        if (handler == null) {
            throw new NullPointerException();
        }
        if (steerMsgHandlers.contains(handler)) {
            return;
        }
        steerMsgHandlers.add(handler);
    }

    default void removeHandler(SteerMsgHandler handler){
        steerMsgHandlers.remove(handler);
    }

    default void notifyHandlers(SteerMsg newMessage, SocketAddress address){
        for (SteerMsgHandler handler : steerMsgHandlers) {
            handler.handle(newMessage, address);
        }
    }
}