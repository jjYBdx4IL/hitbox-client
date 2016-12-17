package com.github.jjYBdx4IL.streaming.clients.twitch;

/**
 *
 * @author jjYBdx4IL
 */
public abstract class TwitchIRCListenerAdapter implements ITwitchIRCListener {

	public void onStatusLineReceived(String from, int code, String to, String args) {}
	public void onCommandReceived(String command, String args) {}
	public void onChannelMessageReceived(String from, String channel, String message) {}
}
