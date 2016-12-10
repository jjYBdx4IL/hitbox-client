package com.github.jjYBdx4IL.streaming.clients;

import com.github.jjYBdx4IL.streaming.clients.xstream.CommaSeparatedStringListConverter;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jjYBdx4IL
 */
public class GenericConfig extends AbstractConfig {

    private static final Logger LOG = LoggerFactory.getLogger(GenericConfig.class);

    public String chatSound = DEFAULT_STRING_VALUE;
    public boolean chatSoundAsFallbackOnly = true;
    public String newFollowerSound = DEFAULT_STRING_VALUE;
    public String filesOutputFolder = DEFAULT_STRING_VALUE;
    @XStreamConverter(CommaSeparatedStringListConverter.class)
    public List<String> ignore = new ArrayList<>();
    @XStreamConverter(CommaSeparatedStringListConverter.class)
    public List<String> games = new ArrayList<>();
    
    public GenericConfig() {
        ignore.add("comma-separated list of ignored users");
        ignore.add(" for example bot names");
        games.add("comma-separated list of game titles, will be used to set the stream game title on startup depending on the stream's title");
    }
    
    @Override
    void postprocess() {
        if (!new File(filesOutputFolder).isAbsolute()) {
            filesOutputFolder = new File(CFG_DIR, filesOutputFolder).getAbsolutePath();
            LOG.debug("expanded filesOutputFolder to: " + filesOutputFolder);
        }

        if (ignore == null) {
            ignore = new ArrayList<>();
        }
        for (int i = 0; i < ignore.size(); i++) {
            ignore.set(i, ignore.get(i).toLowerCase(Locale.ROOT));
        }

        if (games == null) {
            games = new ArrayList<>();
        }
        
        LOG.info("ignored users: " + Arrays.toString(ignore.toArray()));
    }
    

}
