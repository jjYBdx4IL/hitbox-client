package com.github.jjYBdx4IL.streaming.clients.twitch.api;

/**
 *
 * @author Github jjYBdx4IL Projects
 */
public class Channel extends TwitchDTO {

    public String status;
    public String game;
//    public boolean mature;
//    public String broadcaster_language;
//    public String language;
//    public String display_name;
//    public long _id;
//    public String name;
//    public String created_at;
//    public String updated_at;
//    public String logo;
//    public boolean partner;
//    public String url;
//    public long views;
//    public long followers;

    @Override
    public String toString() {
        return "Channel{" + "status=" + status + ", game=" + game + '}';
    }


}
