package com.github.jjYBdx4IL.streaming.clients;

/**
 *
 * @author jjYBdx4IL
 */
public interface FollowerListener {
    
    void onFollow(String name);
    void onUnfollow(String name);
    
}
