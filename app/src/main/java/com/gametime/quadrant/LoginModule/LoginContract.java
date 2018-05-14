package com.gametime.quadrant.LoginModule;

import android.content.Context;

import com.facebook.CallbackManager;

/**
 * Created by Akansh on 09-11-2017.
 */

public class LoginContract {
    public interface loginView {
        void loginSuccessView(String msg);
        void progreessBarVisibility(int visibility);
    }

    public interface LoginActions {
        void loginWithFB(CallbackManager callbackManager, Context context);
    }
}
