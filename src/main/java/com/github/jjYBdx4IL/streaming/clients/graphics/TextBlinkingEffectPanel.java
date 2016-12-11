package com.github.jjYBdx4IL.streaming.clients.graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Locale;

public class TextBlinkingEffectPanel extends AnimPainterAdapterPanel {

	private static final long serialVersionUID = 1L;

	public static enum BlinkMode {
		DISCRETE, PULSING;
	}

	private final String text;
	private Font font;
	private Color color = Color.BLACK;
	private Color bgColor = Color.GREEN;
	private final BlinkMode blinkMode;

	public TextBlinkingEffectPanel(String text) {
		this(text, BlinkMode.DISCRETE);
	}

	public TextBlinkingEffectPanel(String text, BlinkMode blinkMode) {
		super();
		setBackground(bgColor);
		this.text = text;
		this.font = new Font("Arial", Font.BOLD, 10);
		this.blinkMode = blinkMode;
	}

	public Dimension getPreferredSize() {
		return new Dimension(250, 200);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		g.setFont(font);
		switch (blinkMode) {
		case PULSING:
			break;
		default:
			g.setColor(((int) getSecs()) % 2 == 0 ? color : bgColor);
		}
		g.drawString(String.format(Locale.ROOT, "%s - %d - %.3f", text, getFrame(), getSecs()), 10, 20);
	}

}
