package com.github.jjYBdx4IL.streaming.clients;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jjYBdx4IL
 */
public abstract class ConnectionManager extends TimerTask implements ChatListenerHandler, FollowerListenerHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionManager.class);

    private final Set<ChatListener> chatListeners;
    private final Set<FollowerListener> followerListeners;

    private final Timer timer = new Timer(getClass().getSimpleName(), true);
    
    public ConnectionManager() {
        this.chatListeners = Collections.synchronizedSet(new HashSet<>());
        this.followerListeners = Collections.synchronizedSet(new HashSet<>());
    }

    public abstract void reconnect();

    public abstract boolean isConnected();

    public void start() {
        timer.schedule(this, 0, 60000L);
    }

    public void run() {
        LOG.debug("checking connection state");
        if (!isConnected()) {
            LOG.info("(re)connecting");
            reconnect();
        }
    }

    protected Set<ChatListener> getChatListeners() {
        return Collections.unmodifiableSet(chatListeners);
    }
    
    protected Set<FollowerListener> getFollowerListeners() {
        return Collections.unmodifiableSet(followerListeners);
    }
    
    @Override
    public void addChatListener(ChatListener listener) {
        chatListeners.add(listener);
    }

    @Override
    public void removeChatListener(ChatListener listener) {
        chatListeners.remove(listener);
    }

    @Override
    public void addFollowerListener(FollowerListener listener) {
        followerListeners.add(listener);
    }

    @Override
    public void removeFollowerListener(FollowerListener listener) {
        followerListeners.remove(listener);
    }
    
}
