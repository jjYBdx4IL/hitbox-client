package com.github.jjYBdx4IL.streaming.clients;

/**
 *
 * @author jjYBdx4IL
 */
public interface ConnectionListenerHandler {
    
    void addConnectionListener(ConnectionListener listener);
    void removeConnectionListener(ConnectionListener listener);
    
}
