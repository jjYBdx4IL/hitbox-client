package com.github.jjYBdx4IL.streaming.clients;

/**
 *
 * @author mark
 */
public interface ChatListenerHandler {
    
    void addChatListener(ChatListener listener);
    void removeChatListener(ChatListener listener);
}
