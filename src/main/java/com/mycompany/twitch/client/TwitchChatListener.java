package com.mycompany.twitch.client;

/**
 *
 * @author mark
 */
public interface TwitchChatListener {
    
    void onChatMessage(String from, String message);
}
