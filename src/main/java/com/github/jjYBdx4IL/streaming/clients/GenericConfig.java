package com.github.jjYBdx4IL.streaming.clients;

import com.github.jjYBdx4IL.streaming.clients.xstream.CommaSeparatedStringListConverter;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

/**
 *
 * @author jjYBdx4IL
 */
public class GenericConfig {

    private static final Logger LOG = LoggerFactory.getLogger(GenericConfig.class);
    public static final File CFG_DIR = new File(System.getProperty("user.home"), ".java-streaming-clients");

    public String chatSound = "replace or delete me";
    public boolean chatSoundAsFallbackOnly = true;
    public String newFollowerSound = "replace or delete me";
    public String filesOutputFolder = "replace or delete me";
    @XStreamConverter(CommaSeparatedStringListConverter.class)
    public List<String> ignore = new ArrayList<>();
    @XStreamConverter(CommaSeparatedStringListConverter.class)
    public List<String> games = new ArrayList<>();
    public String discordBotToken = "replace or delete me";
    
    public GenericConfig() {
        ignore.add("comma-separated list of ignored users");
        ignore.add(" for example bot names");
        games.add("comma-separated list of game titles, will be used to set the stream game title on startup depending on the stream's title");
    }
    
    public static Object readConfig(String filename, Class<?> clazz) throws IOException {
        try {
            File configFile = new File(CFG_DIR, filename);
            
            XStream xstream = new XStream(new StaxDriver());
            xstream.autodetectAnnotations(true);
            
            if (configFile.exists()) {
                return xstream.fromXML(configFile);
            }
            
            // save empty config so user is able to add his details
            configFile.getParentFile().mkdirs();
            Object config = clazz.newInstance();
            String xml = xstream.toXML(config);
            try (OutputStream os = new FileOutputStream(configFile)) {
                IOUtils.write(formatXml(xml), os);
            }
            return config;
        } catch (InstantiationException|IllegalAccessException ex) {
            throw new IOException(ex);
        }
    }
    
    public static String formatXml(String xml) {

        try {
            Transformer serializer = SAXTransformerFactory.newInstance().newTransformer();

            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            Source xmlSource = new SAXSource(new InputSource(new ByteArrayInputStream(xml.getBytes())));
            StreamResult res = new StreamResult(new ByteArrayOutputStream());

            serializer.transform(xmlSource, res);

            return new String(((ByteArrayOutputStream) res.getOutputStream()).toByteArray());

        } catch (IllegalArgumentException | TransformerException e) {
            LOG.error("", e);
            return xml;
        }
    }

    public void postprocess() {
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
