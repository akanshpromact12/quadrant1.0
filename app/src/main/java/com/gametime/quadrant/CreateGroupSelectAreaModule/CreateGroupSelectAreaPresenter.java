package com.gametime.quadrant.CreateGroupSelectAreaModule;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.gametime.quadrant.CreateGroupModule.CreateGroupActivity;
import com.gametime.quadrant.Exceptions.GenExceptions;
import com.gametime.quadrant.Models.AreaCheck;
import com.gametime.quadrant.Models.AreaDetails;
import com.gametime.quadrant.Network.APIClient;
import com.gametime.quadrant.Network.APIInterface;
import com.gametime.quadrant.Utils.Constants;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

public class CreateGroupSelectAreaPresenter implements CreateGroupSelectAreaContract.SelectAreaActions {
    private static final String TAG = "electAreaActivity";
    private CreateGroupSelectAreaContract.SelectAreaView view;
    private Context context;

    CreateGroupSelectAreaPresenter(Context context, CreateGroupSelectAreaContract.SelectAreaView view) {
        this.view = view;
        this.context = context;
    }

    @Override
    public void postDetails(LatLng lng, String strLatLng, Context context) {
        Intent intent = new Intent(context,
                CreateGroupActivity.class);
        intent.putExtra(Constants.EXTRA_CENTRAL_LAT_LNG_KEY, lng);
        intent.putExtra(Constants.EXTRA_LAT_LNG_KEY, strLatLng);

        ((CreateGroupSelectAreaActivity) context).startActivityForResult(intent, 0);
    }

    @Override
    public void postAreaDetails(final LatLng lng, final Context context, final ProgressBar progressBar, final RelativeLayout ok) {
        AreaDetails areaDetails = new AreaDetails(""+
                lng.latitude, ""+lng.longitude);

        APIInterface apiInterface = APIClient
                .getClientWithAuth(context)
                .create(APIInterface.class);
        Call<AreaCheck> callAreaPost = apiInterface
                .postArea(areaDetails);
        callAreaPost.enqueue(new Callback<AreaCheck>() {
            @Override
            public void onResponse(Call<AreaCheck> call, Response<AreaCheck> response) {
                try {
                    progressBar.setVisibility(View.GONE);

                    Log.d(TAG, "onResponse: ok.isEnabled(): " + ok.isEnabled());
                    if (!response.body().getStatus().equalsIgnoreCase("OK")) {
                        ok.setClickable(false);
                        ok.setEnabled(false);
                    } else {
                        ok.setClickable(true);
                        ok.setEnabled(true);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<AreaCheck> call, Throwable t) {
                view.showMessage(GenExceptions.fireException(t));
            }
        });
    }

    @Override
    public LatLng getCenterPoint(ArrayList<LatLng> latLngList) {
        LatLng centralLatLng;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (int i=0; i<latLngList.size(); i++) {
            builder.include(latLngList.get(i));
        }
        LatLngBounds bounds = builder.build();
        centralLatLng = bounds.getCenter();

        return centralLatLng;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Log.d(TAG, "CreateGroup Map Select");
            Intent intent = new Intent();
            ((CreateGroupSelectAreaActivity) context).setResult(RESULT_OK, intent);

            ((CreateGroupSelectAreaActivity) context).finish();
        } else {
            Log.d(TAG, "CreateGroup Map Select RESULT OK");
        }
    }
}
