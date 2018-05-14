package com.gametime.quadrant.JoinGroupModule;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;

import com.gametime.quadrant.Models.JoinGroup;
import com.gametime.quadrant.Network.APIClient;
import com.gametime.quadrant.Network.APIInterface;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JoinGroupPresenter implements JoinGroupContract.JoinGroupActions {
    private static final String TAG = "JoinGroupActivity";
    JoinGroupContract.JoinGroupView view;

    public JoinGroupPresenter(JoinGroupContract.JoinGroupView view) {
        this.view = view;
    }

    @Override
    public void JoinGroupMethod(final View activityView, final Context context, final String grpId, final String grpName, final String grpAccess, final String password, final String requestType) {
        JoinGroup(activityView, grpId, context, password, requestType);
    }

    private void JoinGroup(final View activityView, String grpId, final Context context, final String password, final String requestType) {
        APIInterface apiInterface = APIClient.getClientWithAuth(context)
                .create(APIInterface.class);

        JoinGroup joinGroup;
        if (password.equals("")) {
            joinGroup = new JoinGroup(password, grpId);
        } else {
            joinGroup = new JoinGroup(grpId);
        }
        Call<ResponseBody> call = apiInterface.joinGroup(grpId, joinGroup);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "onResponse: -> grp joined successfully");
                if (requestType.equals("MANUAL")) {
                    view.showMessage(activityView, "Request for group joining was sent.");
                } else {
                    view.showMessage(activityView, "Group joined successfully");
                    /*Intent intent = new Intent(context, HomeActivity.class);

                    context.startActivity(intent);*/
                    ((Activity) context).finish();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                view.showMessage(activityView, t.getMessage());
            }
        });
    }
}
