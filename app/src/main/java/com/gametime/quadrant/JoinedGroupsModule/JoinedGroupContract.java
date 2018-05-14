package com.gametime.quadrant.JoinedGroupsModule;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.widget.RelativeLayout;

/**
 * Created by Akansh on 16-11-2017.
 */

public class JoinedGroupContract {
    public interface joinedGroupView {
        void joinedGroupSuccess(String msg);
        void noJoinedGroupsVisible(int visible);
        void progressBarVisible(int visible);
    }

    public interface joinGroupActions {
        void showJoinedGroups(Context context, RecyclerView recyclerView, RelativeLayout relativeLayout);
    }
}
