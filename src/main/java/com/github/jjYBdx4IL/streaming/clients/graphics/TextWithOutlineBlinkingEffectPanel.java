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
import java.awt.image.BufferedImage;

public class TextWithOutlineBlinkingEffectPanel extends AnimPainterAdapterPanel {

	private static final long serialVersionUID = 1L;

	public static enum BlinkMode {
		DISCRETE, PULSING;
	}

	private final String text;
	private Font font;
	private Color fillColor = Color.WHITE;
	private Color outlineColor = Color.BLACK;
	private Color bgColor = Color.GREEN;
	private final BlinkMode blinkMode;
	private volatile boolean paused = false;
	private static final int BORDER_SIZE = 10;
	private boolean useAlpha = false;
	private float outlineThickness = 0f;

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
		BufferedImage output = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) output.getGraphics();
		Font bigfont = font.deriveFont(AffineTransform.getScaleInstance(2.0, 2.0));
		GlyphVector gv = bigfont.createGlyphVector(g.getFontRenderContext(), text);
		Rectangle2D bounds = gv.getLogicalBounds();
		return new Dimension((int) bounds.getWidth() + 2 * BORDER_SIZE, (int) bounds.getHeight() + 2 * BORDER_SIZE);
	}

	public void stop() {
		this.paused = true;
	}

	public void restart() {
		this.paused = false;
	}

	private int t(int input) {
		int value = (int) ((input * 3 / 4) + 64 * Math.cos(getSecs()));
		if (value < 0) {
			value = 0;
		}
		else if (value > 255) {
			value = 255;
		}
		return value;
	}

	public void paintComponent(Graphics g1) {
		super.paintComponent(g1);

		if (paused) {
			return;
		}

		Graphics2D g2 = (Graphics2D) g1;

		Font bigfont = font.deriveFont(AffineTransform.getScaleInstance(2.0, 2.0));
		GlyphVector gv = bigfont.createGlyphVector(g2.getFontRenderContext(), text);

		Rectangle2D bounds = gv.getLogicalBounds();
		g2.setTransform(
				AffineTransform.getTranslateInstance(bounds.getX() + BORDER_SIZE, bounds.getHeight() + BORDER_SIZE));

		Color effectiveOutlineColor;
		Color effectiveFillColor;

		switch (blinkMode) {
		case PULSING:
			int a = useAlpha ? t(255) : 255;
			int r = t(outlineColor.getRed());
			int g = t(outlineColor.getGreen());
			int b = t(outlineColor.getBlue());
			effectiveOutlineColor = new Color(r, g, b, a);
			r = t(fillColor.getRed());
			g = t(fillColor.getGreen());
			b = t(fillColor.getBlue());
			effectiveFillColor = new Color(r, g, b, a);
			break;
		default:
			effectiveOutlineColor = ((int) getSecs()) % 2 == 0 ? outlineColor : bgColor;
			effectiveFillColor = ((int) getSecs()) % 2 == 0 ? fillColor : bgColor;
		}

		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

		if (outlineThickness > 0f) {
			g2.setStroke(new BasicStroke(outlineThickness));
		}
		for (int i = 0; i < gv.getNumGlyphs(); i++) {
			Shape s = gv.getGlyphOutline(i);
			g2.setPaint(effectiveFillColor);
			g2.fill(s);
			if (outlineThickness > 0f) {
				g2.setPaint(effectiveOutlineColor);
				g2.draw(s);
			}
		}
	}

}
