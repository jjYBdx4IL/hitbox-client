package com.github.jjYBdx4IL.streaming.clients;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

/**
 *
 * @author mark
 */
public class GenericConfig {

    private static final Logger log = LoggerFactory.getLogger(GenericConfig.class);
    public static final File CFG_DIR = new File(System.getProperty("user.home"), ".java-streaming-clients");

    public String chatSound = "replace or delete me";
    public String newFollowerSound = "replace or delete me";
    public String filesOutputFolder = "replace or delete me";

    public static Object readConfig(String filename, Class<?> clazz) throws IOException, InstantiationException, IllegalAccessException {
        File configFile = new File(CFG_DIR, filename);

        Object config = null;

        XStream xstream = new XStream(new StaxDriver());
        if (configFile.exists()) {
            return xstream.fromXML(configFile);
        } else {
            // save empty config so user is able to add his details
            configFile.getParentFile().mkdirs();
            config = clazz.newInstance();
            String xml = xstream.toXML(config);
            try (OutputStream os = new FileOutputStream(configFile)) {
                IOUtils.write(formatXml(xml), os);
            }
        }
        return config;
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

        } catch (Exception e) {
            log.error("", e);
            return xml;
        }
    }

    void postprocess() {
        if (!new File(filesOutputFolder).isAbsolute()) {
            filesOutputFolder = new File(CFG_DIR, filesOutputFolder).getAbsolutePath();
            log.info("expanded filesOutputFolder to: " + filesOutputFolder);
        }
    }
    

}
