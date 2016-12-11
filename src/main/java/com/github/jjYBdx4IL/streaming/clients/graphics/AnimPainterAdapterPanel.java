package com.github.jjYBdx4IL.streaming.clients.graphics;

import javax.swing.JPanel;

public abstract class AnimPainterAdapterPanel extends JPanel implements IAnimPainter {
	private static final long serialVersionUID = 1L;

	public int getFrame() {
		return frame;
	}

	private void setFrame(int frame) {
		this.frame = frame;
	}

	public float getSecs() {
		return secs;
	}

	private void setSecs(float secs) {
		this.secs = secs;
	}

	private int frame;
	private float secs;

	@Override
	public void paintFrame(int frame, float secs) {
		setFrame(frame);
		setSecs(secs);
		repaint();
	}

}
