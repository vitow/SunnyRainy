package com.example.tijingwang.sunnyrainy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
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
 * {@link SecondFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SecondFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SecondFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    public static final String TAG = SecondFragment.class.getSimpleName();

    private Marker meMarker;
    private Circle meCircle;
    final private float AVAILABLE_RADIUS = 1000;


    @BindView(R.id.hostCreateImageView)
    ImageView mCreateHost;
    @BindView(R.id.hostScanImageView)
    ImageView mScanHost;
    @BindView(R.id.locateImageView)
    ImageView mLocateMe;

    private GoogleMap mMap;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public SecondFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SecondFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SecondFragment newInstance(String param1, String param2) {
        SecondFragment fragment = new SecondFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_second, container, false);
        ButterKnife.bind(this, view);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Bind callbacks
        mCreateHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).updateLocation(); // Prepare for the host creation

                Intent intent = new Intent(getActivity(), FavoritesActivity.class);
                startActivityForResult(intent, MainActivity.PICK_SONG_REQUEST);
            }
        });
        mScanHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retrieveHosts();

                Toast.makeText(getActivity(), "All music hosts nearby are retrieved!", Toast.LENGTH_LONG).show();
            }
        });
        mLocateMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locateMe();
            }
        });

        return view;
    }

    private void createHost(int song_id) {
        SharedPreferences pref = getActivity().getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        final int user_id = pref.getInt("user_id", 0);
        if(user_id == 0) {
            Toast.makeText(getActivity(), "Only logged in user can create a host!", Toast.LENGTH_LONG).show();
            return;
        }

        String apiPath = "host/create";
        String apiUrl = getString(R.string.api_host) + apiPath;

        double latitude = MainActivity.getLatitude();
        double longitude = MainActivity.getLongitude();
        if (latitude == 0.0 && longitude == 0.0) {
            Toast.makeText(getActivity(), "Fail! Cannot get location information, try again later!", Toast.LENGTH_LONG).show();
            return;
        }


        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("user_id", "" + user_id)
                .add("song_id", "" + song_id)
                .add("latitude", "" + latitude)
                .add("longitude", "" + longitude)
                .build();
        //Log.d(TAG, "user_id: " + user_id + ", song_id: " + song_id + ", lat: " + MainActivity.latitude + ", lng: " + MainActivity.longitude);
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
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "Congrats! You've created a music host!", Toast.LENGTH_LONG).show();
                                mScanHost.performClick();
                            }
                        });
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Exception caught", e);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == MainActivity.PICK_SONG_REQUEST) {
            if(resultCode == Activity.RESULT_OK) {
                int song_id = data.getIntExtra("song_id", 0);
                if(song_id > 0) {
                    // Create the host
                    Log.v(TAG, "Creating the host for song_id: " + song_id);

                    createHost(song_id);
                }
            }
        }
    }

    private void locateMe() {
        try {
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    ((MainActivity) getActivity()).getGoogleApiClient());
            LatLng latLng;
            if (mLastLocation != null) {
                //place marker at current position
                //mGoogleMap.clear();
                latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            } else {
                latLng = new LatLng(40.4435, -79.9435); // CMU lat & lng
            }
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Current Position");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            if(meMarker != null)
                meMarker.remove();
            meMarker = mMap.addMarker(markerOptions);
            if(meCircle != null)
                meCircle.remove();
            meCircle = mMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .radius(AVAILABLE_RADIUS)
                    .strokeColor(Color.WHITE)
                    .fillColor(0x4099ff99));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13.5f));
        } catch (SecurityException e) {
            Log.v(TAG, "LocateMe exception: ", e);
        }
    }

    private void retrieveHosts() {
        String apiPath = "host/scan";
        String apiUrl = getString(R.string.api_host) + apiPath;
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
                        JSONObject obj = new JSONObject(jsonData);
                        JSONArray items = obj.getJSONArray("hosts");
                        final ArrayList<HashMap<String, Double>> list = new ArrayList<>();
                        for(int i=0; i<items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            HashMap<String, Double> d = new HashMap<String, Double>();
                            d.put("latitude", item.getDouble("latitude"));
                            d.put("longitude", item.getDouble("longitude"));
                            d.put("host_id", (double)item.getInt("id"));
                            list.add(d);
                        }


                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mMap.clear();
                                locateMe();

                                for(int i=0; i<list.size(); i++) {
                                    HashMap<String, Double> d = list.get(i);
                                    double lat = d.get("latitude");
                                    double lng = d.get("longitude");
                                    int host_id = d.get("host_id").intValue();

                                    LatLng latLng = new LatLng(lat, lng);
                                    MarkerOptions markerOptions = new MarkerOptions();
                                    markerOptions.position(latLng);
                                    markerOptions.title("Music Host " + host_id);
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                                    mMap.addMarker(markerOptions);
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

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getActivity().getFragmentManager(), "error_dialog");
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
//        try {
//            mMap.setMyLocationEnabled(true);
//        } catch(SecurityException se) {
//            Log.e(TAG, "Google Map setMyLocationEnabled exception caught", se);
//        }

        mLocateMe.performClick();
        mScanHost.performClick();
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
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(!marker.equals(meMarker)) {
            SharedPreferences pref = getActivity().getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
            String spotifyToken = pref.getString(MainActivity.SPOTIFY_TOKEN, "");
            if(spotifyToken.equals("")) {
                Toast.makeText(getActivity(), "Haven't login to Spotify?! You need to log in to Spotify to play song in a host.", Toast.LENGTH_LONG).show();
                return true;
            }

            LatLng ll1 = meMarker.getPosition();
            LatLng ll2 = marker.getPosition();

            Location loc1 = new Location("");
            loc1.setLongitude(ll1.longitude);
            loc1.setLatitude(ll1.latitude);

            Location loc2 = new Location("");
            loc2.setLongitude(ll2.longitude);
            loc2.setLatitude(ll2.latitude);

            double distance = loc1.distanceTo(loc2);
            if(distance > AVAILABLE_RADIUS) {
                Toast.makeText(getActivity(), "Too far away! You need to get closer to join that music host.", Toast.LENGTH_LONG).show();
                return  true;
            } else {
                String title = marker.getTitle();
                String[] parts = title.split(" ");
                if(parts.length != 3)
                    return false;

                int host_id = Integer.parseInt(parts[2]);

                Intent intent = new Intent(getActivity(), HostActivity.class);
                intent.putExtra("host_id", host_id);
                startActivity(intent);
                return true;
            }
        }
        return false;
    }
}
