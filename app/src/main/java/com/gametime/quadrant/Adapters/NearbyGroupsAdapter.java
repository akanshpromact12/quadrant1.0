package com.gametime.quadrant.Adapters;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gametime.quadrant.GroupMessagesModule.GroupMessageActivity;
import com.gametime.quadrant.JoinGroupModule.JoinGroupActivity;
import com.gametime.quadrant.Models.Group;
import com.gametime.quadrant.Models.QuadrantLoginDetails;
import com.gametime.quadrant.NearbyGroupsModule.NearbyGroupsActivity;
import com.gametime.quadrant.R;
import com.gametime.quadrant.SocketHandler;
import com.gametime.quadrant.Utils.Constants;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;
import static com.gametime.quadrant.Utils.Constants.EXTRA_CHECK_GROUP_JOINED;
import static com.gametime.quadrant.Utils.Constants.EXTRA_GROUP_ACCESS_KEY;
import static com.gametime.quadrant.Utils.Constants.EXTRA_GROUP_ID_KEY;
import static com.gametime.quadrant.Utils.Constants.EXTRA_GROUP_JID;
import static com.gametime.quadrant.Utils.Constants.EXTRA_GROUP_NAME_KEY;
import static com.gametime.quadrant.Utils.Constants.EXTRA_GRP_ACCESS_KEY;
import static com.gametime.quadrant.Utils.Constants.EXTRA_MSG_SENDER_NAME;
import static com.gametime.quadrant.Utils.Constants.EXTRA_NEARBY_GROUP_OBJ;
import static com.gametime.quadrant.Utils.Constants.EXTRA_REQUEST_TYPE;
import static com.gametime.quadrant.Utils.Constants.GROUP_TYPE_PUBLIC;
import static com.gametime.quadrant.Utils.Constants.PREF_KEY_LOGGED_IN_USER;

public class NearbyGroupsAdapter extends RecyclerView.Adapter<NearbyGroupsAdapter.NearbyGroupsViewHolder> {
    private Context context;
    private ArrayList<Group> nearbyGroups = new ArrayList<>();
    private QuadrantLoginDetails login;

    public NearbyGroupsAdapter(Context context, ArrayList<Group> nearbyGroups) {
        this.context = context;
        this.nearbyGroups = nearbyGroups;
        String APICreds = context.getSharedPreferences(Constants.PREF_FILE_NAME,
                Context.MODE_PRIVATE).getString(PREF_KEY_LOGGED_IN_USER, "");
        login = new Gson().fromJson(APICreds, QuadrantLoginDetails.class);
    }

    @Override
    public int getItemCount() {
        return nearbyGroups.size();
    }

    @Override
    public NearbyGroupsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.nearby_groups, parent, false);

        return new NearbyGroupsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NearbyGroupsViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public void onBindViewHolder(final NearbyGroupsViewHolder holder, final int position) {
        final Group nearGroups = nearbyGroups.get(position);

        Log.d(TAG, "onResponse: -> private/public: " +
                nearbyGroups.get(position).getAccess());

        holder.groupPic.setImageResource(R.drawable.group);
        holder.groupPic.setMinimumHeight(40);
        holder.groupPic.setMinimumWidth(40);
        holder.groupName.setText(nearGroups.getName());
        holder.publicPrivateDescriptor.setImageResource(R.drawable
                .ic_lock_white_24dp);

        if (nearbyGroups.get(position).getAccess().equals(GROUP_TYPE_PUBLIC)) {
            Log.d(TAG, "onBindViewHolder: -> print public");
            holder.publicPrivateDescriptor.setVisibility(View.GONE);
        } else {
            holder.publicPrivateDescriptor.setVisibility(View.VISIBLE);
        }

        holder.groupParticipants.setBackgroundResource(R.drawable.rounded_rect);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: -> joined: " + nearbyGroups.get(position).getJoined());
                Log.d(TAG, "onClick: -> getAccess: " + nearbyGroups.get(position).getAccess());

                Intent intent;
                if (nearGroups.getJoined()) {
                    //new EstablishConn().execute();
                    //startMyTask(new EstablishConn());
                    intent = new Intent(context, GroupMessageActivity.class);
                    intent.putExtra(EXTRA_NEARBY_GROUP_OBJ, nearGroups);
                    intent.putExtra(EXTRA_GROUP_ID_KEY, nearGroups.getId());
                    intent.putExtra(EXTRA_GROUP_NAME_KEY, holder.groupName
                            .getText().toString());
                    intent.putExtra(EXTRA_GROUP_ACCESS_KEY, nearGroups.getAccess());
                    intent.putExtra(EXTRA_CHECK_GROUP_JOINED, nearGroups.getJoined());
                    intent.putExtra(EXTRA_MSG_SENDER_NAME, login.getXmppUserDetails().getNick());
                    intent.putExtra(EXTRA_GROUP_JID, nearGroups.getRoomJid());

                    ((Activity)context).startActivityForResult(intent, 1);
                } else {
                    intent = new Intent(context, JoinGroupActivity.class);

                    intent.putExtra(EXTRA_NEARBY_GROUP_OBJ, nearGroups);
                    intent.putExtra(EXTRA_REQUEST_TYPE, nearGroups.getRequestAcceptType());
                    intent.putExtra(EXTRA_GROUP_ID_KEY, String.valueOf(nearGroups.getId()));
                    intent.putExtra(EXTRA_GROUP_NAME_KEY, holder.groupName
                            .getText().toString());
                    intent.putExtra(EXTRA_GRP_ACCESS_KEY,nearGroups.getAccess());

                    ((Activity)context).startActivityForResult(intent, 1);
                    //context.startActivity(intent);
                }
            }

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            private void startMyTask(EstablishConn establishConn) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    establishConn.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    establishConn.execute();
                }
            }
        });

        Log.d(TAG, "onBindViewHolder: -> name id combo: " + nearGroups.getId());
        holder.groupParticipants.setText(String.valueOf(nearGroups.getJoinedUsersCount()));
    }

    static class NearbyGroupsViewHolder extends RecyclerView.ViewHolder {
        TextView groupName, groupParticipants;
        ImageView groupPic, publicPrivateDescriptor;
        CardView cardView;

        NearbyGroupsViewHolder(View itemView) {
            super(itemView);

            groupName = itemView.findViewById(R.id.groupName);
            groupPic = itemView.findViewById(R.id.groupImage);
            publicPrivateDescriptor = itemView.findViewById(R.id
                    .privatePublicDescriptor);
            groupParticipants = itemView.findViewById(R.id.groupParticipants);
            cardView = itemView.findViewById(R.id.card_view);
        }
    }

    private class EstablishConn extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "In PreExecute");
        }

        @Override
        protected Void doInBackground(Void... strings) {
            Log.d(TAG, "In background process");
            /*try {
                Socket socket = SocketHandler.getSocket();

                //Send the message to the server
                OutputStream os = socket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os);
                BufferedWriter bw = new BufferedWriter(osw);

                String sendMessage = "<iq type='get' to='auth' id='auth1'><query xmlns='quadrant:iq:auth'/></iq>";
                bw.write(sendMessage);
                bw.flush();
                System.out.println("Message sent to the server : "+sendMessage);

                //Get the return message from the server
                InputStream is = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String message = br.readLine();
                String str;
                //StringBuilder stringBuilder = new StringBuilder();
                Log.d(TAG, "readline" + br.readLine());
                while ((str = br.readLine()) != null) {
                    //stringBuilder.append(str);
                    System.out.println("The message for the pre-requisites: " + str);
                }
                //Log.d(TAG, "Message received from the server : " + stringBuilder);
                //System.out.println("Message received from the server : " +message);
            } catch (Exception ex) {
                Log.d(TAG, "There was some problem");
                ex.printStackTrace();
            }*//* finally {
                try {
                    socket.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }*/

            return null;
        }
    }
}
