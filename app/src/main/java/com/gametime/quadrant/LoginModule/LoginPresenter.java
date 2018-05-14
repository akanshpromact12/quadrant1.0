package com.gametime.quadrant.LoginModule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.gametime.quadrant.Exceptions.GenExceptions;
import com.gametime.quadrant.HomeModule.HomeActivity;
import com.gametime.quadrant.Models.FbToken;
import com.gametime.quadrant.Models.QuadrantLoginDetails;
import com.gametime.quadrant.Utils.Constants;
import com.google.gson.Gson;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.ContentValues.TAG;

/**
 * Created by Akansh on 09-11-2017.
 */

public class LoginPresenter implements LoginContract.LoginActions {
    public final LoginContract.loginView view;
    private String access_token;
    private String username;
    private Context context;

    @Override
    public void loginWithFB(CallbackManager callbackManager, final Context context) {
        //view.progreessBarVisibility(View.VISIBLE);
        LoginManager.getInstance().logOut();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(TAG, "onSuccess: -> login was successful");

                        getUserDets(loginResult, context);
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "onSuccess: -> login was successful");
                        //view.progreessBarVisibility(View.GONE);
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.e(TAG, "onError: -> error: " + error.getMessage() +
                                "cause: " + error.getCause());
                        if (AccessToken.getCurrentAccessToken() != null) {

                        }
                    }
                });
    }

    private void getUserDets(LoginResult loginResult, final Context context) {
        access_token = loginResult.getAccessToken().getToken();
        final FbToken fbToken = new FbToken(loginResult.getAccessToken().getToken());
        SharedPreferences sharedPreferences = context.getSharedPreferences("FbToken", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("FbToken", loginResult.getAccessToken().getToken()).apply();

        GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult
                .getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject jsonObject, GraphResponse response) {
                try {
                    username = jsonObject.get("name").toString();
                    Log.d(TAG, "onGraph: -> username: " + username);
                    //Toast.makeText(context, "username: " + username, Toast.LENGTH_SHORT).show();
                    sendFBInfo(fbToken, context, username);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    view.loginSuccessView(GenExceptions.fireException(ex));
                    //view.progreessBarVisibility(View.GONE);
                }
            }
        });

        Bundle permission_param = new Bundle();
        permission_param.putString("fields", "id,name,email," +
                "picture.width(120).height(120)");

        graphRequest.setParameters(permission_param);
        graphRequest.executeAsync();
    }

    private void sendFBInfo(final FbToken access_token, final Context context, final String username) {
        Log.d(TAG, "Sending Fb Info to API");
        view.progreessBarVisibility(View.VISIBLE);
        /* final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setText("Please wait...");
        progressDialog.setTitle("logging in");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();*/

        try {
            com.gametime.quadrant.Network.APIInterface apiInterface = com.gametime.quadrant.Network.APIClient
                    .getClient()
                    .create(com.gametime.quadrant.Network.APIInterface.class);

            Call<QuadrantLoginDetails> call = apiInterface.postFbAccessToken(access_token);
            call.enqueue(new Callback<QuadrantLoginDetails>() {
                @Override
                public void onResponse(Call<QuadrantLoginDetails> call, Response<QuadrantLoginDetails> response) {
                    try {
                        Log.d(TAG, "onResponse: -> response: " +
                                response.body().getToken());

                        saveShredPrefs("APICredentials",
                                response.body());
                        Log.d(TAG, "onResponse: -> response body: " +
                                response.body());

                        Intent intent = new Intent(context,
                                HomeActivity.class);
                        intent.putExtra("username", username);

                        context.startActivity(intent);
                        view.progreessBarVisibility(View.GONE);
                        //((Activity) context).finish();

                        view.loginSuccessView("The api call was successful");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        view.loginSuccessView("The api call was unsuccessful\n" +
                            "Error: " + ex.getMessage());
                        view.progreessBarVisibility(View.GONE);

                        view.loginSuccessView("There was some problem in facebook login. Please try again after some time");
                    }

                    //view.progreessBarVisibility(View.GONE);
                }

                @Override
                public void onFailure(Call<QuadrantLoginDetails> call, Throwable t) {
                    t.printStackTrace();
                    view.loginSuccessView(GenExceptions.fireException(t));

                    view.progreessBarVisibility(View.GONE);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            view.loginSuccessView(GenExceptions.fireException(ex));
        }
    }

    private void saveShredPrefs(String sharedPrefsName, QuadrantLoginDetails loginDetails) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(loginDetails);

        editor.putString(Constants.PREF_KEY_LOGGED_IN_USER, json).apply();
    }

    public LoginPresenter(LoginContract.loginView view, Context context) {
        this.view = view;
        this.context = context;
    }
}
