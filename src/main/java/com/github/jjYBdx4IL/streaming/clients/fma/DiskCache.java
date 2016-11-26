package com.github.jjYBdx4IL.streaming.clients.fma;

import com.github.jjYBdx4IL.streaming.clients.GenericConfig;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jjYBdx4IL
 */
public class DiskCache {

    private static final Logger LOG = LoggerFactory.getLogger(DiskCache.class);
    private final Connection conn;
    private static final String TABLE_NAME = "binarydata";
    private final long expiryMillis;
    public static final int MAX_URL_LEN = 32672;
    private final HttpClient httpclient;
    public static final long DEFAULT_EXPIRY_SECS = 86400;

    public DiskCache(File parentDir) {
        this(parentDir, DEFAULT_EXPIRY_SECS);
    }
    
    public DiskCache() {
        this(null, DEFAULT_EXPIRY_SECS);
    }

    public DiskCache(File parentDir, long expirySecs) {
        httpclient = HttpClients.createDefault();

        try {
            expiryMillis = expirySecs * 1000L;
            String derbyLog = new File(parentDir != null ? parentDir : GenericConfig.CFG_DIR, "derby.log").getAbsolutePath();
            LOG.info("setting derby log file to " + derbyLog);
            System.setProperty("derby.stream.error.file", derbyLog);
            String dbLocation = new File(parentDir != null ? parentDir : GenericConfig.CFG_DIR, "webcache").getAbsolutePath();
            dbLocation = dbLocation.replaceAll(":", "\\:");
            conn = DriverManager.getConnection("jdbc:derby:" + dbLocation + ";create=true");
            final boolean tableExists;
            try (ResultSet res = conn.getMetaData().getTables(null, "APP", TABLE_NAME.toUpperCase(), null)) {
                tableExists = res.next();
            }
            if (!tableExists) {
                execStmt("CREATE TABLE " + TABLE_NAME + " (ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), url VARCHAR(" + MAX_URL_LEN + ") UNIQUE NOT NULL, lmod BIGINT NOT NULL, value BLOB NOT NULL)");
                execStmt("CREATE INDEX idx0 ON " + TABLE_NAME + " (url)");
                LOG.info("initialized.");
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void execStmt(String s) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(s);
        }
    }

    public void put(URL url, byte[] data) throws IOException {
        String urlExternalForm = url.toExternalForm();
        if (urlExternalForm.length() > MAX_URL_LEN) {
            throw new IllegalArgumentException("url too long: " + urlExternalForm);
        }
        try {
            Blob blob = conn.createBlob();
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO binarydata(url,lmod,value) VALUES(?,?,?)")) {
                ps.setString(1, url.toExternalForm());
                ps.setLong(2, System.currentTimeMillis());
                try (OutputStream os = blob.setBinaryStream(1)) {
                    IOUtils.write(data, os);
                }
                ps.setBlob(3, blob);
                ps.execute();
            } finally {
                blob.free();
            }
        } catch (SQLException ex) {
            throw new IOException(ex);
        }
        LOG.debug("stored " + urlExternalForm);
    }

    public byte[] get(URL url) throws IOException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT value FROM binarydata WHERE url = ? AND lmod > ?")) {
            ps.setString(1, url.toExternalForm());
            ps.setLong(2, System.currentTimeMillis() - expiryMillis);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Blob value = rs.getBlob(1);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try (InputStream is = value.getBinaryStream()) {
                        IOUtils.copy(is, baos);
                    }
                    return baos.toByteArray();
                }
            }
        } catch (SQLException ex) {
            throw new IOException(ex);
        }
        return null;
    }

    public byte[] retrieve(URL url) throws IOException {
        byte[] data = get(url);
        if (data != null) {
            LOG.debug("returning cached data for " + url.toExternalForm());
            return data;
        }

        LOG.debug("retrieving " + url.toExternalForm());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        HttpGet httpGet = new HttpGet(url.toExternalForm());
        HttpResponse response = httpclient.execute(httpGet);
        if ( response.getStatusLine().getStatusCode() != 200) {
            throw new IOException("url returned status code " + response.getStatusLine().getStatusCode() + ": " + url.toExternalForm());
        }
        try (InputStream is = response.getEntity().getContent()) {
            IOUtils.copy(is, baos);
        }

        data = baos.toByteArray();
        put(url, data);
        return data;
    }

    public byte[] retrieve(String url) throws IOException {
        try {
            return retrieve(new URL(url));
        } catch (MalformedURLException ex) {
            throw new IOException(ex);
        }
    }
}
