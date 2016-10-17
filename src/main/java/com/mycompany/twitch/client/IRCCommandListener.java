package com.mycompany.twitch.client;

/**
 *
 * @author mark
 */
public interface IRCCommandListener {
    
    void onCommandReceived(String command, String args);
}
