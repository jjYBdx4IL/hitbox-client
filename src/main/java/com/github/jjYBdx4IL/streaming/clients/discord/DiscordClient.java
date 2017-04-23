package com.github.jjYBdx4IL.streaming.clients.discord;

import java.awt.Image;
import java.awt.TrayIcon;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.security.auth.login.LoginException;
import javax.swing.SwingUtilities;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jjYBdx4IL.streaming.clients.RunThemAllMain;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.PrivateChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.entities.VoiceStatus;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.events.voice.GenericVoiceEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;
import net.dv8tion.jda.requests.WebSocketCustomHandler;

public class DiscordClient extends ListenerAdapter implements Closeable {

	private static final Logger LOG = LoggerFactory.getLogger(DiscordClient.class);

	protected final DiscordConfig config;
	protected JDA jda = null;
	private volatile boolean currentVoiceStatus = true;
	private final TrayIcon trayIcon;
	private final Image micOn;
	private final Image micOff;
	private final DiscordVoiceStatusFrame statusFrame = new DiscordVoiceStatusFrame();

	public DiscordClient(TrayIcon trayIcon) throws FileNotFoundException, IOException {
		this.trayIcon = trayIcon;
		this.config = new DiscordConfig();
		this.config.read();
		micOn = ImageIO.read(RunThemAllMain.class.getResourceAsStream("mic-on.png"));
		micOff = ImageIO.read(RunThemAllMain.class.getResourceAsStream("mic-off.png"));
	}

	public void start() throws LoginException, IllegalArgumentException, InterruptedException {
		jda = new JDABuilder().setAudioEnabled(false).setBotToken(config.botToken).buildBlocking();
		
		if (LOG.isTraceEnabled()) {
			((JDAImpl) jda).getClient().setCustomHandler(new WebSocketCustomHandler() {

				@Override
				public boolean handle(JSONObject obj) {
					LOG.trace(obj.toString());
					return false;
				}
			});
		}

		for (User user : jda.getUsers()) {
			LOG.info(user.toString());
		}
		for (Guild guild : jda.getGuilds()) {
			LOG.info(guild.toString());
		}
		for (PrivateChannel channel : jda.getPrivateChannels()) {
			LOG.info(channel.toString());
		}
		for (TextChannel channel : jda.getTextChannels()) {
			LOG.info(channel.toString());
			for (User user : channel.getUsers()) {
				LOG.info(" \\- " + user.toString());
			}
		}
		for (VoiceChannel channel : jda.getVoiceChannels()) {
			LOG.info(channel.toString());
			for (User user : channel.getUsers()) {
				LOG.info(" \\- " + user.toString());
			}
		}
		
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				statusFrame.go();
			}
		});
		
		updateCurrentVoiceStatus();
		
		jda.addEventListener(this);
		
	}
	
	private void updateCurrentVoiceStatus() {
		if (config.displayVoiceStatusOfUser == null) {
			return;
		}
		
		VoiceStatus vs = getVoiceStatus();
		LOG.debug("inVoiceChannel: " + vs.inVoiceChannel());
		LOG.debug("isDeaf: " + vs.isDeaf());
		LOG.debug("isMuted: " + vs.isMuted());
		
		currentVoiceStatus = vs.inVoiceChannel() && !vs.isDeaf();
		
		// update tray icon to reflect voice status
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				Image desiredImage = currentVoiceStatus ? micOn : micOff;
				if (trayIcon.getImage() != desiredImage) {
					LOG.info("new voice status: " + currentVoiceStatus);
					trayIcon.setImage(desiredImage);
				}
			}
			
		});
		
		// update discord status display frame
		SwingUtilities.invokeLater(new Runnable(){

			@Override
			public void run() {
				if (currentVoiceStatus) {
					statusFrame.restart();
				} else {
					statusFrame.stop();
				}
			}
			
		});
	}
	
	@Override
	public void close() {
		jda.shutdown();
	}
	
	@Override
	public void onGenericVoice(GenericVoiceEvent e) {
		if (config.displayVoiceStatusOfUser == null) {
			return;
		}
		if (!e.getUser().getId().equals(config.displayVoiceStatusOfUser)) {
			return;
		}
		updateCurrentVoiceStatus();
	}
	
	private VoiceStatus getVoiceStatus() {
		for (Guild g : jda.getGuilds()) {
			User u = g.getUserById(config.displayVoiceStatusOfUser);
			if (u == null) {
				continue;
			}
			VoiceStatus vs = g.getVoiceStatusOfUser(u);
			if (vs != null) {
				return vs;
			}
		}
		return null;
	}
}
