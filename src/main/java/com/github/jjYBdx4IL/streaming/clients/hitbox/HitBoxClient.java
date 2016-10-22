package com.github.jjYBdx4IL.streaming.clients.hitbox;

import com.github.jjYBdx4IL.streaming.clients.ChatListener;
import com.github.jjYBdx4IL.streaming.clients.ChatListenerHandler;
import com.github.jjYBdx4IL.streaming.clients.FollowerListener;
import com.github.jjYBdx4IL.streaming.clients.FollowerListenerHandler;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.NotYetConnectedException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Based on
 * https://www.reddit.com/r/hitbox/comments/2y27nm/tutorial_connecting_to_the_hitbox_chat_with/
 *
 * @author jjYBdx4IL
 */
public class HitBoxClient extends WebSocketClient implements ChatListenerHandler, FollowerListenerHandler {

    private static final Logger LOG = LoggerFactory.getLogger(HitBoxClient.class);
    // consider the connection dead when the server does not send anything for 6 mins
    public static final long SERVER_TIMEOUT_MILLIS = 6 * 60 * 1000L;
    private static String token;
    public static String readUrl(String urlString) {
        BufferedReader reader;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1) {
                buffer.append(chars, 0, read);
            }
            reader.close();
            return buffer.toString();
        } catch (IOException e) {
            LOG.error("", e);
        }
        return null;
    }
    private static String getID(String IP) {
        String connectionID = readUrl("http://" + IP + "/socket.io/1/");
        String ID = connectionID.substring(0, connectionID.indexOf(':'));
        //log.info("connection ID: " + ID);
        return ID;
    }
    private static String getToken(String name, String pass) {
        try {
            URL url = new URL("http://api.hitbox.tv/auth/token");
            URLConnection connection = url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            try (DataOutputStream os = new DataOutputStream(connection.getOutputStream())) {
                String content = "login=" + name + "&pass=" + pass;
                os.writeBytes(content);
                os.flush();
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                token = new JSONObject(br.readLine()).get("authToken").toString();
            }
        } catch (IOException | JSONException e) {
            LOG.error("", e);
        }
        //log.info("token: " + token);
        return token;
    }
    private long chatSessionStartingTime = System.currentTimeMillis();
    private final Set<ChatListener> chatListeners;
    private final Set<FollowerListener> followerListeners;
    private final Pattern FOLLOW_PATTERN = Pattern.compile("<user>(.*)</user> followed");
    private final Pattern UNFOLLOW_PATTERN = Pattern.compile("<user>(.*)</user> unfollowed");
    private final AtomicLong lastServerActivityTimestamp = new AtomicLong(System.currentTimeMillis());
    private final String username;
    private final String pass;

    public HitBoxClient(String name, String pass, String IP) throws URISyntaxException {
        super(new URI("ws://" + IP + "/socket.io/1/websocket/" + getID(IP)), new Draft_10());
        //log.info("websocket connection URI: " + getURI());
        this.username = name;
        this.pass = pass;
        this.chatListeners = Collections.synchronizedSet(new HashSet<>());
        this.followerListeners = Collections.synchronizedSet(new HashSet<>());
    }
    
    public void shutdown() {
        close();
    }
    
    @Override
    public void onOpen(ServerHandshake handshakedata) {

    }

    @Override
    public void onMessage(String message) {
        lastServerActivityTimestamp.set(System.currentTimeMillis());
        
        try {
            if (message.equals("2::")) {
                this.send("2::");
                return;
            }

            boolean messageRecognized = false;

            LOG.trace(message);
            if (!message.contains("{")) {
                return;
            }
            JSONObject jsonMessage = new JSONObject(message.substring(message.indexOf('{')));
            LOG.trace(jsonMessage.toString(2));
            String name = jsonMessage.get("name").toString();
            LOG.trace(name);
            if (name.equals("message")) {
                JSONArray args = new JSONArray(jsonMessage.get("args").toString());
                LOG.trace("" + args.length());
                for (int i = 0; i < args.length(); i++) {
                    JSONObject argsElement = new JSONObject(args.getString(i));
                    JSONObject params = argsElement.getJSONObject("params");
                    LOG.trace(argsElement.toString(2));
                    if (argsElement.get("method").toString().equals("chatMsg") && params.has("name")) {
                        messageRecognized = true;
                        long chatMessageTimestamp = Long.valueOf(params.get("time").toString()) * 1000L;
                        if (chatMessageTimestamp > chatSessionStartingTime) {
                            LOG.trace(params.get("name").toString() + ": " + params.get("text").toString());
                            for (ChatListener listener : chatListeners) {
                                listener.onChatMessage(params.get("name").toString(), params.get("text").toString());
                            }
                        }
                    }
                    if (argsElement.get("method").toString().equals("chatLog") && params.has("text")) {
                        messageRecognized = true;
                        long chatMessageTimestamp = Long.valueOf(params.get("timestamp").toString()) * 1000L;
                        if (chatMessageTimestamp > chatSessionStartingTime) {
                            LOG.trace(params.get("text").toString());
                            handleFollower(params.get("text").toString());
                        }
                    }
                }
            }
            if (!messageRecognized) {
                LOG.info("unhandled message: " + jsonMessage.toString(2));
            }
        } catch (NumberFormatException | NotYetConnectedException | JSONException ex) {
            LOG.error("", ex);
            throw ex;
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        LOG.info("closed, " + reason);
    }

    @Override
    public void onError(Exception e) {
        LOG.error("", e);
    }

    public void joinChannel(String name, String pass, String channel) {
        this.send("5:::{\"name\":\"message\",\"args\":[{\"method\":\"joinChannel\",\"params\":{\"channel\":\"" + channel.toLowerCase() + "\",\"name\":\"" + name + "\",\"token\":\"" + getToken(name, pass) + "\",\"isAdmin\":false}}]}");
        LOG.info("Channel " + channel + " joined.");
        chatSessionStartingTime = System.currentTimeMillis();
    }

    public void sendMessage(String name, String channel, String message) {
        this.send("5:::{\"name\":\"message\",\"args\":[{\"method\":\"chatMsg\",\"params\":{\"channel\":\"" + channel + "\",\"name\":\"" + name + "\",\"nameColor\":\"FA5858\",\"text\":\"" + message + "\"}}]}");
    }

    public void handleFollower(String text) {
        LOG.trace(text);
        
        Matcher m = FOLLOW_PATTERN.matcher(text);
        if (m.find()) {
            String followerName = m.group(1);
            if (followerName.isEmpty()) {
                return;
            }
            for (FollowerListener listener : followerListeners) {
                listener.onFollow(followerName);
            }
            return;
        }
        
        m = UNFOLLOW_PATTERN.matcher(text);
        if (m.find()) {
            String unfollowerName = m.group(1);
            if (unfollowerName.isEmpty()) {
                return;
            }
            for (FollowerListener listener : followerListeners) {
                listener.onUnfollow(unfollowerName);
            }
        }
    }

    @Override
    public void addChatListener(ChatListener listener) {
        chatListeners.add(listener);
    }

    @Override
    public void removeChatListener(ChatListener listener) {
        chatListeners.remove(listener);
    }

    @Override
    public void addFollowerListener(FollowerListener listener) {
        followerListeners.add(listener);
    }

    @Override
    public void removeFollowerListener(FollowerListener listener) {
        followerListeners.remove(listener);
    }

    public boolean isConnected() {
        return System.currentTimeMillis() - lastServerActivityTimestamp.get() < SERVER_TIMEOUT_MILLIS;
    }

}
