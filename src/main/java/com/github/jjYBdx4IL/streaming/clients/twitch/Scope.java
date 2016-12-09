package com.github.jjYBdx4IL.streaming.clients.twitch;

import java.util.Locale;

/**
 *
 * @author jjYBdx4IL
 */
public enum Scope {

    USER_READ,
    USER_BLOCKS_EDIT,
    USER_BLOCKS_READ,
    USER_FOLLOWS_EDIT,
    CHANNEL_READ,
    CHANNEL_EDITOR,
    CHANNEL_COMMERCIAL,
    CHANNEL_STREAM,
    CHANNEL_SUBSCRIPTIONS,
    USER_SUBSCRIPTIONS,
    CHANNEL_CHECK_SUBSCRIPTION,
    CHAT_LOGIN,
    CHANNEL_FEED_READ,
    CHANNEL_FEED_EDIT;

    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
