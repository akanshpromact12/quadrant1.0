package com.gametime.quadrant.GroupMemberListModule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gametime.quadrant.InvitesModule.InviteCurrentUsersActivity;
import com.gametime.quadrant.Models.CreatedGroups;
import com.gametime.quadrant.Models.Group;
import com.gametime.quadrant.Models.GroupMemberList;
import com.gametime.quadrant.Models.JoinedGroups;
import com.gametime.quadrant.Models.PrivateMessagesList;
import com.gametime.quadrant.Models.QuadrantLoginDetails;
import com.gametime.quadrant.Network.APIClient;
import com.gametime.quadrant.Network.APIInterface;
import com.gametime.quadrant.PrivateMessagesModule.PrivateMessagesActivity;
import com.gametime.quadrant.R;
import com.gametime.quadrant.Utils.Constants;
import com.gametime.quadrant.Utils.Network;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.gametime.quadrant.Utils.Constants.PREF_KEY_LOGGED_IN_USER;

public class GroupMemberListActivity extends AppCompatActivity implements GroupMemberListContract.GroupMemberListView {
    private static final String TAG = "GroupMemberListActivity";
    private static ArrayList<Object> PMList;
    private static PrivateMessagesList.Success privateMessages;
    private Group groups;
    private JoinedGroups.Groups joinedGroups;
    private CreatedGroups.Groupsd createdGroups;
    ImageView back;
    TextView title;
    GroupMemberListPresenter presenter;
    String id;
    private static QuadrantLoginDetails login;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_member_list);

        if (!Network.isNetworkAvailable(this)) {
            finishAndRemoveTask();
        }
        groups = (Group) getIntent().getSerializableExtra("groups");
        joinedGroups = (JoinedGroups.Groups) getIntent().getSerializableExtra("joinedGroups");
        createdGroups = (CreatedGroups.Groupsd) getIntent().getSerializableExtra("createdGroups");
        back = findViewById(R.id.backButtonGrpMemberList);
        title = findViewById(R.id.titleOfGrpMemberList);

        if (groups != null) {
            id = String.valueOf(groups.getId());

            Log.d(TAG, "onCreate: -> group: " + groups.getId());
        } else if (joinedGroups != null) {
            id = String.valueOf(joinedGroups.getId());

            Log.d(TAG, "onCreate: -> group: " + joinedGroups.getId());
        } else if (createdGroups != null) {
            id = String.valueOf(createdGroups.getId());

            Log.d(TAG, "onCreate: -> group: " + createdGroups.getId());
        }

        Log.d(TAG, "onCreate: -> id: " + id);
        presenter = new GroupMemberListPresenter(this);
        RecyclerView grpList = findViewById(R.id.grpList_recycler_view);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        grpList.setLayoutManager(layoutManager);

        presenter.getAllMemberDetails(id, grpList, this);

        title.setText(R.string.member_list);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!Network.isNetworkAvailable(this)) {
            finishAndRemoveTask();
        }
    }

    @Override
    public void showMessage(String msg) {
        Snackbar.make(findViewById(android.R.id.content), msg,
                Snackbar.LENGTH_LONG);
    }

    public static void checkMemberChatExists(final GroupMemberList.Success success, final Context context, final View view) {
        String APICreds = context.getSharedPreferences(Constants.PREF_FILE_NAME,
                Context.MODE_PRIVATE).getString(PREF_KEY_LOGGED_IN_USER, "");
        login = new Gson().fromJson(APICreds, QuadrantLoginDetails.class);

        Log.d(TAG, "onClick: -> success: " + success.getFirstName());
        final APIInterface apiInterface = APIClient.getClientWithAuth(context)
                .create(APIInterface.class);
        Call<PrivateMessagesList> call = apiInterface.getAllPrivateMessages();
        PMList = new ArrayList<>();
        call.enqueue(new Callback<PrivateMessagesList>() {
            @Override
            public void onResponse(Call<PrivateMessagesList> call, Response<PrivateMessagesList> response) {
                for (int i=0; i<response.body().getSuccess().size(); i++) {
                    privateMessages = response.body().getSuccess().get(i);

                    PMList.add(String.valueOf(privateMessages.getId()));
                }
                 if (success.getId() == login.getId()) {
                    Snackbar.make(view, "You can't chat with yourself",
                            Snackbar.LENGTH_LONG).show();
                } else if (PMList.contains(String.valueOf(success.getId()))) {
                    List<PrivateMessagesList.Success> PMsList = response.body().getSuccess();
                            for (int j=0; j<PMsList.size(); j++) {
                                if (PMsList.get(j).getId().equals(success.getId())) {
                                    PrivateMessagesList.Success PMs = PMsList.get(j);
                                    Log.d(TAG, "onResponse: exists -> " + PMs.getNick());

                                    Intent intent = new Intent(context, PrivateMessagesActivity.class);
                                    intent.putExtra("privateMessages", PMs);

                                    context.startActivity(intent);
                                }
                            }
                    //Toast.makeText(context, "exists", Toast.LENGTH_SHORT).show();
                } else {
                            Call<ResponseBody> callAddUser = apiInterface.addFriendsToPrivateMessaging(String.valueOf(success.getId()));
                            callAddUser.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    Call<PrivateMessagesList> callPMList = apiInterface.getAllPrivateMessages();
                                    callPMList.enqueue(new Callback<PrivateMessagesList>() {
                                        @Override
                                        public void onResponse(Call<PrivateMessagesList> call, Response<PrivateMessagesList> response) {
                                            for (int k=0; k<response.body().getSuccess().size(); k++) {
                                                if (response.body().getSuccess().get(k).getId().equals(success.getId())) {
                                                    PrivateMessagesList.Success addedUserList = response.body().getSuccess().get(k);

                                                    Intent intent = new Intent(context, PrivateMessagesActivity.class);
                                                    intent.putExtra("privateMessages", addedUserList);

                                                    context.startActivity(intent);
                                                    ((Activity) context).finish();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<PrivateMessagesList> call, Throwable t) {

                                        }
                                    });
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {

                                }
                            });
                    Log.d(TAG, "onResponse: doesn't exists -> " + success.getFirstName() + " ID: " + success.getId());
                    //Toast.makeText(context, "doesn't exists -> " + success.getFirstName(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PrivateMessagesList> call, Throwable t) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_invite_people, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_invite_friends:
                Intent intent = new Intent(GroupMemberListActivity.this,
                        InviteCurrentUsersActivity.class);

                if (groups != null) {
                    intent.putExtra("groups", groups);
                } else if (joinedGroups != null) {
                    intent.putExtra("joinedGroups", joinedGroups);
                } else if (createdGroups != null) {
                    intent.putExtra("createdGroups", createdGroups);
                }

                startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
