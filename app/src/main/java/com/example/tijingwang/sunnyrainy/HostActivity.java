package com.example.tijingwang.sunnyrainy;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.Spotify;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HostActivity extends AppCompatActivity {
    public static final String TAG = HostActivity.class.getSimpleName();

    private Player mPlayer;

    @BindView(R.id.hostNameTv)
    TextView mHostName;
    @BindView(R.id.albumArtIv)
    ImageView mAlbumArt;
    @BindView(R.id.songTitleTv)
    TextView mSongTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        ButterKnife.bind(this, findViewById(android.R.id.content));

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int host_id = extras.getInt("host_id");
            if(host_id > 0) {
                retrieveHost(host_id);
            }
        }
    }

    private void retrieveHost(int host_id) {
        SharedPreferences pref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        final String spotifyToken = pref.getString(MainActivity.SPOTIFY_TOKEN, "");

        String apiPath = "host/" + host_id;
        String apiUrl = getString(R.string.api_host) + apiPath;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(apiUrl).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Exception caught", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String jsonData = response.body().string();
                    Log.v(TAG, jsonData);
                    if (response.isSuccessful()) {
                        JSONObject obj = new JSONObject(jsonData);
                        JSONObject host = obj.getJSONObject("host");
                        JSONObject song = host.getJSONObject("song");
                        JSONObject user = host.getJSONObject("user");

                        final String song_uri = song.getString("url");
                        final String song_line = song.getString("title") + "\nby " + song.getString("artist");
                        final String user_name = user.getString("nickname");
                        final String user_avatar = user.getString("avatar");

                        // Play the song if possible
                        Config playerConfig = new Config(HostActivity.this, spotifyToken, getString(R.string.spotify_client_id));
                        mPlayer = Spotify.getPlayer(playerConfig, this, null);
                        if(mPlayer != null) {
                            mPlayer.play(song_uri);
                        }


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mHostName.setText(user_name + "'s HOST");
                                mSongTitle.setText(song_line);

                                if(!user_avatar.equals("")) {
                                    new ImageLoadTask(user_avatar, mAlbumArt).execute();
                                }
                            }
                        });
                    }
                } catch (IOException e) {
                    Log.e(TAG, "IOException caught", e);
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException caught", e);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if(mPlayer != null)
            mPlayer.pause();
    }
}
