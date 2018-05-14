package com.gametime.quadrant.InvitesModule;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gametime.quadrant.Adapters.InviteCurrUsersAdapter;
import com.gametime.quadrant.Exceptions.GenExceptions;
import com.gametime.quadrant.Models.CreateInvitation;
import com.gametime.quadrant.Models.CreatedGroups;
import com.gametime.quadrant.Models.CurrUsers;
import com.gametime.quadrant.Models.Group;
import com.gametime.quadrant.Models.GroupMemberList;
import com.gametime.quadrant.Models.JoinedGroups;
import com.gametime.quadrant.Models.MemberListParams;
import com.gametime.quadrant.Models.QuadrantLoginDetails;
import com.gametime.quadrant.Network.APIClient;
import com.gametime.quadrant.Network.APIInterface;
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

/**
 * Created by Akansh on 01-12-2017.
 */

public class InviteCurrentUsersActivity extends AppCompatActivity implements InvitesItemClickListener {
    private RecyclerView recyclerView;
    private List<CurrUsers.Success> success;
    private InviteCurrUsersAdapter currUsersAdapter;
    private Group groups;
    private JoinedGroups.Groups joinedGroups;
    private CreatedGroups.Groupsd createdGroups;
    private String groupId;
    private String APICreds;
    private QuadrantLoginDetails login;
    private TextView noActiveQuadrantUsers;
    private ImageView back;
    private TextView title;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_current_users);

        if (!Network.isNetworkAvailable(this)) {
            finishAndRemoveTask();
        }
        recyclerView = (RecyclerView) findViewById(R.id.inviteCurrUsersRecyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        groups = (Group) getIntent().getSerializableExtra("groups");
        joinedGroups = (JoinedGroups.Groups) getIntent().getSerializableExtra("joinedGroups");
        createdGroups = (CreatedGroups.Groupsd) getIntent().getSerializableExtra("createdGroups");
        back = findViewById(R.id.backButtonInvites);
        title = findViewById(R.id.InvitesActTitle);
        noActiveQuadrantUsers = findViewById(R.id.noActiveQuadrantUsers);

        title.setText(R.string.invite_curr_users);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        if (groups != null) {
            groupId = String.valueOf(groups.getId());
        } else if (joinedGroups != null) {
            groupId = String.valueOf(joinedGroups.getId());
        } else if (createdGroups != null) {
            groupId = String.valueOf(createdGroups.getId());
        }

        APICreds = getSharedPreferences(Constants.PREF_FILE_NAME,
                Context.MODE_PRIVATE).getString(PREF_KEY_LOGGED_IN_USER, "");
        login = new Gson().fromJson(APICreds, QuadrantLoginDetails.class);

        getUserDetails(this, login, recyclerView, groupId, this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!Network.isNetworkAvailable(this)) {
            finishAndRemoveTask();
        }
    }

    private void getUserDetails(final Context context, final QuadrantLoginDetails QuadrantLoginDetails, final RecyclerView recyclerView, final String gid, final InvitesItemClickListener itemClickListener) {
        APIInterface apiInterface = APIClient.getClientWithAuth(context)
                .create(APIInterface.class);
        final MemberListParams memberListParams = new MemberListParams(groupId);

        Call<GroupMemberList> call = apiInterface.getAllMembersOfGrp(memberListParams);
        call.enqueue(new Callback<GroupMemberList>() {
            @Override
            public void onResponse(Call<GroupMemberList> call, Response<GroupMemberList> response) {
                if (response.body().getSuccess() != null) {
                    for (int i=0; i<response.body().getSuccess().size(); i++) {
                        if (response.body().getSuccess().get(i).getJid().equals(QuadrantLoginDetails.getXmppUserDetails().getJid()) && response.body().getSuccess().get(i).getRole().equalsIgnoreCase("admin")) {
                            getCurrentUserDetails(recyclerView, context, groupId, itemClickListener);
                        } else {
                            AlertDialog.Builder alertNotAdmin = new AlertDialog
                                    .Builder(InviteCurrentUsersActivity
                                    .this).setTitle(R.string.notAdmin)
                                    .setMessage(R.string.notAdminMessage)
                                    .setPositiveButton(android.R.string.yes, new
                                            DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    finish();
                                                }
                                            });
                            AlertDialog dialog = alertNotAdmin.create();
                            dialog.show();
                        }
                    }
                } else {
                    Toast.makeText(context, "some Error occured..", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GroupMemberList> call, Throwable t) {

            }
        });
    }

    private void getCurrentUserDetails(final RecyclerView recyclerView, final Context context, final String gid, final InvitesItemClickListener itemClickListener) {
        APIInterface apiInterface = APIClient.getClientWithAuth(context)
                .create(APIInterface.class);

        Call<CurrUsers> call = apiInterface.getCurrentUsersList();
        call.enqueue(new Callback<CurrUsers>() {
            @Override
            public void onResponse(Call<CurrUsers> call, Response<CurrUsers> response) {
                if (response.body().getSuccess() != null) {
                    success = new ArrayList<>();

                    currUsersAdapter = new InviteCurrUsersAdapter(context,
                            response.body().getSuccess(), gid, itemClickListener);
                    success.addAll(response.body().getSuccess());
                    currUsersAdapter.notifyDataSetChanged();
                    recyclerView.setAdapter(currUsersAdapter);

                    if (response.body().getSuccess().size() == 0) {
                        noActiveQuadrantUsers.setText("No pending requests");
                    } else {
                        noActiveQuadrantUsers.setText("");
                    }
                } else {
                    Toast.makeText(InviteCurrentUsersActivity.this,
                            "Some problem occurred",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CurrUsers> call, Throwable t) {

            }
        });
    }

    @Override
    public void onItemClick(CurrUsers.Success currUsers, String gid) {
        CreateInvitation createInvitation = new CreateInvitation(gid,
                currUsers.getExternalId());

        APIInterface apiInterface = APIClient.getClientWithAuth(this)
                .create(APIInterface.class);
        Call<ResponseBody> call = apiInterface.createInvite(createInvitation);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Toast.makeText(InviteCurrentUsersActivity.this,
                        "invite sent successfully",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                GenExceptions.fireException(t);
            }
        });
    }
}
