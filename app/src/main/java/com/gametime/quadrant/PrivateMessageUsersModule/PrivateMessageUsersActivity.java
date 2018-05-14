package com.gametime.quadrant.PrivateMessageUsersModule;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gametime.quadrant.Adapters.PrivateMessageUsersAdapter;
import com.gametime.quadrant.BaseActivity;
import com.gametime.quadrant.Exceptions.GenExceptions;
import com.gametime.quadrant.Models.GroupMemberList;
import com.gametime.quadrant.Models.JoinedGroups;
import com.gametime.quadrant.Models.MemberListParams;
import com.gametime.quadrant.Models.PrivateMessagesList;
import com.gametime.quadrant.Network.APIClient;
import com.gametime.quadrant.Network.APIInterface;
import com.gametime.quadrant.R;
import com.gametime.quadrant.Utils.Network;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Akansh on 29-11-2017.
 */

public class PrivateMessageUsersActivity extends BaseActivity {
    private static final String TAG = "PrivateMsg";
    RecyclerView recyclerView;
    private ArrayList<PrivateMessagesList.Success> success;
    private PrivateMessageUsersAdapter adapter = new PrivateMessageUsersAdapter(this);
    private ArrayList<String> memberIDs, groupIDs;
    private JoinedGroups joinedGroups;
    private GroupMemberList groupMemberList;
    private String userId;
    private ProgressBar progressBar;
    //private Toolbar toolbar;
    private ImageButton backButton;
    private TextView title;
    private android.support.v7.widget.SearchView searchView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_message_users);

        if (!Network.isNetworkAvailable(this)) {
            finishAndRemoveTask();
        }

        Toast.makeText(this, "Hello PM Users", Toast.LENGTH_SHORT).show();

        searchView = findViewById(R.id.searchFriends);
        recyclerView = (RecyclerView) findViewById(R.id.private_msg_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setIndeterminate(true);
        title = (TextView) findViewById(R.id.titleOfPMUsers);
        backButton = (ImageButton) findViewById(R.id.backButtonPMUser);
        title.setText("Private Messages");
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);

                finish();
            }
        });
        searchView.setActivated(true);
        searchView.onActionViewExpanded();
        searchView.setIconified(true);
        searchView.clearFocus();

        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(PrivateMessageUsersActivity.this, "onQueryTextSubmit", Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Toast.makeText(PrivateMessageUsersActivity.this, "OnQueryTectChange", Toast.LENGTH_SHORT).show();
                adapter.getFilter().filter(newText);

                return true;
            }
        });

        memberIDs = new ArrayList<>();
        groupIDs = new ArrayList<>();
        joinedGroups = new JoinedGroups();
        groupMemberList = new GroupMemberList();
        userId = getIntent().getStringExtra("userId");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        showPrivateMessenger(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!Network.isNetworkAvailable(this)) {
            finishAndRemoveTask();
        }
    }

    private ArrayList<String> getAllGroupIDs(final Context context) {
        APIInterface apiInterface = APIClient.getClientWithAuth(context)
                .create(APIInterface.class);
        Call<JoinedGroups> call = apiInterface.fetchJoinedGroups();
        try {
            joinedGroups = call.execute().body();

            for (int i=0; i<joinedGroups.getGroups().size(); i++) {
                groupIDs.add(String.valueOf(joinedGroups.getGroups().get(i).getId()));
            }
        } catch (Exception ex) {
            GenExceptions.fireException(ex);
        }

        return groupIDs;
    }

    private ArrayList<String> getAllMemberIDs(Context context, ArrayList<String> groupID) {
        APIInterface apiInterface = APIClient.getClientWithAuth(context)
                .create(APIInterface.class);
        for (int i=0; i<groupID.size(); i++) {
            MemberListParams memberListParams = new MemberListParams(groupID.get(i));

            Call<GroupMemberList> call = apiInterface.getAllMembersOfGrp(memberListParams);
            try {
                Boolean memberExists = false;

                groupMemberList = call.execute().body();
                for (int j=0; j<groupMemberList.getSuccess().size(); j++) {

                    if (!String.valueOf(groupMemberList
                            .getSuccess().get(j).getId())
                            .equals(userId) && !memberIDs.contains(String
                            .valueOf(groupMemberList
                            .getSuccess().get(j).getId()))) {
                        memberIDs.add(String.valueOf(groupMemberList
                                .getSuccess().get(j).getId()/* + "-" +
                                groupMemberList.getSuccess().get(j)
                                        .getFirstName()*/));
                    }
                }
            } catch (Exception ex) {
                GenExceptions.fireException(ex);
            }
        }

        return memberIDs;
    }

    private void addFriendstoPM(final Context context, ArrayList<String> memID, final ArrayList<PrivateMessagesList.Success> success) {
        APIInterface apiInterface = APIClient.getClientWithAuth(context)
                .create(APIInterface.class);
        for (int i=0; i<memID.size(); i++) {
            Call<ResponseBody> call = apiInterface.addFriendsToPrivateMessaging(memID.get(i));
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    showPrivateMessenger(context);

                    Snackbar.make(getWindow().getDecorView().getRootView(), "user added successfully..", Snackbar.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Snackbar.make(getWindow().getDecorView().getRootView(), "There was some problem adding the users..", Snackbar.LENGTH_LONG).show();
                }
            });
        }
    }

    private void showPrivateMessenger(final Context context) {
        APIInterface apiInterface = APIClient.getClientWithAuth(this)
                .create(APIInterface.class);

        progressBar.setVisibility(View.VISIBLE);

        Call<PrivateMessagesList> call = apiInterface.getAllPrivateMessages();
        call.enqueue(new Callback<PrivateMessagesList>() {
            @Override
            public void onResponse(Call<PrivateMessagesList> call, Response<PrivateMessagesList> response) {
                success = new ArrayList<>();
                if (response.body().getSuccess().size() == 0) {
                    groupIDs = getAllGroupIDs(PrivateMessageUsersActivity.this);
                    memberIDs = getAllMemberIDs(PrivateMessageUsersActivity.this, groupIDs);

                    addFriendstoPM(PrivateMessageUsersActivity.this, memberIDs, success);
                }

                for (int i=0;i<response.body().getSuccess().size();i++) {
                    Log.d(TAG, "onResponse: -> users: " + response.body().getSuccess().get(i).getNick());
                }

                adapter = new PrivateMessageUsersAdapter(context, success);
                success.addAll(response.body().getSuccess());
                adapter.notifyDataSetChanged();
                recyclerView.setAdapter(adapter);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<PrivateMessagesList> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Snackbar.make(getWindow().getDecorView().getRootView(), "There was some problem fetching users..", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);

        finish();
    }
}
