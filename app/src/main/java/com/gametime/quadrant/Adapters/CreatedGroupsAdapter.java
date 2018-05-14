package com.gametime.quadrant.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gametime.quadrant.GroupMessagesModule.GroupMessageActivity;
import com.gametime.quadrant.Models.CreatedGroups;
import com.gametime.quadrant.R;
import com.gametime.quadrant.Utils.Constants;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class CreatedGroupsAdapter extends RecyclerView.Adapter<CreatedGroupsAdapter.CreatedGroupsViewHolder> {
    private Context context;
    private ArrayList<CreatedGroups.Groupsd> createdGroups;

    @Override
    public CreatedGroupsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.created_group, parent, false);

        return new CreatedGroupsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CreatedGroupsViewHolder holder,
                                 int position) {
        final CreatedGroups.Groupsd createdGrps = createdGroups.get(position);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, GroupMessageActivity.class);
                intent.putExtra(Constants.EXTRA_GROUP_ID_KEY, createdGrps.getId());
                intent.putExtra(Constants.EXTRA_GROUP_NAME_KEY, holder.createdGrpName.getText()
                        .toString());
                intent.putExtra(Constants.EXTRA_GROUP_ACCESS_KEY, createdGrps.getAccess());
                intent.putExtra(Constants.EXTRA_GROUP_JID, createdGrps.getRoomJid());
                intent.putExtra("createdGroups",  createdGrps);
                Log.d(TAG, "onClick: -> Group jid: " + createdGrps.getRoomJid());

                ((Activity)context).startActivityForResult(intent, 1);
            }
        });
        holder.createdGrpName.setText(createdGrps.getName());
    }

    @Override
    public int getItemCount() {
        return createdGroups.size();
    }

    public CreatedGroupsAdapter(Context context, ArrayList<CreatedGroups.Groupsd> createdGroups) {
        this.context = context;
        this.createdGroups = createdGroups;
    }

    public static class CreatedGroupsViewHolder extends RecyclerView.ViewHolder {
        TextView createdGrpName;
        CardView cardView;

        private CreatedGroupsViewHolder(View itemView) {
            super(itemView);

            createdGrpName = itemView.findViewById(R.id.createdGrpName);
            cardView = itemView.findViewById(R.id.createdGroupsCardView);
        }
    }
}
