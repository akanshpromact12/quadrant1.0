package com.gametime.quadrant.CreateGroupModule;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.gametime.quadrant.Exceptions.GenExceptions;
import com.gametime.quadrant.InvitePeopleModule.InvitePeopleActivity;
import com.gametime.quadrant.Models.CreateGroup;
import com.gametime.quadrant.Network.APIClient;
import com.gametime.quadrant.Network.APIInterface;
import com.gametime.quadrant.PrivateGroupModule.PrivateGroupActivity;
import com.gametime.quadrant.R;
import com.gametime.quadrant.Utils.Constants;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static com.gametime.quadrant.Utils.Constants.CREATE_GROUP_BOUNDRY_TYPE;
import static com.gametime.quadrant.Utils.Constants.GROUP_TYPE_PUBLIC;
import static com.gametime.quadrant.Utils.Constants.REQUEST_ACCESS_TYPE;

public class CreateGroupPresenter implements CreateGroupContract.CreateGroupActions {
    private static final String TAG = "CreateGroupActivity";
    private CreateGroupContract.CreateGroupView view;
    private Context context;

    CreateGroupPresenter(Context context, CreateGroupContract.CreateGroupView view) {
        this.view = view;
        this.context = context;
    }

    @Override
    public void createPublicGroup(final String groupName, final Context context, final String grpDesc, final String latLng) {
        if (!groupName.equals("")) {
            view.progressBarVisibility(View.VISIBLE);
            CreateGroup createGroup = new CreateGroup(GROUP_TYPE_PUBLIC,
                    CREATE_GROUP_BOUNDRY_TYPE, grpDesc,
                    1, 0, groupName,
                    "", latLng, REQUEST_ACCESS_TYPE);

            APIInterface apiInterface = APIClient
                    .getClientWithAuth(context)
                    .create(APIInterface.class);
            Call<ResponseBody> callPostGroupInfo = apiInterface
                    .postCreateGroup(createGroup);
            callPostGroupInfo.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    view.progressBarVisibility(View.GONE);
                    Intent intent = new Intent(context, InvitePeopleActivity.class);
                    intent.putExtra("activityClose", true);
                    intent.putExtra("groupType", "public");
                    ((CreateGroupActivity) context).startActivityForResult(intent, 0);

                    Intent intent2 = new Intent();
                    ((CreateGroupActivity) context).setResult(RESULT_OK, intent2);
                    ((CreateGroupActivity) context).finish();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    view.progressBarVisibility(View.GONE);
                    view.showSuccessMsg(GenExceptions.fireException(t));
                    Log.d(TAG, GenExceptions.fireException(t));
                }
            });
        } else {
            AlertDialog.Builder alertPublicGrp = new AlertDialog.Builder(context);
            alertPublicGrp.setTitle("Public Group");
            alertPublicGrp.setMessage(context.getString(R.string.enter_group_name));
            alertPublicGrp.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            AlertDialog dialog = alertPublicGrp.create();
            dialog.show();
        }
    }

    @Override
    public void createPrivateGroup(String grpName, String grpDesc, String latLng, Context context) {
        if (!grpName.equals("") || grpName.length() != 0) {
            Intent intent = new Intent(context,
                    PrivateGroupActivity.class);
            intent.putExtra(Constants.EXTRA_GROUP_NAME_KEY, grpName);
            intent.putExtra(Constants.EXTRA_GROUP_DESCRIPTION_KEY, grpDesc);
            intent.putExtra(Constants.EXTRA_LAT_LNG_KEY, latLng);

            context.startActivity(intent);
            ((Activity) context).finish();
        } else {
            AlertDialog.Builder alertPublicGrp = new AlertDialog.Builder(context);
            alertPublicGrp.setTitle("Private Group");
            alertPublicGrp.setMessage(context.getString(R.string.enter_group_name));
            alertPublicGrp.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            AlertDialog dialog = alertPublicGrp.create();
            dialog.show();
        }
    }

    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        Intent intent = new Intent();
        ((CreateGroupActivity) context).setResult(RESULT_OK, intent);
        ((CreateGroupActivity) context).finish();
    }
}
