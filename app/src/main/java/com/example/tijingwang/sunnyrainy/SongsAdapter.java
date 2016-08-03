package com.example.tijingwang.sunnyrainy;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by netdong on 8/2/16.
 */
public class SongsAdapter extends ArrayAdapter<Song> {
    public SongsAdapter(Context context, ArrayList<Song> songs) {
        super(context, 0, songs);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final Song song = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_song, parent, false);
        }
        // Lookup view for data population
        TextView tvTitle = (TextView) convertView.findViewById(R.id.songTitle);
        TextView tvDuration = (TextView) convertView.findViewById(R.id.songDuration);
        // Populate the data into the template view using the data object
        tvTitle.setText(song.title);
        tvDuration.setText("Duration: " + song.getDurationString() + " Artist: " + song.artist);

        convertView.setTag(song.song_id);

        // Return the completed view to render on screen
        return convertView;
    }
}
