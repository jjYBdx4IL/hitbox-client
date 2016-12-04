package com.github.jjYBdx4IL.streaming.clients.twitch.api;

import com.github.jjYBdx4IL.utils.env.Surefire;
import com.google.gson.Gson;

import static org.junit.Assert.*;
import org.junit.Assume;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Github jjYBdx4IL Projects
 */
public class TwitchRESTClientTest {

    private static final Logger LOG = LoggerFactory.getLogger(TwitchRESTClientTest.class);

    @Test
    public void testGet() throws Exception {
        Assume.assumeTrue(Surefire.isSingleTextExecution());

        TwitchRESTClient client = new TwitchRESTClient();
        Channel channel = (Channel) client.get(Channel.class);
        assertNotNull(channel);
        LOG.info(channel.toString());

        Gson gson = new Gson();
        LOG.info(gson.toJson(channel));

        channel.status = "test";
        channel.game = "abs";
        client.put(channel);
    }

}
