package com.example.tijingwang.sunnyrainy.helper;

import com.spotify.sdk.android.player.Player;

/**
 * Created by YLTL on 7/25/16.
 */
public class MusicConstant {
    /**
     * Set clientID, redirectURL and requestCode according to Spotify developer account.
     */
    public static final String REDIRECT_URI = "test1-musicplayer-login://callback/";
    public static final String CLIENT_ID = "90aed54790a74dee92a61da9424b3ca3";
    public static final int REQUEST_CODE = 1337;
    /**
     * String to store spotify access token.
     */
    public static String AccessToken;

    public static Player mPlayer;

    /**
     * A flag that used to identify login status.
     */
    public static boolean DEFAULT_LOGIN_STATUS = false;
}
