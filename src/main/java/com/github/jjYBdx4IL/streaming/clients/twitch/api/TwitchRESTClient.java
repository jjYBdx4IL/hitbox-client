package com.github.jjYBdx4IL.streaming.clients.twitch.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jjYBdx4IL.streaming.clients.TwitchConfig;
import com.google.gson.Gson;

/**
 *
 * @author jjYBdx4IL
 */
public class TwitchRESTClient {

    private static final Logger LOG = LoggerFactory.getLogger(TwitchRESTClient.class);
    private final HttpClient httpclient;
    private final TwitchConfig config;

    public TwitchRESTClient() throws IOException {
        this.httpclient = HttpClients.createDefault();
        this.config = new TwitchConfig();
        this.config.read();
    }

    private String createURI(String channel, Class<? extends TwitchDTO> type) {
        if (channel == null || type == null) {
            throw new IllegalArgumentException();
        }

        URIBuilder b = new URIBuilder();
        b.setScheme("https");
        b.setHost("api.twitch.tv");
        b.addParameter("oauth_token", config.oauthToken.substring(config.oauthToken.indexOf(":") + 1));
        b.setPath(String.format(Locale.ROOT, "/kraken/channels/%s", channel.toLowerCase(Locale.ROOT)));

        String uri = b.toString();
        LOG.debug("constructed uri: " + uri);
        return uri;
    }

    public Object get(Class<? extends TwitchDTO> type) throws IOException {
        String uri = createURI(config.channel, type);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        HttpGet httpGet = new HttpGet(uri);
        HttpResponse response = httpclient.execute(httpGet);
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new IOException("url returned status code " + response.getStatusLine().getStatusCode() + ": " + uri);
        }
        try (InputStream is = response.getEntity().getContent()) {
            IOUtils.copy(is, baos);
        }

        byte[] data = baos.toByteArray();
        String reply = new String(data);
        LOG.debug("reply is: " + reply);
        Gson gson = new Gson();
        return gson.fromJson(reply, type);
    }

    public void put(Channel payload) throws IOException {
        String uri = createURI(config.channel, payload.getClass());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        HttpPut httpPut = new HttpPut(uri);
        Gson gson = new Gson();
        String _payload = "{\"channel\":" + gson.toJson(payload) + "}";
        LOG.debug("payload: " + _payload);
        StringEntity params = new StringEntity(_payload, "UTF-8");
        params.setContentType("application/json");
        httpPut.addHeader("Accept", "*/*");
        httpPut.addHeader("Accept-Encoding", "gzip,deflate,sdch");
        httpPut.addHeader("Accept-Language", "en-US,en;q=0.8");
        httpPut.setEntity(params);

        HttpResponse response = httpclient.execute(httpPut);
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new IOException("url returned status code " + response.getStatusLine().getStatusCode() + ": " + uri);
        }
        try (InputStream is = response.getEntity().getContent()) {
            IOUtils.copy(is, baos);
        }

        byte[] data = baos.toByteArray();
        String reply = new String(data);
        LOG.debug("reply is: " + reply);
    }
}
