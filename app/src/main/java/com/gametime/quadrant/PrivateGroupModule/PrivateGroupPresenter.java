package com.gametime.quadrant.PrivateGroupModule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.gametime.quadrant.Exceptions.GenExceptions;
import com.gametime.quadrant.InvitePeopleModule.InvitePeopleActivity;
import com.gametime.quadrant.Models.CreateGroup;
import com.gametime.quadrant.Network.APIClient;
import com.gametime.quadrant.Network.APIInterface;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static com.gametime.quadrant.Utils.Constants.PWD_MANUAL_REQUEST_ACCESS_TYPE;
import static com.gametime.quadrant.Utils.Constants.PWD_REQUEST_ACCESS_TYPE;
import static com.gametime.quadrant.Utils.Constants.REQUEST_ACCESS_TYPE;

public class PrivateGroupPresenter implements PrivateGroupContract.PrivateGroupActions {
    private static final String TAG = "PrivateGroupActivity";
    PrivateGroupContract.PrivateGroupView view;

    public PrivateGroupPresenter(PrivateGroupContract.PrivateGroupView view) {
        this.view = view;
    }

    @Override
    public void createPrivateGroup(final String buttonSelected, final Context context, String desc, final String groupName, String password, String strLatLng) {
        String reqType = "";
        if (buttonSelected.equals("pwdOnly")) {
            reqType = PWD_REQUEST_ACCESS_TYPE;
        } else if (buttonSelected.equals("reqOnly")) {
            reqType = REQUEST_ACCESS_TYPE;
        } else if (buttonSelected.equals("reqOrPwd")) {
            reqType = PWD_MANUAL_REQUEST_ACCESS_TYPE;
        }

        CreateGroup createGroup = new CreateGroup("PRIVATE",
                "POLYGON", desc, 1, 0, groupName,
                password, strLatLng, reqType);

        APIInterface apiInterface = APIClient
                .getClientWithAuth(context)
                .create(APIInterface.class);
        Call<ResponseBody> callPostGroupInfo = apiInterface
                .postCreateGroup(createGroup);
        callPostGroupInfo.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Toast.makeText(context,
                        "Group " + groupName +
                                " successfully created",
                        Toast.LENGTH_SHORT).show();

                /*Intent intent = new Intent(context,
                        HomeActivity.class);*/
                view.showMessage("Congrats!! A new private group was created");

                /*context.startActivity(intent);*/
                view.progressBarVisibility(View.GONE);
                Intent intent = new Intent(context, InvitePeopleActivity.class);
                intent.putExtra("activityClose", true);
                intent.putExtra("groupType", "private");
                ((PrivateGroupActivity) context).startActivityForResult(intent, 0);

                Intent intent2 = new Intent();
                ((PrivateGroupActivity) context).setResult(RESULT_OK, intent2);
                ((PrivateGroupActivity) context).finish();

                ((Activity) context).finish();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "onFailure: -> error: " +
                        GenExceptions.fireException(t));
                view.progressBarVisibility(View.GONE);
            }
        });
    }
}
