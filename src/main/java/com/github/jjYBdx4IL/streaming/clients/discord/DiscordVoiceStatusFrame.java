package com.github.jjYBdx4IL.streaming.clients.discord;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.github.jjYBdx4IL.streaming.clients.graphics.AnimController;
import com.github.jjYBdx4IL.streaming.clients.graphics.TextWithOutlineBlinkingEffectPanel;
import com.github.jjYBdx4IL.streaming.clients.graphics.TextWithOutlineBlinkingEffectPanel.BlinkMode;

public class DiscordVoiceStatusFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	public static final String FRAME_ID = "DISCORD VOICE STATUS FRAME";

	private TextWithOutlineBlinkingEffectPanel blinkPanel = null;
	private AnimController animController = null;

	public DiscordVoiceStatusFrame() {
		super(FRAME_ID);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	public void go() {
		if (!SwingUtilities.isEventDispatchThread()) {
			throw new RuntimeException("not on EDT");
		}
		if (animController != null) {
			return;
		}
		blinkPanel = new TextWithOutlineBlinkingEffectPanel("DISCORD NOW ONLINE!", BlinkMode.PULSING);
		getContentPane().add(blinkPanel);
		pack();
		setVisible(true);

		animController = new AnimController(30f);
		animController.register(blinkPanel);
	}

	public void stop() {
		blinkPanel.stop();
	}
	
	public void restart() {
		blinkPanel.restart();
	}
}
