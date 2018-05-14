package com.gametime.quadrant.Exceptions;

import android.util.Log;

import com.facebook.FacebookAuthorizationException;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class GenExceptions {
    public static final String TAG = "Error";

    public static String fireException(Throwable t) {
        String msg;

        if (t instanceof UnknownHostException) {
            msg = "The internet connection is down. Please try again later";
            logException(t);
        } else if (t instanceof FacebookAuthorizationException) {
            msg = "User not properly authorized to Facebook. Please try again";
            logException(t);
        } else if (t instanceof NullPointerException) {
            msg = "Please enter the values correctly";
            logException(t);
        } else {
            msg = "something went wrong!!";
            logException(t);
            if (t instanceof SocketTimeoutException) {
                msg = "The server may be down. Please try after some time";
                logException(t);
            }
        }

        return msg;
    }

    public static void logException(Throwable t) {
        Log.e(TAG, "Exception: " + t.getMessage(), t);
    }
}
