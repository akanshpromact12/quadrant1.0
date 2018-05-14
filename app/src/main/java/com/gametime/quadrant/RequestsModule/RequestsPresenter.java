package com.gametime.quadrant.RequestsModule;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.gametime.quadrant.Adapters.RequestsAdapter;
import com.gametime.quadrant.Exceptions.GenExceptions;
import com.gametime.quadrant.Models.Requests;
import com.gametime.quadrant.Network.APIClient;
import com.gametime.quadrant.Network.APIInterface;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Akansh on 24-11-2017.
 */

public class RequestsPresenter implements RequestsContract.RequestsActions {
    RequestsContract.RequestsView view;
    ArrayList<Requests.GroupReq> groupReqs;
    RequestsAdapter requestsAdapter;
    private static final String TAG = "RequestsPresenter";

    public RequestsPresenter(RequestsContract.RequestsView view) {
        this.view = view;
    }

    @Override
    public void checkAllRequests(final Context context, final RecyclerView recyclerView, final TextView noRequestsFound) {
        APIInterface apiInterface = APIClient.getClientWithAuth(context)
                .create(APIInterface.class);
        view.progressBarVisibility(View.VISIBLE);
        Call<Requests> call = apiInterface.fetchAllRequests();
        call.enqueue(new Callback<Requests>() {
            @Override
            public void onResponse(Call<Requests> call, Response<Requests> response) {
                if (response.code() == 200/* && response.body().getGroups().size() == 0*/) {
                    noRequestsFound.setText("no requests found");
                } else if (response.code() != 200) {
                    Log.e(TAG, "There was some problem.....");
                    ((Activity) context).finish();
                } else {
                    noRequestsFound.setVisibility(View.GONE);
                    groupReqs = new ArrayList<>();
                    requestsAdapter = new RequestsAdapter(groupReqs, context);

                    groupReqs.addAll(response.body().getGroups());
                    requestsAdapter.notifyDataSetChanged();
                    recyclerView.setAdapter(requestsAdapter);
                }
                view.progressBarVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<Requests> call, Throwable t) {
                view.progressBarVisibility(View.GONE);
            }
        });
    }
}
