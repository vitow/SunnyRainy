package com.example.tijingwang.sunnyrainy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    public static final String TAG = LoginActivity.class.getSimpleName();

    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();

        // Init facebook login button
        final LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("public_profile");
        // If using in a fragment
        // Other app specific specialization

        // Callback registration
        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                loginButton.setVisibility(View.INVISIBLE);

                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.v(TAG, response.toString());

                                // Parse the data
                                try {
                                    final String user_name = object.getString("name");
                                    String fb_id = object.getString("id");
                                    final String avatar = "http://graph.facebook.com/"+fb_id+"/picture?width=10000";

                                    String apiPath = "user/ensure";
                                    String apiUrl = getString(R.string.api_host) + apiPath;

                                    OkHttpClient client = new OkHttpClient();
                                    RequestBody formBody = new FormBody.Builder()
                                            .add("nickname", user_name)
                                            .add("fb_id", fb_id)
                                            .add("avatar", avatar)
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

                                                    SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
                                                    SharedPreferences.Editor editor = preferences.edit();
                                                    editor.putInt("user_id", obj.getInt("user_id"));
                                                    editor.putString("user_name", user_name);
                                                    editor.putString("avatar", avatar);
                                                    editor.commit();

                                                    Log.v(TAG, "User ID: " + obj.getInt("user_id"));

                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Intent intent = new Intent(LoginActivity.this.getApplicationContext(), MainActivity.class);
                                                            startActivity(intent);
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

                                    Log.d(TAG, user_name + "," + fb_id + "," + avatar);
                                } catch (Exception e) {
                                    Log.v(TAG, e.getCause().toString());
                                }

                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                // App code
                Log.v(TAG, "Facebook login cancelled");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.v(TAG, exception.getCause().toString());
            }
        });

        LoginManager.getInstance().logOut();
    }

    @Override
    public void onBackPressed()
    {
        // Your Code Here. Leave empty if you want nothing to happen on back press.
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
