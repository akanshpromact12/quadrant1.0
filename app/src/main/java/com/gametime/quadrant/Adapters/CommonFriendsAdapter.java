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
import com.gametime.quadrant.Models.CommonFriends;
import com.gametime.quadrant.R;

import java.util.ArrayList;

/**
 * Created by Akansh on 30-03-2018.
 */

public class CommonFriendsAdapter extends RecyclerView.Adapter<CommonFriendsAdapter.CommonFriendsViewHolder> {
    private Context context;
    private ArrayList<CommonFriends.AllMutualFriends> friends;

    @Override
    public CommonFriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.common_friends_adapter, parent, false);

        return new CommonFriendsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CommonFriendsViewHolder holder, int position) {
        final CommonFriends.AllMutualFriends commonFriends = friends.get(position);

        holder.friendName.setText(commonFriends.getName());
        Glide.with(context)
                .applyDefaultRequestOptions(RequestOptions.bitmapTransform(new CircleCrop()))
                .load(commonFriends.getPicture().getUrl())
                .apply(RequestOptions.placeholderOf(R.drawable.profile))
                .into(holder.friendImage);
        holder.friendImage.setMaxHeight(commonFriends.getPicture().getHeight());
        holder.friendImage.setMaxWidth(commonFriends.getPicture().getWidth());
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public CommonFriendsAdapter(Context context, ArrayList<CommonFriends.AllMutualFriends> friends) {
        this.context = context;
        this.friends = friends;
    }

    static class CommonFriendsViewHolder extends RecyclerView.ViewHolder {
        ImageView friendImage;
        TextView friendName;

        public CommonFriendsViewHolder(View itemView) {
            super(itemView);

            friendImage = itemView.findViewById(R.id.friendImage);
            friendName = itemView.findViewById(R.id.friendName);
        }
    }
}
