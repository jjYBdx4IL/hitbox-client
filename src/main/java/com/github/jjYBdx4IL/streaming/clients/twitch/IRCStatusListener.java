package com.github.jjYBdx4IL.streaming.clients.twitch;

/**
 *
 * @author jjYBdx4IL
 */
public interface IRCStatusListener {
    
    void onStatusLineReceived(String from, int code, String to, String args);
}
