package com.gametime.quadrant.GroupMemberListModule;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

public class GroupMemberListContract {
    public interface GroupMemberListView {
        void showMessage(String msg);
    }

    public interface GroupMemberListActions {
        void getAllMemberDetails(String groupId, RecyclerView recyclerView, Context context);
    }
}
