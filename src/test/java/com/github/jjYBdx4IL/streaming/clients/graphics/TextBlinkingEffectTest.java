package com.github.jjYBdx4IL.streaming.clients.graphics;

import static org.junit.Assert.assertFalse;

import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.junit.Assume;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jjYBdx4IL.streaming.clients.graphics.TextWithOutlineBlinkingEffectPanel.BlinkMode;
import com.github.jjYBdx4IL.utils.env.Surefire;

public class TextBlinkingEffectTest implements IAnimPainter {
	
    private static final Logger LOG = LoggerFactory.getLogger(TextBlinkingEffectTest.class);
    
    private final AtomicBoolean windowClosed = new AtomicBoolean(false);
    private volatile boolean edtError = false;
    JFrame frame = null;
    TextBlinkingEffectPanel tbep1;
    TextWithOutlineBlinkingEffectPanel tbep2;
    final AnimController animController = new AnimController(30f);
    
    @Test
    public void test() throws IOException, URISyntaxException, InterruptedException, InvocationTargetException {
        Assume.assumeTrue(Surefire.isSingleTextExecution());
        
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    // Create Panels
                    JPanel panel = new JPanel();
                    panel.setLayout(new GridLayout(2, 1));
                    tbep1 = new TextBlinkingEffectPanel("some text");
                    panel.add(tbep1);
                    tbep2 = new TextWithOutlineBlinkingEffectPanel("maybe more words", BlinkMode.PULSING);
                    panel.add(tbep2);
                    
                    animController.register(tbep1);
                    animController.register(tbep2);
                    animController.register(TextBlinkingEffectTest.this);

                    frame = new JFrame("Embedded Effects Viewer");
                    frame.getContentPane().add(panel);
                    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    frame.setSize(1024, 800);
                    frame.setVisible(true);
                    
                    frame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                        	animController.stop();
                            synchronized (windowClosed) {
                                windowClosed.set(true);
                                windowClosed.notify();
                            }
                        }
                    });
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
    
	@Override
	public void paintFrame(int frame, float timeSecs) {
		if (frame >= 1500) {
			animController.stop();
			LOG.info("skipped frames: " + animController.getSkippedFrames());
			this.frame.dispatchEvent(new WindowEvent(this.frame, WindowEvent.WINDOW_CLOSING));
		}
	}
}
