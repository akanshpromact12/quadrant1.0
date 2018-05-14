package com.gametime.quadrant.NearbyGroupsModule;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.gametime.quadrant.Adapters.NearbyGroupsAdapter;
import com.gametime.quadrant.BaseActivity;
import com.gametime.quadrant.Exceptions.GenExceptions;
import com.gametime.quadrant.Location.GPSTracker;
import com.gametime.quadrant.Models.Group;
import com.gametime.quadrant.Models.NearbyGroups;
import com.gametime.quadrant.Network.APIClient;
import com.gametime.quadrant.Network.APIInterface;
import com.gametime.quadrant.R;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.impl.JidCreate;

import java.net.InetAddress;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NearbyGroupsActivity extends BaseActivity {
    private static final String TAG = "NearbyGroupsActivity";
    private RecyclerView nearbyGroups;
    private ArrayList<Group> nearbyGrpList;
    private NearbyGroupsAdapter nearbyGroupsAdapter;
    private Double lat;
    private Double lng;
    GPSTracker gps;
    XMPPTCPConnection connection;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_groups);

        nearbyGroups = (RecyclerView) findViewById(R.id.recycler_view);
        nearbyGrpList = new ArrayList<>();
        username = getIntent().getStringExtra("username");

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        nearbyGroups.setLayoutManager(layoutManager);

        MyXMPPTask task = new MyXMPPTask();
        task.execute("");

        String latLng = getLocation();
        Log.d(TAG, "onCreate: -> loc: " + latLng);

        showNearbyGroups(latLng, nearbyGrpList, nearbyGroups);
        Log.d(TAG, "onResponse: -> list size outside: " +
                nearbyGrpList.size());
    }

    private class MyXMPPTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected String doInBackground(String... strings) {
            try{
                DomainBareJid serviceName = JidCreate.domainBareFrom("jaymin_1@10.1.81.144");
                XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration
                    .builder()
                    .setUsernameAndPassword("jaymin_1",
                            "279ec02696aadc30338cdeeb12552782")
                    .setXmppDomain(serviceName)
                    .setHostAddress(InetAddress.getByName("10.1.81.144"))
                    //.setHost("10.1.81.144")

                    .setConnectTimeout(60000)
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .setDebuggerEnabled(true) // to view what's happening in detail
                    .build();

                XMPPTCPConnection conn1 = new XMPPTCPConnection(config);
                conn1.setUseStreamManagement(true);
                conn1.connect();

                if (conn1.isConnected()) {
                    Log.d(TAG, "onCreate: -> XMPP connection successful");
                }
                conn1.login();

                if (conn1.isAuthenticated()) {
                    Log.d(TAG, "onCreate: -> Connection is authentic");
                }
            } catch (Exception e) {
                Log.d(TAG, "onCreate: -> XMPP Connection unsuccessful\n" +
                        "Error: " + e.getMessage());
                Snackbar.make(getWindow().getDecorView().getRootView(), GenExceptions.fireException(e), Snackbar.LENGTH_LONG).show();
            }

            return "";
        }
    }

    private String getLocation() {
        gps = new GPSTracker(this);

        if (gps.canGetLocation()) {
            lat = gps.getLatitude();
            lng = gps.getLongitude();

            return lat + " " + lng;
        } else {
            gps.showSettingsAlert();

            return "error";
        }
    }

    private void showNearbyGroups(String latLng,
                                  final ArrayList<Group> nearbyGrpList,
                                  final RecyclerView nearbyGroups) {
        APIInterface apiInterface = APIClient.getClient()
                .create(APIInterface.class);
        Call<NearbyGroups> call = apiInterface.getNearbyGroups(latLng);
        call.enqueue(new Callback<NearbyGroups>() {
            @Override
            public void onResponse(Call<NearbyGroups> call, Response<NearbyGroups> response) {
                try {
                    String nmId = "";
                    String joined = "";

                    for (int i=0; i<(response.body().getGroups().size()); i++) {
                        if (i!=(response.body().getGroups().size()-1)) {
                            nmId += response.body().getGroups().get(i).getId() + ":"
                                    + response.body().getGroups().get(i).getName()
                                    + ",";
                            if (!response.body().getGroups().get(i).getJoined()) {
                                joined += response.body().getGroups().get(i)
                                        .getName() + ",";
                            }
                        } else {
                            if (!response.body().getGroups().get(i).getJoined()) {
                                nmId += response.body().getGroups().get(i).getId() + ":"
                                        + response.body().getGroups().get(i).getName();
                                joined += response.body().getGroups().get(i)
                                        .getName();
                            }
                        }
                    }

                    nearbyGroupsAdapter = new NearbyGroupsAdapter(NearbyGroupsActivity.this,
                            nearbyGrpList);
                    nearbyGrpList.clear();

                    nearbyGrpList.addAll(response.body().getGroups());
                    Log.d(TAG, "onResponse: -> list size: " +
                            nearbyGrpList.size());
                    nearbyGroupsAdapter.notifyDataSetChanged();
                    nearbyGroups.setAdapter(nearbyGroupsAdapter);

                } catch (Exception e) {
                    GenExceptions.fireException(e);
                }
            }

            @Override
            public void onFailure(Call<NearbyGroups> call, Throwable t) {
                Log.d(TAG, "Error -> " + t.getMessage());
                Snackbar.make(getWindow().getDecorView().getRootView(), GenExceptions.fireException(t), Snackbar.LENGTH_LONG).show();
            }
        });

    }
}
