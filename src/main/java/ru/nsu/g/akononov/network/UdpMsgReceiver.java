package ru.nsu.g.akononov.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

public class UdpMsgReceiver implements MsgReceiver {
    private final DatagramSocket socket;
    private final static int MAX_MESSAGE_SIZE = 2048;

    public UdpMsgReceiver(DatagramSocket socket) {
        this.socket = socket;

        Thread workingThread = new Thread(this::receive);
        workingThread.start();
    }

    private void receive(){
        byte[] bytes = new byte[MAX_MESSAGE_SIZE];
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length);

        while (!Thread.currentThread().isInterrupted()) {
            try {
                socket.receive(packet);
                processMessage(packet);
            } catch (SocketTimeoutException ignored){}
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processMessage(DatagramPacket packet){
        byte[] data = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
        notifySubscribers(data, packet.getSocketAddress());
    }
}
