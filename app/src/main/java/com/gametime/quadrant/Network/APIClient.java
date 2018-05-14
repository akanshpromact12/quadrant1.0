package com.gametime.quadrant.Network;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.gametime.quadrant.BuildConfig;
import com.gametime.quadrant.Models.QuadrantLoginDetails;
import com.gametime.quadrant.Utils.Constants;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.ContentValues.TAG;

public class APIClient {
    private static final String BASE_URL = "http://10.1.81.144:8080/v1/";
    public static Retrofit getClient() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(interceptor).build();
        Log.d(TAG, "getClient timeout: " + client
                .readTimeoutMillis());

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
    }
    public static Retrofit getClientWithAuth(final Context context) {
        Interceptor interceptorHeader = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREF_FILE_NAME, Context.MODE_PRIVATE);
                String loggedInUser = sharedPreferences.getString(Constants.PREF_KEY_LOGGED_IN_USER, "");

                QuadrantLoginDetails quadrantAPILogin = new Gson().fromJson(loggedInUser, QuadrantLoginDetails.class);
                Log.d(TAG, "intercept: -> XAccessToken: " + quadrantAPILogin.getToken());
                final Request request = chain.request().newBuilder()
                        .addHeader("X-Access-Token", quadrantAPILogin.getToken())
                        .build();

                return chain.proceed(request);
            }
        };

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .addInterceptor(interceptorHeader).build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
    }
}
