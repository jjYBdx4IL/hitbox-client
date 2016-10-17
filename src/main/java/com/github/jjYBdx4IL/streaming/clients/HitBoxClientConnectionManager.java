package com.github.jjYBdx4IL.streaming.clients;

import com.github.jjYBdx4IL.streaming.clients.hitbox.HitBoxClient;
import com.github.jjYBdx4IL.streaming.clients.hitbox.HitBoxClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mark
 */
public class HitBoxClientConnectionManager extends ConnectionManager {

    private static final Logger log = LoggerFactory.getLogger(HitBoxClientConnectionManager.class);
    private HitBoxClient client = null;
    
    @Override
    public void reconnect() {
        log.info("(re)connect");
        
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
            client.connectBlocking();
            client.joinChannel(config.botname, config.password, config.channel);
        } catch (Exception ex) {
            log.error("", ex);
        }
    }

    @Override
    public boolean isConnected() {
        return client != null && client.isConnected();
    }
    
}
