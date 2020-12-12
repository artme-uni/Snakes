package ru.nsu.g.akononov.snakesGame.announcement;

import ru.nsu.g.akononov.network.MsgReceiver;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;

public class AnnouncementRadar implements Runnable, MsgReceiver{
    private final static int MAX_MESSAGE_SIZE = 2048;

    private final MulticastSocket socket;

    public AnnouncementRadar(InetSocketAddress socketAddress, MulticastSocket socket) throws IOException {
        this.socket = socket;

        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netIf : Collections.list(nets)) {
            if(netIf.isUp() && !netIf.isLoopback() && netIf.supportsMulticast()) {
                try {
                    socket.joinGroup(socketAddress, netIf);
                } catch (IOException ignored){ }
            }
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                byte[] buf = new byte[MAX_MESSAGE_SIZE];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);

                socket.receive(packet);
                byte[] result = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());

                notifySubscribers(result, packet.getSocketAddress());

            } catch (SocketTimeoutException ignored){}
            catch (IOException e) {
                if(!e.getMessage().equals("Socket closed")){
                    e.printStackTrace();
                }
            }
        }
    }
}