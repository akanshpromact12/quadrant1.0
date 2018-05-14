package com.gametime.quadrant.JoinGroupModule;

import android.content.Context;
import android.view.View;

/**
 * Created by Akansh on 17-11-2017.
 */

public class JoinGroupContract {
    public interface JoinGroupView {
        void showMessage(View view, String msg);
        void showAlertMessage(String msg);
    }

    public interface JoinGroupActions {
        void JoinGroupMethod(View activityView, Context context, String grpId, String grpName, String grpAccess, String password, String requestType);
    }
}
