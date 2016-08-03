package com.example.tijingwang.sunnyrainy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.sdk.android.player.Player;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FirstFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FirstFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FirstFragment extends Fragment {

    @BindView(R.id.temperatureLabel)
    TextView mTemperatureLabel;
    @BindView(R.id.iconImageView)
    ImageView mIconImageView;
    @BindView(R.id.weatherLabel)
    TextView mWeatherLabel;
    @BindView(R.id.favor_song)
    ImageView mFavorSong;
    @BindView(R.id.play_song)
    ImageView mPlaySong;
    @BindView(R.id.next_song)
    ImageView mNextSong;

    private int curSongIdx = -1;
    private boolean favoredButton = false;
    private boolean userClickPause = false;
    private ArrayList<HashMap<String, String>> songList;
    private Forecast mForecast;
    public static final String TAG = FirstFragment.class.getSimpleName();


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;



    public FirstFragment() {
        // Required empty public constructor
        songList = new ArrayList<>();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FirstFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FirstFragment newInstance(String param1, String param2) {
        FirstFragment fragment = new FirstFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        //getForecast();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_first, container, false);
        ButterKnife.bind(this, view);

        mFavorSong.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                favorSong();
            }
        });
        mPlaySong.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                playPauseSong();
            }
        });
        if(curSongIdx != -1 && !userClickPause) { // It's playing something, init it
            mPlaySong.setImageResource(R.drawable.ctrl_pause);
        }

        mNextSong.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                nextSong();
            }
        });

        return view;

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            getForecast();
        }
    }

    private void getForecast() {
        String apiPath = "weather?latitude=" + MainActivity.latitude + "&longitude=" + MainActivity.longitude;
        String forecastUrl = getString(R.string.api_host) + apiPath;
        Log.d("tijingw", String.valueOf(MainActivity.getLatitude()));
        Log.d("tijingw", String.valueOf(MainActivity.getLongitude()));

        if (isNetworkAvailable()) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(forecastUrl).build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    alertUserAboutError();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {
                            mForecast = parseForecastDetails(jsonData);

                            // Get song list from Spotify API
                            String kw = mForecast.getCurrent().getIcon();
                            retrieveSongList(kw);

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });
                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "Exception caught", e);
                    }
                }
            });
        }
        else {
            Toast.makeText(getActivity(), "Network is not available", Toast.LENGTH_LONG).show();
        }
    }

    private void retrieveSongList(String kw) throws UnsupportedEncodingException {
        kw = kw.replace("-", " ").replace("partly", "");
        String apiUrl = "https://api.spotify.com/v1/search?type=track&q=" + URLEncoder.encode(kw, "UTF-8");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(apiUrl).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                alertUserAboutError();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String jsonData = response.body().string();
                    Log.v(TAG, jsonData);
                    if (response.isSuccessful()) {
                        songList.clear();

                        JSONObject obj = new JSONObject(jsonData);
                        JSONArray items = obj.getJSONObject("tracks").getJSONArray("items");
                        for(int i=0; i<items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            HashMap<String, String> d = new HashMap<String, String>();
                            d.put("title", item.getString("name"));
                            d.put("uri", item.getString("uri"));
                            d.put("artist", item.getJSONArray("artists").getJSONObject(0).getString("name"));
                            d.put("duration", String.valueOf( item.getInt("duration_ms") / 1000 ) );
                            songList.add(d);
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(curSongIdx == -1) { // Only play it in the first load
                                    nextSong();
                                }
                            }
                        });
                    } else {
                        alertUserAboutError();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "IOException caught", e);
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException caught", e);
                }
            }
        });

    }

    public void nextSong() {
        if(songList.size() == 0)
            return;

        if(curSongIdx == -1) {
            mPlaySong.setImageResource(R.drawable.ctrl_pause);
        }

        curSongIdx = (curSongIdx + 1) % songList.size();
        String song_uri = songList.get(curSongIdx).get("uri");
        Player player = ((MainActivity)getActivity()).getSpotifyPlayer();
        if(player != null) {
            player.play(song_uri);
            mPlaySong.setImageResource(R.drawable.ctrl_pause);
        }

        // Check Favor mark
        SharedPreferences pref = getActivity().getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        int user_id = pref.getInt("user_id", 0);
        if(user_id == 0)
            return;
        String uniq_id =  "" + user_id  + "-" + song_uri;
        boolean favor_mark = pref.getBoolean(uniq_id, false);
        if(favor_mark) {
            changeIntoFavoredButton();
        } else {
            changeIntoNormalButton();
        }
    }

    private void playPauseSong() {
        Player player = ((MainActivity)getActivity()).getSpotifyPlayer();

        if(!userClickPause) {
            userClickPause = true;
            player.pause();
            mPlaySong.setImageResource(R.drawable.ctrl_play);
        } else {
            userClickPause = false;
            player.resume();
            mPlaySong.setImageResource(R.drawable.ctrl_pause);
        }
    }

    private void favorSong() {
        SharedPreferences pref = getActivity().getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        if(favoredButton) {
            // Delete Favor mark, but keep the song in the server
            SharedPreferences.Editor editor = pref.edit();
            String song_uri = songList.get(curSongIdx).get("uri");
            String uniq_id =  "" + pref.getInt("user_id", 0) + "-" + song_uri;
            editor.remove(uniq_id);
            editor.commit();

            changeIntoNormalButton();
        } else {
            // Get Use ID
            final int user_id = pref.getInt("user_id", 0);
            if(user_id == 0)
                return;

            // Get song info
            HashMap<String, String> d = songList.get(curSongIdx);
            final String song_uri = d.get("uri");
            // Save it to server
            // retrieve the api host, e.g. http://127.0.0.1:8000/api/
            String apiPath = "song/ensure";
            String apiUrl = getString(R.string.api_host) + apiPath;

            OkHttpClient client = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
                    .add("user_id", String.valueOf(user_id))
                    .add("title", d.get("title"))
                    .add("url", song_uri)
                    .add("artist", d.get("artist"))
                    .add("duration", d.get("duration"))
                    .build();
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .post(formBody)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.v(TAG, e.getCause().toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {
                            JSONObject obj = new JSONObject(jsonData);

                            // Local Favor Mark
                            String uniq_id =  "" + user_id + "-" + song_uri;
                            SharedPreferences preferences = getActivity().getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean(uniq_id, true);
                            editor.commit();

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    changeIntoFavoredButton();
                                }
                            });
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "Exception caught", e);
                    }
                }
            });

        }
    }

    private  void changeIntoFavoredButton() {
        favoredButton = true;

        mFavorSong.setImageResource(R.drawable.ctrl_plus_pink);
    }

    private  void changeIntoNormalButton() {
        favoredButton = false;

        mFavorSong.setImageResource(R.drawable.ctrl_plus);
    }

    private void updateDisplay() {
        Current current = mForecast.getCurrent();
        int fahrenheit = current.getTemperature();
        int celsius = (int)((float)(fahrenheit - 32 ) / 1.8f);
        mTemperatureLabel.setText(celsius + "°C / " + fahrenheit + "°F");
        Drawable drawable = getResources().getDrawable(current.getIconID());
        mIconImageView.setImageDrawable(drawable);
        mWeatherLabel.setText(current.getIcon());
    }

    private Forecast parseForecastDetails(String jsonData) throws JSONException {
        Forecast forecast = new Forecast();

        forecast.setCurrent(getCurrentDetails(jsonData));
        return forecast;
    }

    private Current getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        JSONObject currently = forecast.getJSONObject("result");
        Current current = new Current();
        current.setHumidity(currently.getDouble("humidity"));
        current.setTime(currently.getLong("time"));
        current.setIcon(currently.getString("icon"));
        current.setPrecipChance(currently.getDouble("precipProbability"));
        current.setSummary(currently.getString("summary"));
        current.setTemperature(currently.getDouble("temperature"));

        return current;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if(networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }

        return isAvailable;
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getActivity().getFragmentManager(), "error_dialog");
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
