package com.github.jjYBdx4IL.streaming.clients.twitch;

/**
 *
 * @author mark
 */
public interface IRCCommandListener {
    
    void onCommandReceived(String command, String args);
}
