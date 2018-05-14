package com.gametime.quadrant.CreatedGroupsModule;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gametime.quadrant.R;

public class CreatedGroupsFragment extends Fragment implements CreatedGroupsContract.CreatedGroupsView {
    public static final String TAG = "CreatedGroupsFragment";
    private TextView noCreatedGrps;
    private ProgressBar progressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_created_group, container, false);

        progressBar = rootView.findViewById(R.id.progressBar);
        progressBar.bringToFront();
        progressBar.setIndeterminate(true);
        noCreatedGrps = rootView.findViewById(R.id.noCreatedGroups);
        Log.d(TAG, "CreatedGroupsFragment -> onCreateView: -> sharedPrefs: "+rootView.getContext()
                .getSharedPreferences("APICredentials", 0)
                .getString("X-Access-Token", ""));
        RecyclerView recyclerView = rootView.findViewById(R.id.createdGroupsRecyclerView);

        noCreatedGrps.setVisibility(View.GONE);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(rootView.getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        CreatedGroupsPresenter presenter = new CreatedGroupsPresenter(this, rootView.getContext());
        presenter.showCreatedGroups(recyclerView, rootView.getContext());

        return rootView;
    }

    @Override
    public void CreatedGroupsFetchSuccessView(String msg) {
        Log.d(TAG, "CreatedGroupsFetchSuccessView: -> " + msg);
    }

    @Override
    public void noCreateGrpsVisibility(int visibility) {
        noCreatedGrps.setVisibility(visibility);
    }

    @Override
    public void progressBarVisibility(int visibility) {
        progressBar.setVisibility(visibility);
    }
}
