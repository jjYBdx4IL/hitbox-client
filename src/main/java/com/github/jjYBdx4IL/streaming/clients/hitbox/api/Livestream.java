package com.github.jjYBdx4IL.streaming.clients.hitbox.api;

/**
 *
 * @author Github jjYBdx4IL Projects
 */
public class Livestream {

    public String media_status; // channel title
    public String media_user_name;
    public long media_id;
    public long media_category_id; // game title
    public long media_live_delay;
    public long media_hidden;
    public long media_mature;
    public long media_recording;
    public String[] media_countries;

    @Override
    public String toString() {
        return "Livestream{" + "media_status=" + media_status + ", media_user_name=" + media_user_name + ", media_id=" + media_id + ", media_category_id=" + media_category_id + ", media_live_delay=" + media_live_delay + ", media_hidden=" + media_hidden + ", media_mature=" + media_mature + ", media_recording=" + media_recording + ", media_countries=" + media_countries + '}';
    }

}
