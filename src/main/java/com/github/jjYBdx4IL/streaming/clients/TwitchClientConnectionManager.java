package com.github.jjYBdx4IL.streaming.clients;

import com.github.jjYBdx4IL.streaming.clients.twitch.TwitchIRCClient;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jjYBdx4IL
 */
public class TwitchClientConnectionManager extends ConnectionManager {

    private static final Logger LOG = LoggerFactory.getLogger(TwitchClientConnectionManager.class);
    private TwitchIRCClient client = null;
    
    @Override
    public void reconnect() {
        LOG.info("(re)connect");
        
        if (client != null) {
            client.shutdown();
            client = null;
        }
        
        try {
            TwitchConfig config = (TwitchConfig) GenericConfig.readConfig("twitch.xml", TwitchConfig.class);
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
            
        } catch (IOException | InterruptedException ex) {
            LOG.error("", ex);
        }
    }

    @Override
    public boolean isConnected() {
        return client != null && client.isConnected();
    }
    
}
