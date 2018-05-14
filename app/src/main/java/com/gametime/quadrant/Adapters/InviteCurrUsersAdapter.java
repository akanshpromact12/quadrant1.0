package com.gametime.quadrant.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.gametime.quadrant.InvitesModule.InvitesItemClickListener;
import com.gametime.quadrant.Models.CurrUsers;
import com.gametime.quadrant.R;

import java.util.ArrayList;
import java.util.List;

public class InviteCurrUsersAdapter extends RecyclerView.Adapter<InviteCurrUsersAdapter.InviteCurrUsersViewHolder> {
    private Context context;
    private List<CurrUsers.Success> currMemberList = new ArrayList<>();
    private String groupId;
    private InvitesItemClickListener itemClickListener;

    public InviteCurrUsersAdapter(Context context, List<CurrUsers.Success> currMemberList, String groupId, InvitesItemClickListener itemClickListener) {
        this.context = context;
        this.currMemberList = currMemberList;
        this.groupId = groupId;
        this.itemClickListener = itemClickListener;
    }

    @Override
    public InviteCurrUsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.invite_curr_users, parent, false);

        return new InviteCurrUsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(InviteCurrUsersViewHolder holder, int position) {
        final CurrUsers.Success successList = currMemberList.get(position);

        Glide.with(context).load(successList.getProfileImage())
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(holder.currUserProfilePic);
        holder.currUserMemberName.setText(successList.getNick());
        holder.currUserMemberName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemClickListener.onItemClick(successList, groupId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return currMemberList.size();
    }

    static class InviteCurrUsersViewHolder extends RecyclerView.ViewHolder {
        ImageView currUserProfilePic;
        TextView currUserMemberName;

        InviteCurrUsersViewHolder(View itemView) {
            super(itemView);

            currUserProfilePic = itemView.findViewById(R.id.currUserProfilePic);
            currUserMemberName = itemView.findViewById(R.id.currUserMemberName);
        }
    }
}
