package com.github.jjYBdx4IL.streaming.clients.fma;

/**
 *
 * @author jjYBdx4IL
 */

class FMATrack {
    public Long track_id;
    public Long album_id;
    public String album_title;
    public Long artist_id;
    public String artist_name;
    public String track_file_url;

    @Override
    public String toString() {
        return "FMATrack{" + "track_id=" + track_id + ", album_id=" + album_id + ", album_title=" + album_title + ", artist_id=" + artist_id + ", artist_name=" + artist_name + ", track_file_url=" + track_file_url + '}';
    }
    
}
