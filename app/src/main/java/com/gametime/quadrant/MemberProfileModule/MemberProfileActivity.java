package com.gametime.quadrant.MemberProfileModule;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.gametime.quadrant.Adapters.CommonFriendsAdapter;
import com.gametime.quadrant.BaseActivity;
import com.gametime.quadrant.Models.CommonFriends;
import com.gametime.quadrant.Models.GetMutualFriends;
import com.gametime.quadrant.Models.MemberInfo;
import com.gametime.quadrant.Models.MemberProfile;
import com.gametime.quadrant.Models.PrivateMessagesList;
import com.gametime.quadrant.Models.QuadrantLoginDetails;
import com.gametime.quadrant.Network.APIClient;
import com.gametime.quadrant.Network.APIInterface;
import com.gametime.quadrant.R;
import com.gametime.quadrant.Utils.Constants;
import com.gametime.quadrant.Utils.Network;
import com.google.gson.Gson;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.gametime.quadrant.Utils.Constants.PREF_KEY_LOGGED_IN_USER;

public class MemberProfileActivity extends BaseActivity {
    private String number;
    private ImageView profileImg;
    private TextView profileName, memberEmail, memberMobile, noMutuals;
    private String username;
    private static final String TAG = "MemberProfileActivity";
    private RecyclerView recyclerView;
    private ArrayList<CommonFriends.AllMutualFriends> allFriends;
    private CommonFriendsAdapter adapter;
    private String fbToken;
    private TextView title, mobileM;
    private ImageView back;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_profile);

        if (!Network.isNetworkAvailable(this)) {
            finishAndRemoveTask();
        }
        noMutuals = findViewById(R.id.noMutuals);
        noMutuals.setVisibility(View.GONE);
        title = findViewById(R.id.titleOfGrpMemberList);
        mobileM = findViewById(R.id.mobileM);
        back = findViewById(R.id.backButtonGrpMemberList);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        String loginDetails = getSharedPreferences(Constants.PREF_FILE_NAME,
                Context.MODE_PRIVATE).getString(PREF_KEY_LOGGED_IN_USER, "");
        QuadrantLoginDetails login = new Gson().fromJson(loginDetails, QuadrantLoginDetails.class);
        /*RelativeLayout relativeLayout = findViewById(R.id.frontLayout);
        relativeLayout.bringToFront();*/
        String gid = getIntent().getStringExtra(Constants.EXTRA_GROUP_ID_KEY);
        String id = getIntent().getStringExtra(Constants.EXTRA_SUCCESS_ID);
        String externalId = getIntent().getStringExtra(Constants.EXTRA_SUCCESS__EXTERNAL_ID);
        number = getIntent().getStringExtra(Constants.EXTRA_SUCCESS_NUMBER);
        profileImg = findViewById(R.id.profileImg);
        profileName = findViewById(R.id.profileName);
        memberEmail = findViewById(R.id.memberEmail);
        memberMobile = findViewById(R.id.memberMobile);
        recyclerView = findViewById(R.id.commonFriendsRecyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        PrivateMessagesList.Success privateMessages = (PrivateMessagesList.Success) getIntent().getSerializableExtra("userInfo");
        allFriends = new ArrayList<>();
        adapter = new CommonFriendsAdapter(this, allFriends);
        recyclerView.setAdapter(adapter);
        SharedPreferences sharedPreferences = getSharedPreferences("FbToken", Context.MODE_PRIVATE);
        fbToken = sharedPreferences.getString("FbToken", "");
        APIInterface apiInterface = APIClient.getClientWithAuth(this)
                .create(APIInterface.class);

        profileImg.bringToFront();
        GetMutualFriends getMutualFriends = new GetMutualFriends(fbToken, externalId);
        Call<CommonFriends> call = apiInterface.allMutualFriends(getMutualFriends);
        call.enqueue(new Callback<CommonFriends>() {
            @Override
            public void onResponse(Call<CommonFriends> call, Response<CommonFriends> response) {
                if (response.code() == 200 && response.body().getSuccess() != null && response.body().getSuccess().getContext().getAllMutualFriends() != null) {
                    noMutuals.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    allFriends.clear();
                    allFriends.addAll(response.body().getSuccess().getContext().getAllMutualFriends());

                    adapter.notifyDataSetChanged();
                } else {
                    noMutuals.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    Snackbar.make(recyclerView, "There was some problem processing your request", Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonFriends> call, Throwable t) {
                Snackbar.make(recyclerView, "There was some problem in loading the list", Snackbar.LENGTH_LONG).show();
            }
        });

        if (privateMessages == null) {
            getMemberProfileInfo(gid, id);
        } else {
            getUserProfile(privateMessages);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!Network.isNetworkAvailable(this)) {
            finishAndRemoveTask();
        }
    }

    private void getUserProfile(PrivateMessagesList.Success memberInfo) {
        Glide.with(MemberProfileActivity.this).load(memberInfo.getProfileImage())
                .apply(RequestOptions.placeholderOf(R.drawable.profile))
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(profileImg);
        profileName.setText(memberInfo.getFirstName() + " " + memberInfo.getLastName());
        if (memberInfo.getEmail() != null) {
            memberEmail.setText(memberInfo.getEmail());
        } else {
            memberEmail.setText(R.string.EmailNotProvided);
        }
        if (number == null) {
            memberMobile.setText(memberInfo.getPhoneNo());
        } else {
            memberMobile.setText(R.string.NoNumber);
        }
        title.setText(memberInfo.getFirstName() + " " + memberInfo.getLastName());
    }

    private void getMemberProfileInfo(String gid, String id) {
        APIInterface apiInterface = APIClient.getClientWithAuth(this)
                .create(APIInterface.class);
        MemberInfo memberInfo = new MemberInfo(gid, id);

        Call<MemberProfile> call = apiInterface.getMemberProfileInfo(memberInfo);
        call.enqueue(new Callback<MemberProfile>() {
            @Override
            public void onResponse(Call<MemberProfile> call, Response<MemberProfile> response) {
                Glide.with(MemberProfileActivity.this).load(response.body().getSuccess().getProfileImage())
                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                        .apply(RequestOptions.placeholderOf(R.drawable.profile).circleCrop())
                        .into(profileImg);
                profileName.setText(response.body().getSuccess().getFirstName()+" "
                        +response.body().getSuccess().getLastName());
                title.setText(response.body().getSuccess().getFirstName()+" "
                        +response.body().getSuccess().getLastName());
                if (response.body().getSuccess().getEmail() != null) {
                    memberEmail.setText(response.body().getSuccess().getEmail());
                } else {
                    memberEmail.setText(R.string.EmailNotProvided);
                }
                if (number != null) {
                    mobileM.setVisibility(View.VISIBLE);
                    memberMobile.setVisibility(View.VISIBLE);
                    memberMobile.setText(number);
                } else {
                    mobileM.setVisibility(View.GONE);
                    memberMobile.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<MemberProfile> call, Throwable t) {

            }
        });
    }
}
