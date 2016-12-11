package com.github.jjYBdx4IL.streaming.clients.discord;

import static org.junit.Assert.assertFalse;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;

import org.junit.Assume;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jjYBdx4IL.utils.env.CI;

public class DiscordVoiceStatusFrameTest {
	
	private static final Logger LOG = LoggerFactory.getLogger(DiscordVoiceStatusFrameTest.class);
	
	DiscordVoiceStatusFrame frame;
	final AtomicBoolean windowClosed = new AtomicBoolean(false);
	volatile boolean edtError = false;
	
    @Test
    public void test() throws IOException, URISyntaxException, InterruptedException, InvocationTargetException {
        Assume.assumeFalse(CI.isCI());
        
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                	frame = new DiscordVoiceStatusFrame();
                    
                    frame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                            synchronized (windowClosed) {
                                windowClosed.set(true);
                                windowClosed.notify();
                            }
                        }
                    });
                    
                    frame.go();
                } catch (Exception ex) {
                    edtError = true;
                    throw new RuntimeException(ex);
                }
            }
        });
        waitForWindowClosing();
        assertFalse(edtError);
    }
    
    protected void waitForWindowClosing() throws InterruptedException {
        synchronized (windowClosed) {
            while (!windowClosed.get()) {
                LOG.trace("waitForWindowClosing(): wait for windowClosed");
                windowClosed.wait(1000L);
            }
        }
    }
}
