package com.gametime.quadrant.RequestsModule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gametime.quadrant.HomeModule.HomeActivity;
import com.gametime.quadrant.Network.APIClient;
import com.gametime.quadrant.Network.APIInterface;
import com.gametime.quadrant.R;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestsFragment extends Fragment implements RequestsContract.RequestsView {
    RequestsPresenter presenter;
    RecyclerView recyclerView;
    AppBarLayout appbar;
    TextView noRequestsFound;
    NavigationView navigationView;
    private ProgressBar progressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_requests, container, false);

        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        progressBar.bringToFront();
        progressBar.setIndeterminate(true);
        presenter = new RequestsPresenter(this);
        //navigationView = (NavigationView) rootView.findViewById(R.id.nav_view_requests);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.requestsRecyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(rootView.getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        appbar = (AppBarLayout) rootView.findViewById(R.id.appbar);
        noRequestsFound = (TextView) rootView.findViewById(R.id.noRequestsFound);

        presenter.checkAllRequests(rootView.getContext(), recyclerView, noRequestsFound);
        appbar.setVisibility(View.GONE);
        //navigationView.setVisibility(View.GONE);
        noRequestsFound.setText("No requests found");

        return rootView;
    }

    @Override
    public void viewSuccessMessage(String msg) {

    }

    @Override
    public void performOprnOnReq(final String action, Integer reqId, final Context context, final View view, final Integer position) {
        APIInterface apiInterface = APIClient.getClientWithAuth(context)
                .create(APIInterface.class);
        Call<ResponseBody> call = apiInterface.AcceptRejectRequests(action, reqId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    Snackbar.make(view,
                            "Request has been accepted",
                            Snackbar.LENGTH_LONG).show();

                    Intent intent = new Intent(context, HomeActivity.class);
                    ((Activity)context).startActivityForResult(intent, 1);
                } else {
                    Snackbar.make(view,
                            "Some problem was encountered while " +
                                    "accepting the request. Please try " +
                                    "again after some time.",
                            Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    @Override
    public void progressBarVisibility(int visibility) {
        progressBar.setVisibility(visibility);
    }
}