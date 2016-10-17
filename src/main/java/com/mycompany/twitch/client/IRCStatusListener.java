package com.mycompany.twitch.client;

/**
 *
 * @author mark
 */
public interface IRCStatusListener {
    
    void onStatusLineReceived(String from, int code, String to, String args);
}
