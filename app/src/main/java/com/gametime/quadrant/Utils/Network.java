package com.gametime.quadrant.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by akansh on 01-01-2018.
 */

public class Network {
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo netInfo = connManager.getActiveNetworkInfo();

        return netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED;
    }
}
