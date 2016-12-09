package com.github.jjYBdx4IL.streaming.clients.twitch;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.logging.Level;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jjYBdx4IL.utils.awt.Desktop;

/**
 *
 * @author jjYBdx4IL
 */
public class TwitchAPP extends AbstractHandler {

    private static final Logger LOG = LoggerFactory.getLogger(TwitchAPP.class);

    public static final String APP_NAME = "streaming-clients";
    public static final String APP_ID = "cx0mzcuj3o7ue0k6fho7sjo6qkwjwm";
    public static final String REDIRECT_URI = "http://localhost:11723";
    private static final long MAX_WAIT_MILLIS = 120 * 1000L;
    
    private volatile String code = null;
    private volatile boolean requestReceived = false;

    public URI getOauthURI() throws URISyntaxException {
        URIBuilder ub = new URIBuilder("https://api.twitch.tv/kraken/oauth2/authorize");
        ub.setParameter("response_type", "code");
        ub.setParameter("client_id", APP_ID);
        ub.setParameter("redirect_uri", REDIRECT_URI);
        ub.setParameter("scope", StringUtils.join(Scope.values(), " "));
        ub.setParameter("state", Long.toString(System.currentTimeMillis()));
        ub.setParameter("force_verify", "true");
        return ub.build();
    }

    public String getOauthToken() {
        Server server = null;
        try {
            int dialogResult = JOptionPane.showConfirmDialog(null, "Give this app full authorization to your Twitch account?", "Warning", JOptionPane.YES_NO_OPTION);
            if (dialogResult != JOptionPane.YES_OPTION) {
                return null;
            }
            
            server = new Server(new InetSocketAddress("localhost", 11723));
            server.setHandler(this);
            server.start();
            Desktop.browse(getOauthURI());
            waitForRequestReceived();
        } catch (Exception ex) {
        	LOG.warn("", ex);
		} finally {
            if (server != null) {
                try {
                    server.stop();
                } catch (Exception ex) {
                    LOG.warn("", ex);
                }
            }
        }
        return code;
    }
    
    private synchronized void waitForRequestReceived() {
    	long abortAt = System.currentTimeMillis() + MAX_WAIT_MILLIS;
    	while (!requestReceived && System.currentTimeMillis() < abortAt) {
    		long waitTime = abortAt - System.currentTimeMillis();
    		if (waitTime <= 0) {
    			waitTime = 1L;
    		}
    		try {
				wait(waitTime);
			} catch (InterruptedException e) {
			}
    	}
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        LOG.info(String.format(Locale.ROOT, "handle(%s, ...)", target));
        synchronized(this) {
        	code = request.getParameter("code");
        	LOG.info("received oauth token "+code);
        	requestReceived = true;
        	notifyAll();
        }
        response.setStatus(200);
        response.setContentType("text/html");
        response.getWriter().print("<html><body><h2>Done.</h2></html>");
        baseRequest.setHandled(true);
    }

}
