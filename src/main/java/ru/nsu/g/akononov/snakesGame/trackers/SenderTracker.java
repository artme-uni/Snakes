package ru.nsu.g.akononov.snakesGame.trackers;

import ru.nsu.g.akononov.snakesGame.transfer.MessageSender;

import java.util.Date;

public class SenderTracker {
    private long pingTimout = 200;

    private final Date lastSendMsgTime;
    private final MessageSender sender;

    public SenderTracker(Date lastSendMsgTime, MessageSender sender) {

        this.lastSendMsgTime = lastSendMsgTime;
        this.sender = sender;

        Thread thread = new Thread(()->{
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(pingTimout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                checkPingNotification();
            }
        });

        thread.start();
    }

    public void setPingTimout(long pingTimout) {
        this.pingTimout = pingTimout;
    }

    private void checkPingNotification(){
        synchronized (lastSendMsgTime) {
            long inactivePeriod = new Date().getTime() - lastSendMsgTime.getTime();

            if(inactivePeriod > pingTimout){
                sender.sendPingMsg();
            }
        }
    }
}
