/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jjYBdx4IL.streaming.clients.discord;

import com.github.jjYBdx4IL.streaming.clients.GenericConfig;
import com.github.jjYBdx4IL.utils.env.Surefire;
import java.io.IOException;
import java.util.Locale;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.PrivateChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.events.Event;
import net.dv8tion.jda.events.voice.GenericVoiceEvent;
import net.dv8tion.jda.events.voice.VoiceJoinEvent;
import net.dv8tion.jda.events.voice.VoiceLeaveEvent;
import net.dv8tion.jda.events.voice.VoiceSelfDeafEvent;
import net.dv8tion.jda.events.voice.VoiceSelfMuteEvent;
import net.dv8tion.jda.hooks.EventListener;
import net.dv8tion.jda.requests.WebSocketCustomHandler;
import net.dv8tion.jda.utils.SimpleLog;

import org.json.JSONObject;
import org.junit.Assume;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jjYBdx4IL
 */
public class DiscordAPPTest {

    private static final Logger LOG = LoggerFactory.getLogger(DiscordAPPTest.class);

    @Test
    public void testSomeMethod() throws IOException, LoginException, IllegalArgumentException, InterruptedException {
        Assume.assumeTrue(Surefire.isSingleTextExecution());
        
        SimpleLog.getLog("JDARequester").setLevel(SimpleLog.LEVEL.ALL);
        
        GenericConfig config = new GenericConfig();
        config.read();

        JDA jda = new JDABuilder().setAudioEnabled(false).setBotToken(config.discordBotToken).buildBlocking();
        ((JDAImpl)jda).getClient().setCustomHandler(new WebSocketCustomHandler() {
			
			@Override
			public boolean handle(JSONObject obj) {
				LOG.info(obj.toString());
				return false;
			}
		});

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
        }
        for (VoiceChannel channel : jda.getVoiceChannels()) {
            LOG.info(channel.toString());
            for (User user : channel.getUsers()) {
                LOG.info(" \\- " + user.toString());
            }
        }
        jda.addEventListener(new EventListener() {
            @Override
            public void onEvent(Event event) {
                LOG.info(event.toString());
                if (event instanceof VoiceJoinEvent) {
                    VoiceJoinEvent e = (VoiceJoinEvent) event;
                    LOG.info(String.format(Locale.ROOT, "%s joined %s", e.getUser().toString(), e.getGuild().toString()));
                }
                if (event instanceof VoiceLeaveEvent) {
                    VoiceLeaveEvent e = (VoiceLeaveEvent) event;
                    LOG.info(String.format(Locale.ROOT, "%s left %s", e.getUser().toString(), e.getGuild().toString()));
                }
                if (event instanceof VoiceSelfDeafEvent) {
                	VoiceSelfDeafEvent e = (VoiceSelfDeafEvent) event;
                    LOG.info(String.format(Locale.ROOT, "%s self-deafened %s", e.getUser().toString(), e.getGuild().toString()));
                    
                }
                if (event instanceof VoiceSelfMuteEvent) {
                	VoiceSelfMuteEvent e = (VoiceSelfMuteEvent) event;
                    LOG.info(String.format(Locale.ROOT, "%s self-muted %s", e.getUser().toString(), e.getGuild().toString()));
                }
                if (event instanceof GenericVoiceEvent) {
                	GenericVoiceEvent e = (GenericVoiceEvent) event;
	                LOG.info("inVoiceChannel: " + jda.getGuilds().get(0).getVoiceStatusOfUser(e.getUser()).inVoiceChannel());
	                LOG.info("isDeaf: " + jda.getGuilds().get(0).getVoiceStatusOfUser(e.getUser()).isDeaf());
	                LOG.info("isMuted: " + jda.getGuilds().get(0).getVoiceStatusOfUser(e.getUser()).isMuted());
                }
            }
        });
        Thread.sleep(60000L);
        jda.shutdown();

    }

}
