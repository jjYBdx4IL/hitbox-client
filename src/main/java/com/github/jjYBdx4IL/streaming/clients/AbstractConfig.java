package com.github.jjYBdx4IL.streaming.clients;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
public abstract class AbstractConfig {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractConfig.class);
    public static final String DEFAULT_STRING_VALUE = "replace or delete me";
    public static final File CFG_DIR = new File(System.getProperty("user.home"), ".java-streaming-clients");
    
    void postprocess() {
    }

    public boolean read() throws FileNotFoundException, IOException {
        String filename = getClass().getSimpleName().toLowerCase(Locale.ROOT);
        filename = filename.replaceFirst("config$", "");
        filename += ".xml";
        
        File configFile = new File(CFG_DIR, filename);
        XStream xstream = new XStream(new StaxDriver());
        xstream.autodetectAnnotations(true);

        if (configFile.exists()) {
            xstream.fromXML(configFile, this);
            postprocess();
            return true;
        }
        
        // save empty config so user is able to add his details
        configFile.getParentFile().mkdirs();
        String xml = xstream.toXML(this);
        try (OutputStream os = new FileOutputStream(configFile)) {
            IOUtils.write(formatXml(xml), os);
        }
        return false;
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

    
}
