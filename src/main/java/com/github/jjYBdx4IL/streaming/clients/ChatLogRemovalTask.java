package com.github.jjYBdx4IL.streaming.clients;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jjYBdx4IL
 */
public class ChatLogRemovalTask extends TimerTask {
    
    private static final Logger log = LoggerFactory.getLogger(ChatLogRemovalTask.class);
    public static final long KEEP_CHATLOG_MILLIS = 30 * 1000L;

    private final File chatLog;
    private final Timer timer;
    
    public ChatLogRemovalTask(Timer timer, File chatLog) {
        this.timer = timer;
        this.chatLog = chatLog;
    }

    @Override
    public void run() {
        if (!chatLog.exists()) {
            timer.schedule(new ChatLogRemovalTask(timer, chatLog), KEEP_CHATLOG_MILLIS);
            return;
        }
        
        long age = System.currentTimeMillis() - chatLog.lastModified();
        long timeTillExpiry = KEEP_CHATLOG_MILLIS - age;
        if (timeTillExpiry <= 0L) {
            chatLog.delete();
            timer.schedule(new ChatLogRemovalTask(timer, chatLog), KEEP_CHATLOG_MILLIS);
            return;
        }
        timer.schedule(new ChatLogRemovalTask(timer, chatLog), timeTillExpiry < KEEP_CHATLOG_MILLIS ? timeTillExpiry : KEEP_CHATLOG_MILLIS);
    }
}
