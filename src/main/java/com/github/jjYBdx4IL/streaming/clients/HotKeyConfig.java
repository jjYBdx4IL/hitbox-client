package com.github.jjYBdx4IL.streaming.clients;

import com.tulskiy.keymaster.common.HotKey;
import com.tulskiy.keymaster.common.HotKeyListener;
import com.tulskiy.keymaster.common.Provider;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Locale;
import java.util.logging.Level;
import javax.swing.KeyStroke;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jjYBdx4IL
 */
public class HotKeyConfig implements HotKeyListener, Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(HotKeyConfig.class);
    public static final String VLC_EXE = "C:\\Program Files (x86)\\VideoLAN\\VLC\\vlc.exe";
    public static final KeyStroke CTRL_NUMPAD_7 = KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD7, InputEvent.CTRL_MASK);

    public static final String VLC_HOSTNAME = "localhost";
    public static final int VLC_PORT = 4444;
    public static final int CONNECT_WAIT_MILLIS = 10000;
    public static final String LF = "\r\n";

    private final Socket socket = new Socket();
    private Thread reader;
    private Process vlc;

    public HotKeyConfig() {
    }

    public void init() throws IOException {
        Provider provider = Provider.getCurrentProvider(false);
        provider.register(CTRL_NUMPAD_7, this);

        // start the "non-GUI", remote-controlled VLC console minimized
        //ProcessBuilder pb = new ProcessBuilder("cmd.exe", String.format(Locale.ROOT, "/K START /B /MIN \"\" \"%s\" --rc-host=%s:%d -I rc", VLC_EXE, VLC_HOSTNAME, VLC_PORT));
        ProcessBuilder pb = new ProcessBuilder("cmd.exe", String.format(Locale.ROOT, "/K START /B \"\" \"%s\" --no-video-title-show --rc-host=%s:%d -I rc", VLC_EXE, VLC_HOSTNAME, VLC_PORT));
        vlc = pb.start();

        socket.connect(new InetSocketAddress(VLC_HOSTNAME, VLC_PORT), CONNECT_WAIT_MILLIS);
        reader = new Thread(this, "VLC RC Reader");
        reader.start();
    }

    @Override
    public void onHotKey(HotKey hotkey) {
        LOG.info("hotkey pressed: " + hotkey.toString());
        try {
            if (CTRL_NUMPAD_7.equals(hotkey.keyStroke)) {
                playback("C:\\Users\\mark\\Downloads\\bad_sketch_apology.mp4");
            }
        } catch (IOException ex) {
            LOG.error("", ex);
        }
        LOG.debug("done");
    }

    public synchronized void sendVLCCmd(String cmd) throws IOException {
        LOG.info("> " + cmd);
        IOUtils.write(cmd + LF, socket.getOutputStream());
        socket.getOutputStream().flush();
    }

    /**
     *
     * @param localFile path to the media file to play back
     * @param volume from 0 to 1024
     */
    public void playback(String localFile, int volume) throws IOException {
        sendVLCCmd("add " + localFile);
        if (volume >= 0) {
            // setting the volume only works after the media file has started playing
            sendVLCCmd("volume " + volume);
        }
    }

    public void playback(String localFile) throws IOException {
        playback(localFile, -1);
    }

    @Override
    public void run() {

        try {
            InputStream instr = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(instr));

            for (String line = br.readLine(); line != null; line = br.readLine()) {
                LOG.info("< " + line);

//                Matcher m = STATUSLINE_PATTERN.matcher(line);
//                if (m.find()) {
//                    for (ITwitchIRCListener listener : this.listeners) {
//                        listener.onStatusLineReceived(m.group(1), Integer.valueOf(m.group(2)), m.group(3), m.group(4));
//                    }
//                    continue;
//                }
//
//                m = COMMANDLINE_PATTERN.matcher(line);
//                if (m.find()) {
//                    for (ITwitchIRCListener listener : this.listeners) {
//                        listener.onCommandReceived(m.group(1), m.group(2));
//                    }
//                }
//
//                m = CHANNELMSG_PATTERN.matcher(line);
//                if (m.find()) {
//                    for (ITwitchIRCListener listener : this.listeners) {
//                        listener.onChannelMessageReceived(m.group(1), m.group(2), m.group(3));
//                    }
//                }
            }
        } catch (IOException e) {
            LOG.error("Exception while reading socket:", e);
        }

        try {
            socket.close();
        } catch (IOException e) {
            LOG.error("Exception while closing telnet:", e);
        }
    }

}
