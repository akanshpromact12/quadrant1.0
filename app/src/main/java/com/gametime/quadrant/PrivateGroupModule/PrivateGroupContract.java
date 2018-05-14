package com.gametime.quadrant.PrivateGroupModule;

import android.content.Context;

/**
 * Created by Akansh on 17-11-2017.
 */

public class PrivateGroupContract {
    public interface PrivateGroupView {
        void showMessage(String msg);
        void progressBarVisibility(Integer visibility);
    }

    public interface PrivateGroupActions {
        void createPrivateGroup(String buttonSelected, Context context, String desc, String groupName, String password, String strLatLng);
    }
}
