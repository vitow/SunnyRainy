package com.example.tijingwang.sunnyrainy.music;

import android.app.Activity;

import com.example.tijingwang.sunnyrainy.helper.MusicConstant;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

/**
 * Created by YLTL on 7/26/16.
 */
public class SpotifyAssembly {

    public void login(Activity activity) {
        /**
         * Build authentication request and get response through login.
         */
        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(MusicConstant.CLIENT_ID, AuthenticationResponse.Type.TOKEN, MusicConstant.REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private","user-top-read","playlist-modify-public", "playlist-modify-private"} );
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(activity, MusicConstant.REQUEST_CODE, request);
    }
}
