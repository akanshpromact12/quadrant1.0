package com.gametime.quadrant.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gametime.quadrant.Models.Requests;
import com.gametime.quadrant.R;
import com.gametime.quadrant.RequestsModule.RequestsContract;
import com.gametime.quadrant.RequestsModule.RequestsFragment;

import java.util.ArrayList;
import java.util.List;

import static com.gametime.quadrant.Utils.Constants.REQUEST_ACCEPT_FLAG;
import static com.gametime.quadrant.Utils.Constants.REQUEST_REJECT_FLAG;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.RequestsViewHolder> {
    private ArrayList<Requests.GroupReq> groupReqs;
    private Context context;
    private List<String> names = new ArrayList<>();

    public RequestsAdapter(ArrayList<Requests.GroupReq> groupReqs, Context context) {
        this.groupReqs = groupReqs;
        this.context = context;
    }

    @Override
    public RequestsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.requests, parent, false);

        return new RequestsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RequestsViewHolder holder, final int position) {
        final Requests.GroupReq groupReq = groupReqs.get(position);
        final int pos = groupReqs.get(position).getRequests().size();

        holder.groupName.setText(groupReq.getName());
        for (int i=0;i<pos; i++) {
            View view = LayoutInflater.from(context).inflate(R.layout.requests_cardview, holder.linearLayout, false);
            final TextView userName = view.findViewById(R.id.txtReqUserName);
            ImageView accept = view.findViewById(R.id.acceptRequest);
            ImageView reject = view.findViewById(R.id.rejectRequest);

            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    /*int acceptId = 0;
                    for (int j=0; j<pos; j++) {
                        acceptId = groupReq.getRequests().get(j).getId();
                    }
                    RequestsContract.RequestsView requestsView = new RequestsFragment();
                    requestsView.performOprnOnReq(REQUEST_ACCEPT_FLAG, acceptId, context, view, position);*/
                    for (int j=0; j<pos; j++) {
                        /*acceptId = groupReq.getRequests().get(j).getId();*/
                        if (userName.getText().equals(groupReq.getRequests().get(j).getFirstName())) {
                            //Log.d("::::RequestsAdapter", "accept Position: " + userName.getText() + " | pos: " + (groupReq.getRequests().get(j).getId()));
                            RequestsContract.RequestsView requestsView = new RequestsFragment();
                            requestsView.performOprnOnReq(REQUEST_ACCEPT_FLAG, groupReq.getRequests().get(j).getId(), context, view, position);
                        }
                    }
                }
            });
            reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (int j=0; j<pos; j++) {
                        /*acceptId = groupReq.getRequests().get(j).getId();*/
                        if (userName.getText().equals(groupReq.getRequests().get(j).getFirstName())) {
                            //Log.d("::::RequestsAdapter", "accept Position: " + userName.getText() + " | pos: " + (groupReq.getRequests().get(j).getId()));
                            RequestsContract.RequestsView requestsView = new RequestsFragment();
                            requestsView.performOprnOnReq(REQUEST_REJECT_FLAG, groupReq.getRequests().get(j).getId(), context, view, position);
                        }
                    }
                    /*int rejectId = 0;
                    for (int k=0; k<pos; k++) {
                        rejectId = groupReq.getRequests().get(k).getId();
                    }
                    RequestsContract.RequestsView requestsView = new RequestsFragment();
                    requestsView.performOprnOnReq(REQUEST_REJECT_FLAG, rejectId, context, view, position);*/
                    Log.d("::::RequestsAdapter", "reject Position: " + pos);
                }
            });

            userName.setText(groupReq.getRequests().get(i).getFirstName());
            holder.linearLayout.addView(view);
        }

        /*holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int acceptId = 0;
                for (int i=0; i<pos; i++) {
                    acceptId = groupReq.getRequests().get(i).getId();
                }
                RequestsContract.RequestsView requestsView = new RequestsFragment();
                requestsView.performOprnOnReq(REQUEST_ACCEPT_FLAG, acceptId, context, view, position);
            }
        });
        holder.reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int rejectId = 0;
                for (int i=0; i<pos; i++) {
                    rejectId = groupReq.getRequests().get(i).getId();
                }
                RequestsContract.RequestsView requestsView = new RequestsFragment();
                requestsView.performOprnOnReq(REQUEST_REJECT_FLAG, rejectId, context, view, position);
            }
        });*/
        //holder.groupName.setText(groupReq.getName());
    }

    @Override
    public int getItemCount() {
        return groupReqs.size();
    }

    static class RequestsViewHolder extends RecyclerView.ViewHolder {
        private TextView groupName;
        private LinearLayout linearLayout;

        RequestsViewHolder(View itemView) {
            super(itemView);

            groupName = itemView.findViewById(R.id.groupNameReqs);
            /*userName = itemView.findViewById(R.id.txtReqUserName);
            accept = itemView.findViewById(R.id.acceptRequest);
            reject = itemView.findViewById(R.id.rejectRequest);*/
            linearLayout = itemView.findViewById(R.id.testLinearLayout);
            //cardView = itemView.findViewById(R.id.requestsCardView);
        }
    }
}