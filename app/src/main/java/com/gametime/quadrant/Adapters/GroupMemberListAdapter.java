package com.gametime.quadrant.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.gametime.quadrant.GroupMemberListModule.GroupMemberListActivity;
import com.gametime.quadrant.MemberProfileModule.MemberProfileActivity;
import com.gametime.quadrant.Models.GroupMemberList;
import com.gametime.quadrant.Models.QuadrantLoginDetails;
import com.gametime.quadrant.Network.APIClient;
import com.gametime.quadrant.Network.APIInterface;
import com.gametime.quadrant.R;
import com.gametime.quadrant.Utils.Constants;
import com.google.gson.Gson;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.gametime.quadrant.Utils.Constants.EXTRA_GROUP_ID_KEY;
import static com.gametime.quadrant.Utils.Constants.EXTRA_SUCCESS_ID;
import static com.gametime.quadrant.Utils.Constants.EXTRA_SUCCESS_NUMBER;
import static com.gametime.quadrant.Utils.Constants.EXTRA_SUCCESS__EXTERNAL_ID;
import static com.gametime.quadrant.Utils.Constants.MEMBER_ROLE;
import static com.gametime.quadrant.Utils.Constants.PREF_KEY_LOGGED_IN_USER;

public class GroupMemberListAdapter extends RecyclerView.Adapter<GroupMemberListAdapter.GroupMemberListViewHolder> {
    private Context context;
    private List<GroupMemberList.Success> memberList;
    private String groupId;

    public GroupMemberListAdapter(Context context, List<GroupMemberList.Success> memberList, String groupId) {
        this.context = context;
        this.memberList = memberList;
        this.groupId = groupId;
    }

    @Override
    public GroupMemberListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.group_members_list, null);

        return new GroupMemberListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GroupMemberListViewHolder holder, final int position) {
        final GroupMemberList.Success success = memberList.get(position);
        String loginDetails = context.getSharedPreferences(Constants.PREF_FILE_NAME,
                Context.MODE_PRIVATE).getString(PREF_KEY_LOGGED_IN_USER, "");
        QuadrantLoginDetails login = new Gson().fromJson(loginDetails, QuadrantLoginDetails.class);

        holder.memberName.setText(success.getNick());
        if (login.getXmppUserDetails().getNick().equals(holder.memberName.getText().toString())) {
            holder.userProfile.setVisibility(View.GONE);
        } else {
            holder.userProfile.setVisibility(View.VISIBLE);
        }

        if (success.getRole() != null) {
            try {
                Glide.with(context).load(success.getProfileImage())
                        .apply(RequestOptions.placeholderOf(R.drawable.profile))
                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                        .into(holder.memberProfilePic);
            } catch (Exception ex) {
                Toast.makeText(context, "Profile pic load error", Toast.LENGTH_SHORT).show();
            }
            if (success.getRole().equalsIgnoreCase(MEMBER_ROLE)) {
                holder.memberRole.setText(success.getRole());
                holder.memberRole.setTextColor(Color.CYAN);
            } else {
                holder.memberRole.setText("");
            }
            holder.userProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, MemberProfileActivity.class);
                    intent.putExtra(EXTRA_GROUP_ID_KEY, groupId);
                    intent.putExtra(EXTRA_SUCCESS_ID, String.valueOf(success.getId()));
                    intent.putExtra(EXTRA_SUCCESS__EXTERNAL_ID, String.valueOf(success.getExternalId()));
                    intent.putExtra(EXTRA_SUCCESS_NUMBER, success.getPhoneNo());

                    context.startActivity(intent);
                }
            });
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    GroupMemberListActivity.checkMemberChatExists(success, context, view);
                }
            });
        } else {
            Glide.with(context).load(R.drawable.profile)
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(holder.memberProfilePic);
            holder.userProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alertUserBlocked = new AlertDialog
                            .Builder(context);
                    alertUserBlocked.setTitle("User Blocked");
                    alertUserBlocked.setMessage("The profile of the user you are trying to access has been blocked. Please unblock user to view profile");
                    alertUserBlocked.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog dialog = alertUserBlocked.create();
                    dialog.show();
                }
            });
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    AlertDialog.Builder builderBlockUnblockUser = new AlertDialog.Builder(context);
                    builderBlockUnblockUser.setTitle("Unblock User");
                    builderBlockUnblockUser.setMessage("Do you want to unblock the user?");
                    builderBlockUnblockUser.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            unblockUser(success.getId(), success, context, view);
                            dialogInterface.dismiss();
                        }
                    });
                    builderBlockUnblockUser.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog dialog = builderBlockUnblockUser.create();
                    dialog.show();
                }
            });
        }
    }

    private void unblockUser(final Integer id, final GroupMemberList.Success success, final Context context, final View view) {
        APIInterface apiInterface = APIClient.getClientWithAuth(context)
                .create(APIInterface.class);
        Call<ResponseBody> response = apiInterface.unblockUserFromPM(String.valueOf(id));
        response.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                GroupMemberListActivity.checkMemberChatExists(success, context, view);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    static class GroupMemberListViewHolder extends RecyclerView.ViewHolder {
        ImageView memberProfilePic, userProfile;
        TextView memberName, memberRole;
        CardView cardView;

        GroupMemberListViewHolder(View itemView) {
            super(itemView);

            memberProfilePic = itemView.findViewById(R.id.memberProfilePic);
            userProfile = itemView.findViewById(R.id.userProfile);
            memberName = itemView.findViewById(R.id.memberName);
            memberRole = itemView.findViewById(R.id.memberRole);
            cardView = itemView.findViewById(R.id.joinedGroupsCardView);
        }
    }
}