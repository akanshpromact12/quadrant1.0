package com.gametime.quadrant.CreatedGroupsModule;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

class CreatedGroupsContract {
    public interface CreatedGroupsView {
        void CreatedGroupsFetchSuccessView(String msg);
        void noCreateGrpsVisibility(int visibility);
        void progressBarVisibility(int visibility);
    }

    public interface CreatedGroupsActions {
        void showCreatedGroups(RecyclerView recyclerView, Context context);
    }
}
