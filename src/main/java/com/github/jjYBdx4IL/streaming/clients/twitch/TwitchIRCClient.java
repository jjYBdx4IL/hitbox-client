package com.github.jjYBdx4IL.streaming.clients.twitch;

import com.github.jjYBdx4IL.streaming.clients.TwitchChatListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TwitchIRCClient implements Runnable {

    public static final String LF = "\r\n";
    private static final Logger log = LoggerFactory.getLogger(TwitchIRCClient.class);
    public static final long MAX_WAIT_MILLIS = 30 * 1000L;
    public static final Pattern STATUSLINE_PATTERN = Pattern.compile("^:(\\S+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S.*)$");
    public static final Pattern COMMANDLINE_PATTERN = Pattern.compile("^(?!:)(\\S+)(?:|\\s+(\\S.*))$");
    public static final Pattern CHANNELMSG_PATTERN = Pattern.compile("^:([^! ]+)!\\S+\\.tmi\\.twitch\\.tv\\s+PRIVMSG\\s+(#\\S+)\\s+:(.+)$");
    public static final long MAX_INACTIVITY_TIME = 6 * 60 * 1000L; // twitch sends out a ping every 5 mins
    public static final long CONNECTION_CHECK_IVAL = 60 * 1000L;

    private final Socket socket = new Socket();
    private final String botname;
    private final String password;
    private final Set<IRCStatusListener> statusListeners;
    private final Set<IRCCommandListener> commandListeners;
    private final Set<IRCChannelMessageListener> channelMessageListeners;
    private final Set<String> joinedChannels; // for reconnect
    private Thread reader;
    private final AtomicLong lastActivityDetected = new AtomicLong(-1L);

    public TwitchIRCClient(String botname, String password) {
        this.statusListeners = Collections.synchronizedSet(new HashSet<>());
        this.commandListeners = Collections.synchronizedSet(new HashSet<>());
        this.channelMessageListeners = Collections.synchronizedSet(new HashSet<>());
        this.joinedChannels = Collections.synchronizedSet(new HashSet<>());
        this.botname = botname;
        this.password = password;
    }
    
    public boolean isConnected() {
        if (!reader.isAlive()) {
            return false;
        }
        return System.currentTimeMillis() - lastActivityDetected.get() <= MAX_INACTIVITY_TIME;
    }

    @Override
    public void run() {

        try {
            InputStream instr = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(instr));
            
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                log.info("< " + line);
                lastActivityDetected.set(System.currentTimeMillis());

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
            log.error("Exception while reading socket:", e);
        }

        try {
            socket.close();
        } catch (IOException e) {
            log.error("Exception while closing telnet:", e);
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
        
        socket.connect(new InetSocketAddress("irc.twitch.tv", 6667), 10000);
        reader = new Thread(this, "Twitch IRC Reader");
        reader.start();

        // auth
        sendNoLog("PASS " + password);
        sendAndWait("NICK " + botname.toLowerCase(), 376);
        log.info("login to twitch irc successful");
    }

    public void shutdown() {
        if (reader.isAlive()) {
            reader.interrupt();
            try {
                reader.join();
            } catch (InterruptedException ex) {
                log.error("", ex);
            }
        }
        try {
            if (socket.isConnected()) {
                socket.close();
            }
        } catch (IOException ex) {
            log.error("", ex);
        }
    }

    public void joinChannel(String channel, TwitchChatListener listener) throws IOException {

        final String ircChannelName = "#" + channel.toLowerCase();

        sendAndWait("JOIN " + ircChannelName, 366);

        if (listener != null) {
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

        joinedChannels.add(channel);

    }

    // only for testing
    private void loop() {
        try {
            reader.join();
        } catch (InterruptedException ex) {
            log.error("", ex);
        }
    }

    public void sendNoLog(String cmd) throws IOException {
        IOUtils.write(cmd + LF, socket.getOutputStream());
        socket.getOutputStream().flush();
    }

    public void send(String cmd) throws IOException {
        log.info("> " + cmd);
        IOUtils.write(cmd + LF, socket.getOutputStream());
        socket.getOutputStream().flush();
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

}