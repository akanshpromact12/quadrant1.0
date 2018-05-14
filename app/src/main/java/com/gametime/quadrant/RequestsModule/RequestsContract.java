package com.gametime.quadrant.RequestsModule;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Akansh on 24-11-2017.
 */

public class RequestsContract {
    public interface RequestsView {
        void viewSuccessMessage(String msg);
        void progressBarVisibility(int visibility);
        void performOprnOnReq(String action, Integer reqId, Context context, View view, Integer position);
    }

    public interface RequestsActions {
        void checkAllRequests(Context context, RecyclerView recyclerView, TextView noRequestsFound);
    }
}
