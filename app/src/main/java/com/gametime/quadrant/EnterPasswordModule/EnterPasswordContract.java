package com.gametime.quadrant.EnterPasswordModule;

import android.content.Context;

class EnterPasswordContract {
    public interface EnterPasswordView {
        void showMessage(String msg);
    }

    public interface EnterPasswordActions {
        void fileMembershipToGrp(String grpId, Context context, String password, String requestType);
    }
}
