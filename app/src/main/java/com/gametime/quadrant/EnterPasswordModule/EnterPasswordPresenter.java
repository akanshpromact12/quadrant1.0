package com.gametime.quadrant.EnterPasswordModule;

import android.app.Activity;
import android.content.Context;

import com.gametime.quadrant.Exceptions.GenExceptions;
import com.gametime.quadrant.Models.JoinGroup;
import com.gametime.quadrant.Network.APIClient;
import com.gametime.quadrant.Network.APIInterface;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EnterPasswordPresenter implements EnterPasswordContract.EnterPasswordActions {
    private EnterPasswordContract.EnterPasswordView view;

    EnterPasswordPresenter(EnterPasswordContract.EnterPasswordView view) {
        this.view = view;
    }

    @Override
    public void fileMembershipToGrp(String grpId, final Context context, final String password, final String requestType) {
        APIInterface apiInterface = APIClient.getClientWithAuth(context)
                .create(APIInterface.class);

        JoinGroup joinGroup = new JoinGroup(password, grpId);

        Call<ResponseBody> call = apiInterface.joinGroup(grpId, joinGroup);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!password.equals("")) {
                    view.showMessage("The password entered is correct.");
                    ((Activity) context).finish();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                view.showMessage(GenExceptions.fireException(t));
            }
        });
    }
}
