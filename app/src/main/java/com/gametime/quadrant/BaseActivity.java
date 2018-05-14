package com.gametime.quadrant;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Akansh on 09-04-2018.
 */

public class BaseActivity extends AppCompatActivity {
    protected static final String TAG = BaseActivity.class.getName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public static boolean isAppWentToBg = false;

    public static boolean isWindowFocused = false;

    public static boolean isMenuOpened = false;

    public static boolean isBackPressed = false;

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart isAppWentToBg" + isAppWentToBg);

        applicationWillEnterForeground();
        super.onStart();
    }

    private void applicationWillEnterForeground() {
        if (isAppWentToBg) {
            isAppWentToBg = false;
            /*Toast.makeText(getApplicationContext(), "App's in foreground", Toast.LENGTH_SHORT).show();*/
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop ");
        applicationdidenterbackground();
    }

    public void appUnderUse() {
        if (!isWindowFocused) {
            isAppWentToBg = true;
        }
    }

    public void applicationdidenterbackground() {
        if (!isWindowFocused) {
            isAppWentToBg = true;
            /*Toast.makeText(getApplicationContext(),
                    "App is Going to Background", Toast.LENGTH_SHORT).show();*/
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        isWindowFocused = hasFocus;

        if (isBackPressed && !hasFocus) {
            isBackPressed = false;
            isWindowFocused = true;
        }
        super.onWindowFocusChanged(hasFocus);
    }
}
