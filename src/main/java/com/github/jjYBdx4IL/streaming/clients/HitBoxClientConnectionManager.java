package com.github.jjYBdx4IL.streaming.clients;

import com.github.jjYBdx4IL.streaming.clients.hitbox.api.Livestream;
import com.github.jjYBdx4IL.streaming.clients.hitbox.HitBoxClient;
import com.github.jjYBdx4IL.streaming.clients.hitbox.HitBoxClientFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jjYBdx4IL
 */
public class HitBoxClientConnectionManager extends ConnectionManager {

    private static final Logger LOG = LoggerFactory.getLogger(HitBoxClientConnectionManager.class);
    private HitBoxClient client = null;
    private boolean gameUpdated = false;
    
    public HitBoxClientConnectionManager(GenericConfig config) {
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
            HitBoxConfig config = new HitBoxConfig();
            config.read();
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
                notifyConnected();
                updateHitBoxGame();
            } else {
                LOG.error("connect failed");
                throw new IOException("connect failed");
            }
        } catch (IOException | InterruptedException | URISyntaxException ex) {
            LOG.error("", ex);
        }
    }

    @Override
    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    private void updateHitBoxGame() throws IOException {
        if (gameUpdated) {
            return;
        }
        gameUpdated = true;
        
        Livestream channel = client.getChannelInfo();
        for (String game : genericConfig.games) {
            if (channel.media_status.toLowerCase(Locale.ROOT).contains(game.toLowerCase(Locale.ROOT))) {
                channel.media_category_id = client.getCategoryId(game);
                if (channel.media_category_id < 0) {
                    LOG.warn("no category found for " + game);
                    break;
                }
                LOG.info("setting HitBox channel info to " + channel);
                if (!client.updateChannelInfo(channel)) {
                    LOG.error("failed to update game title");
                }
                break;
            }
        }
    }


    
}
