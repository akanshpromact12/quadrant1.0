package com.gametime.quadrant.InvitesModule;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gametime.quadrant.Adapters.InvitesAdapter;
import com.gametime.quadrant.Models.Invites;
import com.gametime.quadrant.Network.APIClient;
import com.gametime.quadrant.Network.APIInterface;
import com.gametime.quadrant.R;
import com.gametime.quadrant.Utils.Network;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InvitesActivity extends AppCompatActivity implements ItemClickListener {
    RecyclerView recyclerView;
    List<Invites.GroupInv> groups;
    TextView noPendingInvites;
    ImageView back;
    TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invites);

        if (!Network.isNetworkAvailable(this)) {
            finishAndRemoveTask();
        }
        back = findViewById(R.id.backButtonInvites);
        title = findViewById(R.id.InvitesActTitle);
        recyclerView = findViewById(R.id.invitesRecyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        noPendingInvites = findViewById(R.id.noPendingInvites);
        title.setText(R.string.invite_users);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        getInvitationList(this, recyclerView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!Network.isNetworkAvailable(this)) {
            finishAndRemoveTask();
        }
    }

    private void getInvitationList(final ItemClickListener itemClickListener, final RecyclerView recyclerView) {
        APIInterface apiInterface = APIClient.getClientWithAuth(this)
                .create(APIInterface.class);
        Call<Invites> call = apiInterface.getInvitesList();
        call.enqueue(new Callback<Invites>() {
            @Override
            public void onResponse(Call<Invites> call, Response<Invites> response) {
                groups = new ArrayList<>();
                final InvitesAdapter invitesAdapter = new InvitesAdapter(InvitesActivity.this,
                        itemClickListener, response.body().getGroups());
                groups.addAll(response.body().getGroups());
                invitesAdapter.notifyDataSetChanged();

                recyclerView.setAdapter(invitesAdapter);

                if (response.body().getGroups().size() == 0) {
                    noPendingInvites.setText("No pending invites");
                } else {
                    noPendingInvites.setText("");
                }
            }

            @Override
            public void onFailure(Call<Invites> call, Throwable t) {

            }
        });
    }

    public void onItemClick(Invites.GroupInv groupInv) {
        Toast.makeText(InvitesActivity.this, "grp nm: "+groupInv
                .getName()+" grp req no: "+groupInv.getRequest().getId(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }
}
