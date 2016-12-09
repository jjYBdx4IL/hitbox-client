package com.github.jjYBdx4IL.streaming.clients.twitch;

import com.github.jjYBdx4IL.utils.env.Surefire;
import java.net.URISyntaxException;
import org.junit.Assume;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jjYBdx4IL
 */
public class TwitchAPPTest {

    private static final Logger LOG = LoggerFactory.getLogger(TwitchAPPTest.class);

    @Test
    public void testGetOauthURI() throws URISyntaxException {
        LOG.info(new TwitchAPP().getOauthURI().toString());
    }
    
    @Test
    public void testGetOauthToken() {
        Assume.assumeTrue(Surefire.isSingleTextExecution());
        
        LOG.info(new TwitchAPP().getOauthToken());
    }

}
