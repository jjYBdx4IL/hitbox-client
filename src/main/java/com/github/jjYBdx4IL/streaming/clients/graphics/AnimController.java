package com.github.jjYBdx4IL.streaming.clients.graphics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class AnimController implements ActionListener {

	private final float fps;
	private final int delay;
	private final Set<IAnimPainter> painters;
	private int frame = 0;
	private final Timer timer;
	private final long started;
	private int framesSkipped = 0;

	public AnimController(float fps) {
		this.fps = fps;
		this.painters = new HashSet<>();
		this.timer = new Timer(0, this);
		this.timer.setInitialDelay(0);
		this.timer.setCoalesce(false); // we do this on our own bec we need to
										// keep track
		this.delay = (int) (1000 / fps);
		this.timer.setDelay(this.delay);
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				timer.start();
			}
		});
		this.started = System.currentTimeMillis();
	}

	public void stop() {
		assertEDT();
		timer.stop();
	}

	public void register(IAnimPainter painter) {
		assertEDT();
		this.painters.add(painter);
	}

	private void assertEDT() {
		if (!SwingUtilities.isEventDispatchThread()) {
			throw new RuntimeException("not on EDT");
		}
	}

	public int getSkippedFrames() {
		return framesSkipped;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final long elapsed = System.currentTimeMillis() - started;
		final int frameByTime = (int) Math.floor(fps * elapsed / 1000d);
		
		// skip any frames?
		if (frameByTime > frame) {
			framesSkipped += frameByTime - frame;
			frame = frameByTime;
		}
		
		if (frameByTime == frame) {
			for (IAnimPainter painter : this.painters) {
				painter.paintFrame(frame, elapsed / 1000f);
			}
			frame++;
		}
		
		final long nextFrameStartTime = started + (long) (frame / fps * 1000f);
		long waitMillis = nextFrameStartTime - System.currentTimeMillis();
		if (waitMillis < 1L) {
			waitMillis = 1L;
		}
		timer.setInitialDelay((int) waitMillis);
		timer.restart();
	}
}
