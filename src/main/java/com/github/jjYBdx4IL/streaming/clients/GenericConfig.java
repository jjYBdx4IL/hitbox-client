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
    public String discordBotToken = DEFAULT_STRING_VALUE;
    
    public GenericConfig() {
        ignore.add("comma-separated list of ignored users");
        ignore.add(" for example bot names");
        games.add("comma-separated list of game titles, will be used to set the stream game title on startup depending on the stream's title");
    }
    
//    public static Object readConfig(String filename, Class<?> clazz) throws IOException {
//        try {
//            File configFile = new File(CFG_DIR, filename);
//            
//            XStream xstream = new XStream(new StaxDriver());
//            xstream.autodetectAnnotations(true);
//            
//            if (configFile.exists()) {
//                return xstream.fromXML(configFile);
//            }
//            
//            // save empty config so user is able to add his details
//            configFile.getParentFile().mkdirs();
//            Object config = clazz.newInstance();
//            String xml = xstream.toXML(config);
//            try (OutputStream os = new FileOutputStream(configFile)) {
//                IOUtils.write(formatXml(xml), os);
//            }
//            return config;
//        } catch (InstantiationException|IllegalAccessException ex) {
//            throw new IOException(ex);
//        }
//    }
    
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
