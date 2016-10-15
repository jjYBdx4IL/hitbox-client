package com.mycompany.hitbox.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Based on https://www.reddit.com/r/hitbox/comments/2y27nm/tutorial_connecting_to_the_hitbox_chat_with/
 *
 * @author mark
 */
public class HitBoxClient extends WebSocketClient {

    public static final File CFG_FILE = new File(new File(System.getProperty("user.home"), ".hitbox-java-client"), "config.xml");

    public static String readUrl(String urlString) {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1) {
                buffer.append(chars, 0, read);
            }
            reader.close();
            return buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getIP() {
        JSONArray arr = new JSONArray(readUrl("http://api.hitbox.tv/chat/servers.json?redis=true"));
        return arr.getJSONObject(0).getString("server_ip");
    }

    private static String getID(String IP) {
        String connectionID = readUrl("http://" + IP + "/socket.io/1/");
        String ID = connectionID.substring(0, connectionID.indexOf(":"));
        return ID;
    }
    private static String token;

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
            try (DataInputStream is = new DataInputStream(connection.getInputStream())) {
                token = new JSONObject(is.readLine()).get("authToken").toString();
                //System.out.println(token);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return token;
    }
    private final String name;
    private final String channel;

    public HitBoxClient(String name, String pass, String channel, String IP) throws Exception {
        super(new URI("ws://" + IP + "/socket.io/1/websocket/" + getID(IP)), new Draft_10());
        this.name = name;
        this.channel = channel;
        connectBlocking();
        joinChannel(name, pass, channel.toLowerCase());
    }

    public void onOpen(ServerHandshake handshakedata) {

    }

    public void onMessage(String message) {
        try {
            if (message.equals("2::")) {
                this.send("2::");
            } else {
                //System.out.println(message);
                if (message.indexOf("{") == -1) {
                    return;
                }
                JSONObject jsonMessage = new JSONObject(message.substring(message.indexOf("{")));
                //System.out.println(jsonMessage.toString(2));
                String name = jsonMessage.get("name").toString();
                //System.out.println(name);
                if (name.equals("message")) {
                    //System.out.println(".");
                    JSONArray args = new JSONArray(jsonMessage.get("args").toString());
                    //System.out.println(args.length());
                    for (int i = 0; i < args.length(); i++) {
                        JSONObject argsElement = new JSONObject(args.getString(i));
                        JSONObject params = argsElement.getJSONObject("params");
                        //System.out.println(argsElement.toString(2));
                        if (argsElement.get("method").toString().equals("chatMsg") && params.has("name")) {
                            System.out.println(params.get("name").toString() + ": " + params.get("text").toString());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    public void onClose(int code, String reason, boolean remote) {

    }

    public void onError(Exception e) {

    }

    public void joinChannel(String name, String pass, String channel) {
        this.send("5:::{\"name\":\"message\",\"args\":[{\"method\":\"joinChannel\",\"params\":{\"channel\":\"" + channel + "\",\"name\":\"" + name + "\",\"token\":\"" + getToken(name, pass) + "\",\"isAdmin\":false}}]}");
        System.out.println("Channel Joined.");
    }

    public void sendMessage(String name, String channel, String message) {
        this.send("5:::{\"name\":\"message\",\"args\":[{\"method\":\"chatMsg\",\"params\":{\"channel\":\"" + channel + "\",\"name\":\"" + name + "\",\"nameColor\":\"FA5858\",\"text\":\"" + message + "\"}}]}");
    }
    static HitBoxClient client;

    public static void main(String[] args) {
        try {
            Properties p = new Properties();
            p.setProperty("botname", "replace me");
            p.setProperty("password", "replace me");
            p.setProperty("channel", "replace me");
            if (CFG_FILE.exists()) {
                try (InputStream is = new FileInputStream(CFG_FILE)) {
                    p.loadFromXML(is);
                }
            } else {
                // save empty config so user is able to add his details
                CFG_FILE.getParentFile().mkdirs();
                try (OutputStream os = new FileOutputStream(CFG_FILE)) {
                    p.storeToXML(os, null);
                }
            }
            if (p.getProperty("botname").equals("replace me")) {
                throw new RuntimeException("please update your config file at " + CFG_FILE.getCanonicalPath());
            }
            client = new HitBoxClient(p.getProperty("botname"), p.getProperty("password"), p.getProperty("channel"), HitBoxClient.getIP());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
