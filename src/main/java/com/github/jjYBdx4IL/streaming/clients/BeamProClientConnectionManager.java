package com.github.jjYBdx4IL.streaming.clients;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.beam.api.BeamAPI;
import pro.beam.api.resource.BeamUser;
import pro.beam.api.resource.chat.BeamChat;
import pro.beam.api.resource.chat.events.IncomingMessageEvent;
import pro.beam.api.resource.chat.events.UserJoinEvent;
import pro.beam.api.resource.chat.methods.AuthenticateMessage;
import pro.beam.api.resource.chat.methods.ChatSendMethod;
import pro.beam.api.resource.chat.replies.AuthenticationReply;
import pro.beam.api.resource.chat.replies.ReplyHandler;
import pro.beam.api.resource.chat.ws.BeamChatConnectable;
import pro.beam.api.services.impl.ChatService;
import pro.beam.api.services.impl.UsersService;

/**
 *
 * @author jjYBdx4IL
 */
public class BeamProClientConnectionManager extends ConnectionManager {

    private static final Logger LOG = LoggerFactory.getLogger(BeamProClientConnectionManager.class);
    private BeamAPI client = null;
    private boolean gameUpdated = false;

    public BeamProClientConnectionManager(GenericConfig config) {
        super(config);
    }

    @Override
    public void reconnect() {
        LOG.info("(re)connect");
        notifyReconnect();

        if (client != null) {
            //client.shutdown();
            client = null;
        }

        try {
            BeamProConfig config = new BeamProConfig();
            config.read();
            client = new BeamAPI(config.chatToken);
            BeamUser user = client.use(UsersService.class).getCurrent().get();
            BeamChat chat = client.use(ChatService.class).findOne(user.channel.id).get();
            BeamChatConnectable chatConnectable = chat.connectable(client);
            chatConnectable.connect();

            if (chatConnectable.connect()) {
                chatConnectable.send(AuthenticateMessage.from(user.channel, user, chat.authkey), new ReplyHandler<AuthenticationReply>() {
                    public void onSuccess(AuthenticationReply reply) {
                        chatConnectable.send(ChatSendMethod.of("Hello World!"));
                    }

                    public void onFailure(Throwable var1) {
                        LOG.error("", var1);
                    }
                });
                notifyConnected();
            }

            chatConnectable.on(IncomingMessageEvent.class, event -> {
                if (event.data.message.message.get(0).text.startsWith("!ping")) {
                    chatConnectable.send(ChatSendMethod.of(String.format("@%s PONG!", event.data.userName)));
                }
            });

            chatConnectable.on(UserJoinEvent.class, event -> {
                chatConnectable.send(ChatSendMethod.of(
                        String.format("Hi %s! I'm pingbot! Write !ping and I will pong back!",
                                event.data.username)));
            });

//            client.joinChannel(config.chatToken, new TwitchChatListener() {
//                @Override
//                public void onChatMessage(String from, String message) {
//                    LOG.info(from + ": " + message);
//                    for (ChatListener listener : getChatListeners()) {
//                        listener.onChatMessage(from, message);
//                    }
//                }
//            });
        } catch (IOException | InterruptedException | ExecutionException ex) {
            LOG.error("", ex);
        }
    }

    @Override
    public boolean isConnected() {
        return client != null;// && client.isConnected();
    }

}
