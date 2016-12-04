package com.github.jjYBdx4IL.streaming.clients.hitbox.api;

/**
 *
 * @author Github jjYBdx4IL Projects
 */
public class Reply {
    public Livestream[] livestream;
    public Category category;
    public Category[] categories;

    @Override
    public String toString() {
        return "Reply{" + "livestream=" + livestream + ", category=" + category + ", categories=" + categories + '}';
    }

}
