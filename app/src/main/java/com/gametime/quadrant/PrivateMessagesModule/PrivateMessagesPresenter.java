package com.gametime.quadrant.PrivateMessagesModule;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.gametime.quadrant.Network.APIClient;
import com.gametime.quadrant.Network.APIInterface;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Akansh on 05-12-2017.
 */

public class PrivateMessagesPresenter implements PrivateMessagesContract.PrivateMessagesActions {
    PrivateMessagesContract.PrivateMessagesView view;

    public PrivateMessagesPresenter(PrivateMessagesContract.PrivateMessagesView view) {
        this.view = view;
    }

    @Override
    public void sendMessage(ArrayList<String> messages, String textToSend, ArrayList<String> names, String senderName, ArrayList<String> times, String date, Context context, RecyclerView recyclerView) {
        String dt = date.split(" ")[0];
        String tm = date.split(" ")[1];
        String mnt = dt.split("-")[1];
        String currDt = dt.split("-")[2];
        int hr = Integer.parseInt(tm.split(":")[0]);
        String mins = tm.split(":")[1];
        String newMnt = "";

        switch (mnt) {
            case "01":
                newMnt = "Jan";
                break;
            case "02":
                newMnt = "Feb";
                break;
            case "03":
                newMnt = "Mar";
                break;
            case "04":
                newMnt = "Apr";
                break;
            case "05":
                newMnt = "May";
                break;
            case "06":
                newMnt = "June";
                break;
            case "07":
                newMnt = "July";
                break;
            case "08":
                newMnt = "Aug";
                break;
            case "09":
                newMnt = "Sept";
                break;
            case "10":
                newMnt = "Oct";
                break;
            case "11":
                newMnt = "Nov";
                break;
            case "12":
                newMnt = "Dec";
                break;
            default:
                newMnt = "Mnt";
                break;
        }

        String newDen = "";
        if (hr >= 13 && hr <= 23) {
            newDen = "PM";
        } else {
            newDen = "AM";
        }

        String newHr = "";
        switch (hr) {
            case 1:
                newHr = "1";
            case 2:
                newHr = "2";
            case 3:
                newHr = "3";
            case 4:
                newHr = "4";
            case 5:
                newHr = "5";
            case 6:
                newHr = "6";
            case 7:
                newHr = "7";
            case 8:
                newHr = "8";
            case 9:
                newHr = "9";
            case 10:
                newHr = "10";
            case 11:
                newHr = "11";
            case 12:
                newHr = "12";
            case 13:
                newHr = "1";
                break;
            case 14:
                newHr = "2";
                break;
            case 15:
                newHr = "3";
                break;
            case 16:
                newHr = "4";
                break;
            case 17:
                newHr = "5";
                break;
            case 18:
                newHr = "6";
                break;
            case 19:
                newHr = "7";
                break;
            case 20:
                newHr = "8";
                break;
            case 21:
                newHr = "9";
                break;
            case 22:
                newHr = "10";
                break;
            case 23:
                newHr = "11";
                break;
        }

        String newDt = newMnt + " " + currDt + ", " + newHr + ":" + mins + " " + newDen;

        messages.add(textToSend);
        names.add(senderName);
        times.add(newDt);

        /*PrivateMessagesAdapter privateMessagesAdapter = new
                PrivateMessagesAdapter(context,
                messages, times, names);*/
        /*privateMessagesAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(privateMessagesAdapter);*/

        view.successfulMessage("Message sent");
    }

    @Override
    public void blockUser(String userId, Context context) {
        APIInterface apiInterface = APIClient.getClientWithAuth(context)
                .create(APIInterface.class);
        view.makeProgreessBarVisible();
        Call<ResponseBody> callBlockUser = apiInterface.blockUserFromPM(userId);
        callBlockUser.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                view.makeProgreessBarInvisible();
                view.successfulMessage("User was blocked...");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                view.makeProgreessBarInvisible();
            }
        });
    }

    @Override
    public void unBlockUser(String userId, Context context) {
        APIInterface apiInterface = APIClient.getClientWithAuth(context)
                .create(APIInterface.class);
        Call<ResponseBody> response = apiInterface.unblockUserFromPM(String.valueOf(userId));
        response.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }
}
