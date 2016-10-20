package com.github.jjYBdx4IL.streaming.clients;

/**
 *
 * @author jjYBdx4IL
 */
public interface TwitchChatListener {
    
    void onChatMessage(String from, String message);
}
