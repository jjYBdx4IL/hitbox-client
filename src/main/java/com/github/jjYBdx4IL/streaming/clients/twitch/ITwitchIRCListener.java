package com.github.jjYBdx4IL.streaming.clients.twitch;

/**
 *
 * @author jjYBdx4IL
 */
public interface ITwitchIRCListener {

	void onStatusLineReceived(String from, int code, String to, String args);
	void onCommandReceived(String command, String args);
	void onChannelMessageReceived(String from, String channel, String message);
}
