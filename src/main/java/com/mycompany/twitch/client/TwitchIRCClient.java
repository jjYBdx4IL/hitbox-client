package com.mycompany.twitch.client;

import com.mycompany.hitbox.client.Config;
import static com.mycompany.hitbox.client.HitBoxClient.CFG_FILE;
import static com.mycompany.hitbox.client.HitBoxClient.config;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.telnet.TelnetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TwitchIRCClient implements Runnable {

    public static final String LF = "\r\n";
    private static final Logger log = LoggerFactory.getLogger(TwitchIRCClient.class);
    public static final long MAX_WAIT_MILLIS = 30 * 1000L;
    public static final Pattern STATUSLINE_PATTERN = Pattern.compile("^:(\\S+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S.*)$");
    public static final Pattern COMMANDLINE_PATTERN = Pattern.compile("^(?!:)(\\S+)(?:|\\s+(\\S.*))$");
    public static final Pattern CHANNELMSG_PATTERN = Pattern.compile("^:([^! ]+)!\\S+\\.tmi\\.twitch\\.tv\\s+PRIVMSG\\s+(#\\S+)\\s+:(.+)$");

    private final TelnetClient tc = new TelnetClient();
    private final String botname;
    private final String password;
    private final Set<IRCStatusListener> statusListeners;
    private final Set<IRCCommandListener> commandListeners;
    private final Set<IRCChannelMessageListener> channelMessageListeners;
    private final Thread reader = new Thread(this);

    public TwitchIRCClient(String botname, String password) {
        this.statusListeners = Collections.synchronizedSet(new HashSet<>());
        this.commandListeners = Collections.synchronizedSet(new HashSet<>());
        this.channelMessageListeners = Collections.synchronizedSet(new HashSet<>());
        this.botname = botname;
        this.password = password;
    }

    @Override
    public void run() {

        try {
            InputStream instr = tc.getInputStream();

            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader(instr));
            while ((line = br.readLine()) != null) {
                log.info("< " + line);

                Matcher m = STATUSLINE_PATTERN.matcher(line);
                if (m.find()) {
                    for (IRCStatusListener listener : statusListeners) {
                        listener.onStatusLineReceived(m.group(1), Integer.valueOf(m.group(2)), m.group(3), m.group(4));
                    }
                    continue;
                }

                m = COMMANDLINE_PATTERN.matcher(line);
                if (m.find()) {
                    for (IRCCommandListener listener : commandListeners) {
                        listener.onCommandReceived(m.group(1), m.group(2));
                    }
                }

                m = CHANNELMSG_PATTERN.matcher(line);
                if (m.find()) {
                    for (IRCChannelMessageListener listener : channelMessageListeners) {
                        listener.onChannelMessageReceived(m.group(1), m.group(2), m.group(3));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Exception while reading socket:" + e.getMessage());
        }

        try {
            tc.disconnect();
        } catch (IOException e) {
            System.err.println("Exception while closing telnet:" + e.getMessage());
        }
    }

    public void addListener(IRCChannelMessageListener listener) {
        channelMessageListeners.add(listener);
    }

    public void removeListener(IRCChannelMessageListener listener) {
        channelMessageListeners.remove(listener);
    }

    public void addListener(IRCCommandListener listener) {
        commandListeners.add(listener);
    }

    public void removeListener(IRCCommandListener listener) {
        commandListeners.remove(listener);
    }

    public void addListener(IRCStatusListener listener) {
        statusListeners.add(listener);
    }

    public void removeListener(IRCStatusListener listener) {
        statusListeners.remove(listener);
    }

    public void connect() throws IOException, InterruptedException {

        tc.connect("irc.twitch.tv", 6667);
        reader.start();

        // auth
        sendNoLog("PASS " + password);
        sendAndWait("NICK " + botname.toLowerCase(), 376);
        log.info("login to twitch irc successful");

        // PING handler
        addListener(new IRCCommandListener() {
            @Override
            public void onCommandReceived(String command, String args) {
                try {
                    send("PONG " + args);
                } catch (IOException ex) {
                    log.error("", ex);
                }
            }
        });
    }
    
    public void joinChannel(String channel, TwitchChatListener listener) throws IOException {
        
        final String ircChannelName = "#" + channel.toLowerCase();
        
        sendAndWait("JOIN " + ircChannelName, 366);

        addListener(new IRCChannelMessageListener() {
            @Override
            public void onChannelMessageReceived(String from, String channelRcvd, String message) {
                if (channelRcvd.equals(ircChannelName)) {
                    log.info("msg received on channel " + ircChannelName + ": " + from + ": " + message);
                    listener.onChatMessage(from, message);
                }
            }
        });

    }

    public void loop() {
        try {
            reader.join();
        } catch (InterruptedException ex) {
            log.error("", ex);
        }
    }

    public void sendNoLog(String cmd) throws IOException {
        IOUtils.write(cmd + LF, tc.getOutputStream());
        tc.getOutputStream().flush();
    }
    
    public void send(String cmd) throws IOException {
        log.info("> " + cmd);
        IOUtils.write(cmd + LF, tc.getOutputStream());
        tc.getOutputStream().flush();
    }

    public void sendAndWait(String cmd, int retCode) throws IOException {
        AtomicBoolean retCodeReceived = new AtomicBoolean(false);
        IRCStatusListener listener = new IRCStatusListener() {
            @Override
            public void onStatusLineReceived(String from, int code, String to, String args) {
                if (code == retCode) {
                    retCodeReceived.set(true);
                    synchronized (retCodeReceived) {
                        retCodeReceived.notifyAll();
                    }
                }
            }
        };
        addListener(listener);
        send(cmd);

        synchronized (retCodeReceived) {
            try {
                retCodeReceived.wait(MAX_WAIT_MILLIS);
            } catch (InterruptedException ex) {
                log.error("", ex);
            }
        }
        if (!retCodeReceived.get()) {
            throw new RuntimeException("timed out waiting for code " + retCode);
        }
        removeListener(listener);
        log.info("success: " + cmd);
    }

    public static void main(String[] args) throws InterruptedException {
        try {
            XStream xstream = new XStream(new StaxDriver());
            config = (Config) xstream.fromXML(CFG_FILE);
            TwitchIRCClient client = new TwitchIRCClient(config.twitchBotname, config.twitchOauthToken);
            client.connect();
            client.joinChannel(config.twitchChannel, new TwitchChatListener() {
                @Override
                public void onChatMessage(String from, String message) {
                    log.info(from + ": " + message);
                }
            });
            client.loop();
        } catch (IOException ex) {
            log.error("", ex);
        }
    }

}
