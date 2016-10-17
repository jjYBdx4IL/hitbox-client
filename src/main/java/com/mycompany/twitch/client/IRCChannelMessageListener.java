package com.mycompany.twitch.client;

/**
 *
 * @author mark
 */
public interface IRCChannelMessageListener {

    void onChannelMessageReceived(String from, String channel, String message);
}
