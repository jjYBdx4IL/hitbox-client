package com.github.jjYBdx4IL.streaming.clients.twitch.api;

/**
 *
 * @author Github jjYBdx4IL Projects
 */
public class Stream extends TwitchDTO {

	public static enum TYPE {
		live,
		playlist,
		/** 'all' means 'both', ie. excluding offline streams */
		all;
	}
	
	public Channel channel;
}
