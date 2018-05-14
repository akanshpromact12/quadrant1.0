package com.gametime.quadrant.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gametime.quadrant.InvitesModule.ItemClickListener;
import com.gametime.quadrant.Models.Invites;
import com.gametime.quadrant.R;

import java.util.ArrayList;
import java.util.List;

import static com.gametime.quadrant.Exceptions.GenExceptions.TAG;

public class InvitesAdapter extends RecyclerView.Adapter<InvitesAdapter.InvitesViewHolder> {
    private Context context;
    private List<Invites.GroupInv> groupsInv = new ArrayList<>();
    private ItemClickListener itemClickListener;

    public InvitesAdapter(Context context, ItemClickListener itemClickListener, List<Invites.GroupInv> groupInv) {
        this.context = context;
        this.itemClickListener = itemClickListener;
        this.groupsInv = groupInv;
    }

    @Override
    public InvitesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new InvitesViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_invites, parent, false));
    }

    @Override
    public void onBindViewHolder(InvitesViewHolder holder, int position) {
        final Invites.GroupInv groups = groupsInv.get(position);

        holder.inviteeGroupName.setText(groups.getName());
        holder.inviteeGroupName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: -> conte" + context);
                itemClickListener.onItemClick(groups);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groupsInv.size();
    }

    static class InvitesViewHolder extends RecyclerView.ViewHolder {
        TextView inviteeGroupName;

        InvitesViewHolder(View itemView) {
            super(itemView);

            inviteeGroupName = itemView.findViewById(R.id.inviteeGroupName);
        }
    }
}
