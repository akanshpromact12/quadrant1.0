package com.gametime.quadrant.CreateGroupModule;

import android.content.Context;

class CreateGroupContract {
    public interface CreateGroupView {
        void showSuccessMsg (String msg);
        void progressBarVisibility (Integer visible);
    }

    public interface CreateGroupActions {
        void createPublicGroup(String groupName, Context context, String grpDesc, String latLng);
        void createPrivateGroup(String grpName, String grpDesc, String latLng,
                                Context context);
    }
}
