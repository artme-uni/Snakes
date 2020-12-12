package ru.nsu.g.akononov.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;


public class UdpMsgSender implements MsgSender {

    private final DatagramSocket socket;
    private final CopyOnWriteArrayList<InetSocketAddress> destinations = new CopyOnWriteArrayList<>();

    private final LinkedBlockingDeque<byte[]> messagesToBeSent = new LinkedBlockingDeque<>();
    private final ConcurrentHashMap<byte[], SocketAddress> privateMessageDestinations = new ConcurrentHashMap<>();

    public UdpMsgSender(DatagramSocket socket) {
        this.socket = socket;

        Thread workingThread = new Thread(this::send);
        workingThread.start();
    }

    private void send() {
        while (!Thread.currentThread().isInterrupted()) {
            byte[] message = new byte[0];
            try {
                message = messagesToBeSent.take();
            } catch (InterruptedException ignored) {}

            SocketAddress destination = privateMessageDestinations.get(message);

            if (destination == null) {
                sendBroadcastMessage(message);
            } else {
                privateMessageDestinations.remove(message);
                sendUnicastMessage(message, destination);
            }
        }
    }

    @Override
    public ArrayList<InetSocketAddress> getDestinations(){
        return new ArrayList<>(destinations);
    }

    private void sendBroadcastMessage(byte[] message){
        for (SocketAddress destination : destinations) {
            sendUnicastMessage(message, destination);
        }
    }

    private void sendUnicastMessage(byte[] message, SocketAddress destination) {
        if(destination.toString().equals("localhost/127.0.0.1:0")){
            return;
        }
        try {
            DatagramPacket packet = new DatagramPacket(message, message.length, destination);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void addDestination(SocketAddress destination) {
        destinations.add((InetSocketAddress) destination);
    }

    @Override
    public void removeDestination(SocketAddress destination) {
        destinations.remove(destination);
    }

    @Override
    public void clearDestinations() {
        destinations.clear();
    }

    @Override
    public void sendBroadcast(byte[] message) {
        messagesToBeSent.addLast(message);
    }

    @Override
    public void sendUnicast(byte[] message, SocketAddress unicastDestination) {
        messagesToBeSent.addLast(message);
        privateMessageDestinations.put(message, unicastDestination);
    }
}
