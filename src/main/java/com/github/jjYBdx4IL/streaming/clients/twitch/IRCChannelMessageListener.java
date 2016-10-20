package com.github.jjYBdx4IL.streaming.clients.twitch;

/**
 *
 * @author jjYBdx4IL
 */
public interface IRCChannelMessageListener {

    void onChannelMessageReceived(String from, String channel, String message);
}
