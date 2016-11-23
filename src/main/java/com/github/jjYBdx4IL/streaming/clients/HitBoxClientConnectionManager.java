package com.github.jjYBdx4IL.streaming.clients;

import com.github.jjYBdx4IL.streaming.clients.hitbox.HitBoxClient;
import com.github.jjYBdx4IL.streaming.clients.hitbox.HitBoxClientFactory;

import java.io.IOException;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jjYBdx4IL
 */
public class HitBoxClientConnectionManager extends ConnectionManager {

    private static final Logger LOG = LoggerFactory.getLogger(HitBoxClientConnectionManager.class);
    private HitBoxClient client = null;
    
    @Override
    public void reconnect() {
        LOG.info("(re)connect");
        
        if (client != null) {
            client.shutdown();
            client = null;
        }
        
        try {
            HitBoxConfig config = (HitBoxConfig) GenericConfig.readConfig("hitbox.xml", HitBoxConfig.class);
            client = HitBoxClientFactory.create(config.botname, config.password);
            for (ChatListener listener : getChatListeners()) {
                client.addChatListener(listener);
            }
            for (FollowerListener listener : getFollowerListeners()) {
                client.addFollowerListener(listener);
            }
            if (client.connectBlocking()) {
                LOG.info("connected.");
                client.joinChannel(config.botname, config.password, config.channel);
            } else {
                LOG.error("connect failed");
                throw new IOException("connect failed");
            }
        } catch (IOException | IllegalAccessException | InstantiationException | InterruptedException | URISyntaxException ex) {
            LOG.error("", ex);
        }
    }

    @Override
    public boolean isConnected() {
        return client != null && client.isConnected();
    }
    
}
