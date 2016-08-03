package com.example.tijingwang.sunnyrainy;

/**
 * Created by netdong on 8/2/16.
 */
public class Song {
    public int song_id;
    public String title;
    public String artist;
    public int duration;

    public Song(int song_id, String title, String artist, int duration) {
        this.song_id = song_id;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
    }

    public String getDurationString() {
        int mins = duration / 60;
        int secs = duration % 60;
        String s = "" + mins + ":";
        if(secs < 10) {
            s += "0";
        }
        s += secs;
        return s;
    }

}
