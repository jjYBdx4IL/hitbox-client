/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jjYBdx4IL.streaming.clients.fma;

import static com.github.jjYBdx4IL.streaming.clients.fma.FMAClient.SEARCH_URL;
import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author mark
 */
public class FMAClientTest {
    
    private static final Logger LOG = LoggerFactory.getLogger(FMAClientTest.class);
    private final DiskCache cache;
    
    public FMAClientTest() {
        this.cache = new DiskCache(new File(System.getProperty("basedir"), "target"));
    }

    @Test
    public void testFMA() throws IOException {
        byte[] data = cache.retrieve(SEARCH_URL);
        Gson gson = new Gson();
        FMASearchResult result = gson.fromJson(new String(data), FMASearchResult.class);
        assertNotNull(result);
        assertEquals(1000, result.aTracks.size());
        LOG.info(result.aTracks.get(0).toString());
    }
    
}
