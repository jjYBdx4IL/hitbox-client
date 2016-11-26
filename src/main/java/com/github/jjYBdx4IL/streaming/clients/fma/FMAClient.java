package com.github.jjYBdx4IL.streaming.clients.fma;

import com.github.jjYBdx4IL.streaming.clients.GenericConfig;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FMA API docs: https://freemusicarchive.org/api
 *
 * @author jjYBdx4IL
 */
public class FMAClient implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(FMAClient.class);
    public static final String API_URL = "https://freemusicarchive.org/api/get/";
    public static final String TRACKSEARCH_URL = "https://freemusicarchive.org/api/trackSearch?q=&limit=3";
    // free for commercial use tracks:
    public static final String SEARCH_URL = "http://freemusicarchive.org/search/.json?adv=1&quicksearch=&search-genre=Genres&duration_from=&duration_to=&music-filter-CC-attribution-only=on&music-filter-CC-attribution-sharealike=1&music-filter-CC-attribution-noderivatives=1&music-filter-public-domain=1&music-filter-commercial-allowed=1&sort=track_date_published&d=1&page=1&per_page=1000";
    private final DiskCache cache;
    private final FMAConfig config;

    public FMAClient() throws IOException {
        cache = new DiskCache();
        config = (FMAConfig) GenericConfig.readConfig("fma.xml", FMAConfig.class);
    }

    public void start() {
        Thread t = new Thread(this, FMAClient.class.getSimpleName());
        t.start();
    }
    
    public void run() {
        try {
            byte[] data = cache.retrieve(SEARCH_URL); // + "&api_key=" + config.apiKey);
            LOG.info(new String(data));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
