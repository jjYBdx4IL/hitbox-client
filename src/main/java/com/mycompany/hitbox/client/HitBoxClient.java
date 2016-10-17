package com.mycompany.hitbox.client;

import com.mycompany.twitch.client.TwitchChatListener;
import com.mycompany.twitch.client.TwitchIRCClient;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

/**
 * Based on
 * https://www.reddit.com/r/hitbox/comments/2y27nm/tutorial_connecting_to_the_hitbox_chat_with/
 *
 * @author mark
 */
public class HitBoxClient extends WebSocketClient {

    private static final Logger log = LoggerFactory.getLogger(HitBoxClient.class);
    public static final File CFG_FILE = new File(new File(System.getProperty("user.home"), ".hitbox-java-client"), "config.xml");
    public static Config config = new Config();
    public long chatSessionStartingTime = System.currentTimeMillis();
    private static Timer chatLogRemovalTimer = null;
    public static final long KEEP_CHATLOG_MILLIS = 30 * 1000L;

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
            log.error("", e);
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
            }
        } catch (Exception e) {
            log.error("", e);
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
                return;
            }

            boolean messageRecognized = false;

            log.trace(message);
            if (message.indexOf("{") == -1) {
                return;
            }
            JSONObject jsonMessage = new JSONObject(message.substring(message.indexOf("{")));
            log.trace(jsonMessage.toString(2));
            String name = jsonMessage.get("name").toString();
            log.trace(name);
            if (name.equals("message")) {
                JSONArray args = new JSONArray(jsonMessage.get("args").toString());
                log.trace(""+args.length());
                for (int i = 0; i < args.length(); i++) {
                    JSONObject argsElement = new JSONObject(args.getString(i));
                    JSONObject params = argsElement.getJSONObject("params");
                    log.trace(argsElement.toString(2));
                    if (argsElement.get("method").toString().equals("chatMsg") && params.has("name")) {
                        messageRecognized = true;
                        long chatMessageTimestamp = Long.valueOf(params.get("time").toString()) * 1000L;
                        if (chatMessageTimestamp > chatSessionStartingTime) {
                            System.out.println(params.get("name").toString() + ": " + params.get("text").toString());
                            logChatMessage(params.get("name").toString(), params.get("text").toString());
                            playSound(config.chatSound);
                        }
                    }
                    if (argsElement.get("method").toString().equals("chatLog") && params.has("text")) {
                        messageRecognized = true;
                        long chatMessageTimestamp = Long.valueOf(params.get("timestamp").toString()) * 1000L;
                        if (chatMessageTimestamp > chatSessionStartingTime) {
                            System.out.println(params.get("text").toString());
                            logLatestFollower(params.get("text").toString());
                        }
                    }
                }
            }
            if (!messageRecognized) {
                log.info(jsonMessage.toString(2));
            }
        } catch (Exception ex) {
            log.error("", ex);
            throw ex;
        }
    }

    public void onClose(int code, String reason, boolean remote) {
        log.info("closed, " + reason);
    }

    public void onError(Exception e) {
        log.error("", e);
    }

    public void joinChannel(String name, String pass, String channel) {
        this.send("5:::{\"name\":\"message\",\"args\":[{\"method\":\"joinChannel\",\"params\":{\"channel\":\"" + channel + "\",\"name\":\"" + name + "\",\"token\":\"" + getToken(name, pass) + "\",\"isAdmin\":false}}]}");
        System.out.println("Channel Joined.");
        chatSessionStartingTime = System.currentTimeMillis();
    }

    public void sendMessage(String name, String channel, String message) {
        this.send("5:::{\"name\":\"message\",\"args\":[{\"method\":\"chatMsg\",\"params\":{\"channel\":\"" + channel + "\",\"name\":\"" + name + "\",\"nameColor\":\"FA5858\",\"text\":\"" + message + "\"}}]}");
    }
    static HitBoxClient client;

    public static void main(String[] args) {
        try {
            // read config
            XStream xstream = new XStream(new StaxDriver());
            if (CFG_FILE.exists()) {
                config = (Config) xstream.fromXML(CFG_FILE);
            } else {
                // save empty config so user is able to add his details
                CFG_FILE.getParentFile().mkdirs();
                String xml = xstream.toXML(new Config());
                try (OutputStream os = new FileOutputStream(CFG_FILE)) {
                    IOUtils.write(formatXml(xml), os);
                }
            }
            if (config.botname.equalsIgnoreCase("replace me")) {
                throw new RuntimeException("please update your config file at " + CFG_FILE.getCanonicalPath());
            }
            rescheduleChatLogRemovalTimer();
            
            // start Hitbox client
            client = new HitBoxClient(config.botname, config.password, config.channel, HitBoxClient.getIP());
            
            // start twitch client
            TwitchIRCClient twitchClient = new TwitchIRCClient(config.twitchBotname, config.twitchOauthToken);
            twitchClient.connect();
            twitchClient.joinChannel(config.twitchChannel, new TwitchChatListener() {
                @Override
                public void onChatMessage(String from, String message) {
                    log.info(from + ": " + message);
                    logChatMessage(from, message);
                    playSound(config.chatSound);
                }
            });
            Thread.sleep(1000L*3600L*1000L);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    public static void logLatestFollower(String text) {
        System.out.println(text);
        Pattern pat = Pattern.compile("<user>(.*)</user> followed");
        Matcher m = pat.matcher(text);
        // unfollow?
        if (!m.find()) {
            return;
        }
        String followerName = m.group(1);
        if (followerName.isEmpty()) {
            return;
        }
        if (config.filesOutputFolder == null) {
            return;
        }
        File latestFollowerFile = new File(config.filesOutputFolder, "latestFollower.txt");
        if (!latestFollowerFile.getParentFile().exists()) {
            latestFollowerFile.getParentFile().mkdirs();
        }
        try (OutputStream os = new FileOutputStream(latestFollowerFile, false)) {
            try (OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8")) {
                osw.append("Latest Follower: " + followerName + System.lineSeparator());
            }
        } catch (IOException ex) {
            log.error("", ex);
        }
        playSound(config.newFollowerSound);
    }

    public static void rescheduleChatLogRemovalTimer() {
        if (config.filesOutputFolder == null) {
            return;
        }
        File chatLogFile = new File(config.filesOutputFolder, "chat.log");
        if (chatLogRemovalTimer != null) {
            chatLogRemovalTimer.cancel();
        }
        chatLogRemovalTimer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                chatLogFile.delete();
            }
        };
        chatLogRemovalTimer.schedule(timerTask, KEEP_CHATLOG_MILLIS);
    }

    public static void logChatMessage(String name, String text) {
        if (config.filesOutputFolder == null) {
            return;
        }
        File chatLogFile = new File(config.filesOutputFolder, "chat.log");
        if (!chatLogFile.getParentFile().exists()) {
            chatLogFile.getParentFile().mkdirs();
        }
        try (OutputStream os = new FileOutputStream(chatLogFile, true)) {
            try (OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8")) {
                osw.append(name + ": " + text + System.lineSeparator());
            }
            rescheduleChatLogRemovalTimer();
        } catch (IOException ex) {
            log.error("", ex);
        }
    }

    public static void playSound(String fileName) {
        if (fileName == null) {
            return;
        }
        File soundFile = new File(fileName);
        if (!soundFile.exists()) {
            return;
        }
        play(soundFile);
    }

    public static void play(File file) {
        try {
            final Clip clip = (Clip) AudioSystem.getLine(new Line.Info(Clip.class));

            clip.addLineListener(new LineListener() {
                @Override
                public void update(LineEvent event) {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                }
            });

            clip.open(AudioSystem.getAudioInputStream(file));
            clip.start();
        } catch (Exception exc) {
            log.error("", exc);
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

        } catch (Exception e) {
            log.error("", e);
            return xml;
        }
    }
}
