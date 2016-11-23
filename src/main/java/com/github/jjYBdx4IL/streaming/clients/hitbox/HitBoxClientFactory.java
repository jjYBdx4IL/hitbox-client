package com.github.jjYBdx4IL.streaming.clients.hitbox;

import java.net.URISyntaxException;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jjYBdx4IL
 */
public class HitBoxClientFactory {
    
    private static final Logger LOG = LoggerFactory.getLogger(HitBoxClientFactory.class);
    
    private static String getIP() {
        JSONArray arr = new JSONArray(HitBoxClient.readUrl("http://api.hitbox.tv/chat/servers.json?redis=true"));
        String serverIp = arr.getJSONObject(0).getString("server_ip");
        LOG.debug("server = " + serverIp);
        return serverIp;
    }

    public static HitBoxClient create(String name, String pass) throws URISyntaxException {
        return new HitBoxClient(name, pass, getIP());
    }

    private HitBoxClientFactory() {
    }
}
