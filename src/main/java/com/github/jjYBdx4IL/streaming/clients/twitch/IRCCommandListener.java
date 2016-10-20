package com.github.jjYBdx4IL.streaming.clients.twitch;

/**
 *
 * @author jjYBdx4IL
 */
public interface IRCCommandListener {
    
    void onCommandReceived(String command, String args);
}
