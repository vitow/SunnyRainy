package com.example.tijingwang.sunnyrainy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FavoritesActivity extends AppCompatActivity {
    public static final String TAG = FavoritesActivity.class.getSimpleName();

    private SongsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        if (getCallingActivity() != null) {
            setTitle(getString(R.string.create_host));
        } else {
            setTitle(getString(R.string.app_name) + " | " + getString(R.string.favor_songs));
        }

        // Construct the data source
        ArrayList<Song> songs = new ArrayList<Song>();
        // Create the adapter to convert the array to views
        adapter = new SongsAdapter(this, songs);
        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.lvSongs);
        listView.setAdapter(adapter);

        retrieveSongs();
    }

    public void pickSong(View view) {
        if (getCallingActivity() != null) {
            //This Activity was called by startActivityForResult
            Intent intent = new Intent();
            intent.putExtra("song_id", (int)view.getTag());
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    private void retrieveSongs() {
        final SharedPreferences pref = this.getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        final int user_id = pref.getInt("user_id", 0);
        String apiPath = "user/"+ user_id +"/songs";
        String apiUrl = getString(R.string.api_host) + apiPath;
        //Log.v(TAG, "apiUrl: " + apiUrl);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(apiUrl).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.v(TAG, "Fail to retrieve songs");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String jsonData = response.body().string();
                    Log.v(TAG, jsonData);
                    if (response.isSuccessful()) {
                        JSONObject obj = new JSONObject(jsonData);
                        JSONArray items = obj.getJSONArray("songs");
                        ArrayList<Song> list = new ArrayList<>();
                        for(int i=0; i<items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            Song s = new Song(item.getInt("id"), item.getString("title"), item.getString("artist"), item.getInt("duration"));
                            String song_uri = item.getString("url");

                            // Keep locally favored songs only
                            String uniq_id =  "" + user_id  + "-" + song_uri;
                            boolean favor_mark = pref.getBoolean(uniq_id, false);
                            if(favor_mark) {
                                list.add(s);
                            }
                        }

                        if(list.size() > 0) {
                            // Sorting
                            Collections.sort(list, new Comparator<Song>() {
                                @Override
                                public int compare(Song s1, Song s2)
                                {
                                    return  s1.title.toLowerCase().compareTo(s2.title.toLowerCase());
                                }
                            });

                            final ArrayList<Song> l = list;

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.clear();
                                    adapter.addAll(l);
                                }
                            });
                        } else {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                Toast.makeText(FavoritesActivity.this, "No favorite songs!", Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                    } else {
                        Log.v(TAG, "Wrong response message: " + response.message());
                    }
                } catch (IOException e) {
                    Log.e(TAG, "IOException caught", e);
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException caught", e);
                }
            }
        });
    }
}
