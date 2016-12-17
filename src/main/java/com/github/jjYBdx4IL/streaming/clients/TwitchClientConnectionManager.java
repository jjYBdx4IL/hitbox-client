package com.github.jjYBdx4IL.streaming.clients;

import com.github.jjYBdx4IL.streaming.clients.twitch.TwitchIRCClient;
import com.github.jjYBdx4IL.streaming.clients.twitch.api.Channel;
import com.github.jjYBdx4IL.streaming.clients.twitch.api.TwitchRESTClient;

import java.io.IOException;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jjYBdx4IL
 */
public class TwitchClientConnectionManager extends ConnectionManager {

    private static final Logger LOG = LoggerFactory.getLogger(TwitchClientConnectionManager.class);
    private TwitchIRCClient client = null;
    private boolean gameUpdated = false;

    public TwitchClientConnectionManager(GenericConfig config) {
        super(config);
    }

    @Override
    public void reconnect() {
        LOG.info("(re)connect");
        notifyReconnect();
        
        if (client != null) {
            client.shutdown();
            client = null;
        }
        
        try {
            TwitchConfig config = new TwitchConfig();
            config.read();
            client = new TwitchIRCClient(config.botname, config.oauthToken);
            client.connect();
            client.joinChannel(config.channel, new TwitchChatListener() {
                @Override
                public void onChatMessage(String from, String message) {
                    LOG.info(from + ": " + message);
                    for (ChatListener listener : getChatListeners()) {
                        listener.onChatMessage(from, message);
                    }
                }
            });
            notifyConnected();
            updateTwitchGame();
        } catch (IOException | InterruptedException ex) {
            LOG.error("", ex);
        }
    }

    @Override
    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    /**
     * update twitch game title depending on stream title
     */
    private void updateTwitchGame() throws IOException {
        if (gameUpdated) {
            return;
        }
        gameUpdated = true;
        
        TwitchRESTClient client = new TwitchRESTClient();
        Channel channel = client.getChannelStatus();
        for (String game : genericConfig.games) {
            if (channel.status.toLowerCase(Locale.ROOT).contains(game.toLowerCase(Locale.ROOT))) {
                channel.game = game;
                LOG.info("setting Twitch channel info to " + channel);
                client.putChannelStatus(channel);
                break;
            }
        }
    }


}
