package com.gametime.quadrant.CreateGroupSelectAreaModule;

import android.content.Context;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

class CreateGroupSelectAreaContract {
    public interface SelectAreaView {
        void showMessage(String msg);
    }

    public interface SelectAreaActions {
        void postAreaDetails(LatLng lng, Context context, ProgressBar progressBar, RelativeLayout ok);
        LatLng getCenterPoint(ArrayList<LatLng> latLngList);
        void postDetails(LatLng lng, String strLatLng, Context context);
    }
}
