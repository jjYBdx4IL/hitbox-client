package com.github.jjYBdx4IL.streaming.clients.twitch.api;

import com.github.jjYBdx4IL.utils.env.Surefire;
import com.google.gson.Gson;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

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
    public void testGetAndUpdateChannelStatus() throws Exception {
        Assume.assumeTrue(Surefire.isSingleTextExecution());

        TwitchRESTClient client = new TwitchRESTClient();
        Channel channel = client.getChannelStatus();
        assertNotNull(channel);
        LOG.info(channel.toString());

        Gson gson = new Gson();
        LOG.info(gson.toJson(channel));

        channel.status = "test";
        channel.game = "abs";
        client.putChannelStatus(channel);
    }
    
    @Test
    public void testGetFollowedLiveStreams() throws IOException {
        Assume.assumeTrue(Surefire.isSingleTextExecution());

        TwitchRESTClient client = new TwitchRESTClient();
        List<Channel> channels = client.getFollowedLiveStreams(Stream.TYPE.live);
        assertNotNull(channels);
        for (Channel channel : channels) {
        	LOG.info(channel.toString());
        }
    }
    

}
