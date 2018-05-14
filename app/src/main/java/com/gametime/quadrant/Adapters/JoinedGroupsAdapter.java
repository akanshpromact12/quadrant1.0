package com.gametime.quadrant.Adapters;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gametime.quadrant.GroupMessagesModule.GroupMessageActivity;
import com.gametime.quadrant.Models.ChatsRealm;
import com.gametime.quadrant.Models.JoinedGroups;
import com.gametime.quadrant.R;
import com.gametime.quadrant.Utils.Constants;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.UUID;

import io.realm.Realm;

import static com.gametime.quadrant.CreatedGroupsModule.CreatedGroupsFragment.TAG;
import static com.gametime.quadrant.Utils.Constants.EXTRA_GROUP_ACCESS_KEY;
import static com.gametime.quadrant.Utils.Constants.EXTRA_GROUP_ID_KEY;
import static com.gametime.quadrant.Utils.Constants.EXTRA_GROUP_JID;
import static com.gametime.quadrant.Utils.Constants.EXTRA_GROUP_NAME_KEY;
import static com.gametime.quadrant.Utils.Constants.EXTRA_JOINED_GROUP_OBJ;
import static com.gametime.quadrant.Utils.Constants.GROUP_TYPE_PRIVATE;

public class JoinedGroupsAdapter extends RecyclerView.Adapter<JoinedGroupsAdapter.JoinedGroupsViewHolder> {
    private Context context;
    private ArrayList<JoinedGroups.Groups> joinedGroups = new ArrayList<>();
    private Realm realm = Realm.getDefaultInstance();

    public JoinedGroupsAdapter(Context context, ArrayList<JoinedGroups.Groups> joinedGroups) {
        this.context = context;
        this.joinedGroups = joinedGroups;
    }

    @Override
    public JoinedGroupsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.joined_groups, parent, false);

        return new JoinedGroupsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final JoinedGroupsViewHolder holder, int position) {
        final JoinedGroups.Groups groups = joinedGroups.get(position);

        //deleteRealm();
        saveToSharedPrefs(groups);
        Log.e(TAG, "Data saved...");
        holder.msgCount.setText(String.valueOf(groups.getOfflineMsgCount()));
        holder.grpName.setText(groups.getName());
        holder.lastMsg.setText(groups.getLastMessage());
        if (groups.getAccess().equals(GROUP_TYPE_PRIVATE)) {
            holder.privatePublicDescriptorJoinedGrp.setVisibility(View.VISIBLE);
        } else {
            holder.privatePublicDescriptorJoinedGrp.setVisibility(View.GONE);
        }
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                realm.beginTransaction();
                new ChatsRealm(UUID.randomUUID().toString(), groups);
                realm.commitTransaction();
                Log.d(TAG, "onClick: -> " + holder.grpName.getText().toString());
                Intent intent = new Intent(context, GroupMessageActivity.class);
                intent.putExtra(EXTRA_GROUP_ID_KEY, groups.getId());
                intent.putExtra(EXTRA_JOINED_GROUP_OBJ, groups);
                intent.putExtra(EXTRA_GROUP_NAME_KEY, holder.grpName.getText()
                        .toString());
                intent.putExtra(EXTRA_GROUP_ACCESS_KEY, groups.getAccess());
                intent.putExtra(EXTRA_GROUP_JID, groups.getRoomJid());
                Log.d(ContentValues.TAG, "onClick: -> Group jid: " + groups.getRoomJid());

                ((Activity)context).startActivityForResult(intent, 1);
            }
        });
    }

    private void saveToSharedPrefs(JoinedGroups.Groups groups) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(groups);

        editor.putString(Constants.PREF_KEY_JOINED_GROUPS_INFO, json).apply();
    }

    private void deleteRealm() {
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
    }

    @Override
    public int getItemCount() {
        return joinedGroups.size();
    }

    static class JoinedGroupsViewHolder extends RecyclerView.ViewHolder {
        TextView grpName, lastMsg, msgCount;
        ImageView privatePublicDescriptorJoinedGrp;
        CardView cardView;

        private JoinedGroupsViewHolder(View itemView) {
            super(itemView);

            msgCount = itemView.findViewById(R.id.msgCount);
            grpName = itemView.findViewById(R.id.joinedGrpName);
            lastMsg = itemView.findViewById(R.id.lastMsgJoinedGroup);
            privatePublicDescriptorJoinedGrp = itemView.findViewById(R
                    .id.privatePublicDescriptorJoinedGrp);
            cardView = itemView.findViewById(R.id.joinedGroupsCardView);
        }
    }
}
