package com.github.jjYBdx4IL.streaming.clients.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Locale;

public class TextWithOutlineBlinkingEffectPanel extends AnimPainterAdapterPanel {

	private static final long serialVersionUID = 1L;

	public static enum BlinkMode {
		DISCRETE, PULSING;
	}

	private final String text;
	private Font font;
	private Color fillColor = Color.BLACK;
	private Color outlineColor = Color.WHITE;
	private Color bgColor = Color.GREEN;
	private final BlinkMode blinkMode;
	private volatile boolean paused = false;

	public TextWithOutlineBlinkingEffectPanel(String text) {
		this(text, BlinkMode.DISCRETE);
	}

	public TextWithOutlineBlinkingEffectPanel(String text, BlinkMode blinkMode) {
		super();
		setBackground(bgColor);
		this.text = text;
		this.font = new Font("Arial", Font.BOLD, 10);
		this.blinkMode = blinkMode;
	}

	public Dimension getPreferredSize() {
		return new Dimension(250, 200);
	}
	
	public void stop() {
		this.paused = true;
	}
	
	public void restart() {
		this.paused = false;
	}

	public void paintComponent(Graphics g1) {
		super.paintComponent(g1);
		
		if (paused) {
			return;
		}
		
		Graphics2D g = (Graphics2D) g1;

		Font bigfont = font.deriveFont(AffineTransform.getScaleInstance(2.0, 2.0));
		GlyphVector gv = bigfont.createGlyphVector(g.getFontRenderContext(),
				String.format(Locale.ROOT, "%s - %d - %.3f", text, getFrame(), getSecs()));

		Rectangle2D bounds = gv.getLogicalBounds();
		g.setTransform(AffineTransform.getTranslateInstance(bounds.getX(), bounds.getHeight()));

		Color effectiveOutlineColor;
		Color effectiveFillColor;

		switch (blinkMode) {
		case PULSING:
			int alpha = (int) (191 + 64 * Math.cos(getSecs()));
			effectiveOutlineColor = new Color((outlineColor.getRGB() & 0xFFFFFF) | (alpha << 24), true);
			effectiveFillColor = new Color((fillColor.getRGB() & 0xFFFFFF) | (alpha << 24), true);
			break;
		default:
			effectiveOutlineColor = ((int) getSecs()) % 2 == 0 ? outlineColor : bgColor;
			effectiveFillColor = ((int) getSecs()) % 2 == 0 ? fillColor : bgColor;
		}

		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setStroke(new BasicStroke(1.0f));
		for (int i = 0; i < gv.getNumGlyphs(); i++) {
			Shape s = gv.getGlyphOutline(i);
			g.setPaint(effectiveFillColor);
			g.fill(s);
			g.setPaint(effectiveOutlineColor);
			g.draw(s);
		}
	}

}
