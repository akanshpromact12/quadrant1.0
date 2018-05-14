package com.gametime.quadrant.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.gametime.quadrant.Models.PrivateMessageRealm;
import com.gametime.quadrant.Models.PrivateMessagesList;
import com.gametime.quadrant.Models.QuadrantLoginDetails;
import com.gametime.quadrant.PrivateMessagesModule.PrivateMessagesActivity;
import com.gametime.quadrant.R;
import com.gametime.quadrant.Utils.Constants;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

import static android.content.ContentValues.TAG;
import static com.gametime.quadrant.Utils.Constants.PREF_KEY_LOGGED_IN_USER;

public class PrivateMessageUsersAdapter extends RecyclerView.Adapter<PrivateMessageUsersAdapter.PrivateMessageViewHolder> implements Filterable {
    private QuadrantLoginDetails login;
    private Context context;
    private List<PrivateMessagesList.Success> success;
    private List<String> friendNames = new ArrayList<>();
    private Realm realm;

    public PrivateMessageUsersAdapter(Context context) {
        this.context = context;
    }

    public PrivateMessageUsersAdapter(Context context, List<PrivateMessagesList.Success> success) {
        this.context = context;
        this.success = success;
        realm = Realm.getDefaultInstance();
        friendNames.clear();
        if (success != null) {
            for (int i = 0; i < success.size(); i++) {
                friendNames.add(success.get(i).getFirstName() + " " + success.get(i).getLastName());
            }
        }
        String APICreds = context.getSharedPreferences(Constants.PREF_FILE_NAME,
                Context.MODE_PRIVATE).getString(PREF_KEY_LOGGED_IN_USER, "");
        login = new Gson().fromJson(APICreds, QuadrantLoginDetails.class);
    }

    @Override
    public PrivateMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.private_messages, parent, false);

        return new PrivateMessageViewHolder(view);
    }

    @Override
    public Filter getFilter() {


        return null;
    }

    @Override
    public void onBindViewHolder(final PrivateMessageViewHolder holder, final int position) {
        final PrivateMessagesList.Success privateMsg = success.get(position);

        saveToSharedPrefs(privateMsg);
        fetchLastMessage(privateMsg, holder);
        holder.name.setText(success.get(position).getNick());
            Glide.with(context).load(success.get(position).getProfileImage())
                    .apply(RequestOptions.placeholderOf(R.drawable.profile))
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(holder.profile);
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!success.get(position).getNick().equals(login.getXmppUserDetails().getNick())) {
                        Intent intent = new Intent(context, PrivateMessagesActivity.class);
                        intent.putExtra("privateMessages", privateMsg);

                        context.startActivity(intent);
                    } else {
                        Snackbar.make(view, "User cannot chat with himself", Snackbar.LENGTH_LONG).show();
                    }
                }
            });
        //}
    }

    private void saveToSharedPrefs(PrivateMessagesList.Success privateMsg) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(privateMsg);

        editor.putString(Constants.PREF_KEY_PRIVATE_MSG, json).apply();
    }

    private void fetchLastMessage(final PrivateMessagesList.Success privateMsg, final PrivateMessageViewHolder holder) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                final RealmResults<PrivateMessageRealm> realmResults = realm
                        .where(PrivateMessageRealm.class)
                        .findAll();

                //if (privateMsg.getNick().equals(holder.name.getText())) {
                if (realmResults.size() > 0)
                    Log.d(TAG, "last message for " + privateMsg.getNick() + ": " + realmResults.last().getMessage());
                //}
            }
        });
    }

    @Override
    public int getItemCount() {
        return success.size();
    }

    private class ValueFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();

            if (constraint != null && constraint.length() > 0) {
                List<String> filterList = new ArrayList<>();
                for (int i=0; i<success.size(); i++) {
                    if ((success.get(i).getFirstName()+" "+success.get(i).getLastName()).toUpperCase().contains(constraint.toString().toUpperCase())) {
                        filterList.add(success.get(i).getFirstName()+" "+success.get(i).getLastName());
                    }
                }

                filterResults.count = filterList.size();
                filterResults.values = filterList;
            } else {
                filterResults.count = success.size();
                filterResults.values = success;
            }

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            success = (List<PrivateMessagesList.Success>) results.values;
            notifyDataSetChanged();
        }
    }

    static class PrivateMessageViewHolder extends RecyclerView.ViewHolder {
        ImageView profile;
        TextView name;
        CardView cardView;

        PrivateMessageViewHolder(View itemView) {
            super(itemView);
            profile = itemView.findViewById(R.id.PMProfile);
            name = itemView.findViewById(R.id.PMName);
            cardView = itemView.findViewById(R.id.privateMsgUsersCardView);
        }
    }
}