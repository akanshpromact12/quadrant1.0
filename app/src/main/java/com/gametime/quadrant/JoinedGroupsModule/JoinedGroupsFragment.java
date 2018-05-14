package com.gametime.quadrant.JoinedGroupsModule;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gametime.quadrant.Models.ChatsRealm;
import com.gametime.quadrant.R;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.gametime.quadrant.Exceptions.GenExceptions.TAG;

/**
 * Created by Akansh on 16-11-2017.
 */

    public class

JoinedGroupsFragment extends Fragment implements JoinedGroupContract.joinedGroupView {
    JoinedGroupPresenter presenter;
    RecyclerView recyclerView;
    private TextView noJoinedGroups;
    private ProgressBar progressBar;
    private Context context;
    private RelativeLayout relativeView;
    private Realm realm = Realm.getDefaultInstance();

    public static JoinedGroupsFragment newInstance() {
        return new JoinedGroupsFragment();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            //recyclerView.getAdapter().notifyDataSetChanged();
            //presenter.showJoinedGroups(context, recyclerView);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_joined_groups, container, false);

        //getCountRealm();
        context = rootView.getContext();
        relativeView = rootView.findViewById(R.id.joinedRelativeView);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        presenter = new JoinedGroupPresenter(this);
        noJoinedGroups = (TextView) rootView.findViewById(R.id.noJoinedGrps);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.joinedGroupsRecyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(rootView.getContext());
        recyclerView.setLayoutManager(mLayoutManager);

        progressBar.bringToFront();
        progressBar.setIndeterminate(true);
        noJoinedGroups.setVisibility(View.GONE);
        presenter.showJoinedGroups(rootView.getContext(), recyclerView, relativeView);

        return rootView;
    }

    private void getCountRealm() {
        //realm.beginTransaction();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<ChatsRealm> realmResults = realm.where(ChatsRealm.class)
                        .findAll();

                if (realmResults.size() > 0) {
                    Log.e(TAG, "realm size: " + realmResults.size());
                } else {
                    Log.e(TAG, "realm is empty");
                }
            }
        });
        realm.commitTransaction();
    }

    @Override
    public void joinedGroupSuccess(String msg) {
        Log.d(TAG, "joinedGroupSuccess: -> " + msg);
        //Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void noJoinedGroupsVisible(int visible) {
        noJoinedGroups.setVisibility(visible);
    }

    @Override
    public void progressBarVisible(int visible) {
        progressBar.setVisibility(visible);
    }
}
