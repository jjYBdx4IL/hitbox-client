package com.github.jjYBdx4IL.streaming.clients.twitch;

/**
 *
 * @author mark
 */
public interface IRCStatusListener {
    
    void onStatusLineReceived(String from, int code, String to, String args);
}
