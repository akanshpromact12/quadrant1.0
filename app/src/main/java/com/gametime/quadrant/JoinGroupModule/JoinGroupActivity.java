package com.gametime.quadrant.JoinGroupModule;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gametime.quadrant.EnterPasswordModule.EnterPasswordActivity;
import com.gametime.quadrant.HomeModule.HomeActivity;
import com.gametime.quadrant.Location.GPSTracker;
import com.gametime.quadrant.Models.Group;
import com.gametime.quadrant.Models.GroupInfo;
import com.gametime.quadrant.Network.APIClient;
import com.gametime.quadrant.Network.APIInterface;
import com.gametime.quadrant.PermissionsBasePackage.QuadrantPermissionsBaseActivity;
import com.gametime.quadrant.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.gametime.quadrant.Utils.Constants.EXTRA_NEARBY_GROUP_OBJ;

public class JoinGroupActivity extends QuadrantPermissionsBaseActivity implements JoinGroupContract.JoinGroupView, LocationListener {
    private static final String TAG = "JoinGroupActivity";
    TextView groupNameToJoin, groupTypeToJoin, NoOfMembersOfGroupToJoin;
    TextView adminOfGroupToJoin;
    Button joinGrp, cancel;
    private ImageView backButton;
    private TextView title;
    SupportMapFragment mapFragment;
    private GoogleMap mGoogleMap;
    GPSTracker gps;
    ArrayList<LatLng> mLatLngs;
    GroupInfo groupInfo;
    JoinGroupPresenter presenter;
    String grpName, grpAccess, grpId, requestType;
    private ProgressBar progressBar;
    private Group groups;
    private boolean permissionGrant = false;
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    private LocationManager locationManager;
    private boolean requested;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);

        //permissionGrant = checkLocationPermissions();

        /*if (isNetworkAvailable(this)) {
            if (permissionGrant) {*/
        mLatLngs = new ArrayList<>();
        groupNameToJoin = (TextView) findViewById(R.id.groupNameToJoin);
        groupTypeToJoin = (TextView) findViewById(R.id.groupTypeToJoin);
        NoOfMembersOfGroupToJoin = (TextView) findViewById(R.id
                .NoOfMembersOfGroupToJoin);
        backButton = (ImageView) findViewById(R.id.backButtonJoinGroup);
        title = (TextView) findViewById(R.id.titleJoinGroup);
        adminOfGroupToJoin = (TextView) findViewById(R.id.adminOfGroupToJoin);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapOfGroupToJoin);
        joinGrp = (Button) findViewById(R.id.joinGroup);
        cancel = (Button) findViewById(R.id.cancelGroupJoin);
        presenter = new JoinGroupPresenter(this);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setIndeterminate(true);
        groups = (Group) getIntent().getSerializableExtra("groups");
        requested = groups.getRequested();
        grpName = groups.getName();
        grpAccess = groups.getAccess();
        requestType = groups.getRequestAcceptType();
        grpId = String.valueOf(groups.getId());
        groupInfo = new GroupInfo();
        title.setText("Join Group");
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();

                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        if (requested) {
            joinGrp.setText(getString(R.string.requestAgain));
        } else {
            joinGrp.setText(getString(R.string.joinGroup));
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);

                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    public void onMapReady(GoogleMap map) {
                        mGoogleMap = map;
                        if (ActivityCompat.checkSelfPermission(JoinGroupActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(JoinGroupActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        map.setMyLocationEnabled(true);

                        getGroupInfo(grpId, mGoogleMap);

                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();

                        setResult(RESULT_CANCELED, intent);
                        finish();
                    }
                });
        /*    }
        } else {
            finishAndRemoveTask();
        }*/
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        mGoogleMap.animateCamera(cameraUpdate);
        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    /*@Override
    protected void onResume() {
        super.onResume();

        if (isNetworkAvailable(this)) {
            finishAndRemoveTask();
        }

        if (permissionGrant) {
            mLatLngs = new ArrayList<>();
            groupNameToJoin = (TextView) findViewById(R.id.groupNameToJoin);
            groupTypeToJoin = (TextView) findViewById(R.id.groupTypeToJoin);
            NoOfMembersOfGroupToJoin = (TextView) findViewById(R.id
                    .NoOfMembersOfGroupToJoin);
            backButton = (ImageView) findViewById(R.id.backButtonJoinGroup);
            title = (TextView) findViewById(R.id.titleJoinGroup);
            adminOfGroupToJoin = (TextView) findViewById(R.id.adminOfGroupToJoin);
            mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.mapOfGroupToJoin);
            joinGrp = (Button) findViewById(R.id.joinGroup);
            cancel = (Button) findViewById(R.id.cancelGroupJoin);
            presenter = new JoinGroupPresenter(this);
            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressBar.setIndeterminate(true);
            groups = (Group) getIntent().getSerializableExtra("groups");
            grpName = groups.getName();
            grpAccess = groups.getAccess();
            requestType = groups.getRequestAcceptType();
            grpId = String.valueOf(groups.getId());
            groupInfo = new GroupInfo();
            title.setText("Join Group");
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();

                    setResult(RESULT_CANCELED, intent);
                    finish();
                }
            });

            mapFragment.getMapAsync(new OnMapReadyCallback() {
                public void onMapReady(GoogleMap map) {
                    mGoogleMap = map;
                    if (ActivityCompat.checkSelfPermission(JoinGroupActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(JoinGroupActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    map.setMyLocationEnabled(true);

                    getGroupInfo(grpId, mGoogleMap);

                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();

                    setResult(RESULT_CANCELED, intent);
                    finish();
                }
            });
        }
    }*/

    private void getGroupInfo(final String grpid, final GoogleMap mGoogleMap) {
        APIInterface apiInterface = APIClient.getClientWithAuth(JoinGroupActivity.this)
                .create(APIInterface.class);

        progressBar.setVisibility(View.VISIBLE);

        Call<GroupInfo> call = apiInterface.getGroupInfo(grpid);
        call.enqueue(new Callback<GroupInfo>() {
            @Override
            public void onResponse(Call<GroupInfo> call, final Response<GroupInfo> response) {
                Log.d(TAG, "onResponse: -> Group Info taken successfully");

                groupInfo = response.body();
                Log.d(TAG, "onResponse: access: " + response.body().getAccess());

                Log.d(TAG, "onResponse: -> response lat: " +
                        response.body().getBoundryCenter().getLat() + " lng: " +
                        response.body().getBoundryCenter().getLong());
                Log.d(TAG, "onResponse: -> response: " + response.body().getAccess());
                groupNameToJoin.append(response.body().getName());
                groupTypeToJoin.append(response.body().getAccess());
                NoOfMembersOfGroupToJoin.append(String.valueOf(response
                        .body().getJoinedUsersCount()));

                if (response.body().getAdminUsers() == null) {
                    adminOfGroupToJoin.append("0");
                } else {
                    adminOfGroupToJoin.append(String.valueOf(response
                            .body().getAdminUsers().size()));
                }

                if (groups.getRequested()) {
                    joinGrp.setText(R.string.requestAgain);
                } else {
                    joinGrp.setText(R.string.joinGroup);
                }

                joinGrp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        if (groupInfo.getAccess().equals("PRIVATE")) {
                            Log.d("::::JoinGroup", "request type: " + requestType);
                            if (requestType.equals("REQUEST_PASSWORD")) {
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(JoinGroupActivity.this);

                                alertDialog.setTitle("Select an option");
                                alertDialog.setMessage("Please select an option whether to join through request, or through password..");
                                alertDialog.setPositiveButton(R.string.request, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        presenter.JoinGroupMethod(view, JoinGroupActivity.this, grpId, groupInfo.getName(), groupInfo.getAccess(), "", "");

                                        dialog.dismiss();
                                    }
                                });
                                alertDialog.setNegativeButton(R.string.password, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(JoinGroupActivity.this,
                                                EnterPasswordActivity.class);
                                        intent.putExtra(EXTRA_NEARBY_GROUP_OBJ, groups);/*
                                        intent.putExtra("grpId", grpId);
                                        Log.d(TAG, "onClick: -> grpid: " + grpId);
                                        intent.putExtra("grpName", grpName);
                                        intent.putExtra("grpAccess", grpAccess);*/

                                        startActivity(intent);
                                        dialog.dismiss();
                                    }
                                });

                                AlertDialog dialog = alertDialog.create();
                                dialog.show();
                            } else if (requestType.equals("MANUAL")) {
                                if (joinGrp.getText().toString()
                                        .equalsIgnoreCase("JOIN")) {
                                    joinGrp.setText(R.string.requestAgain);
                                    presenter.JoinGroupMethod(view, JoinGroupActivity.this, grpId, response.body().getName(), response.body().getAccess(), "", requestType);

                                    Snackbar.make(view, "Your request has successfully been recorded.", Snackbar.LENGTH_LONG).show();
                                } else if (joinGrp.getText().toString()
                                        .equalsIgnoreCase("REQUEST AGAIN")) {
                                    presenter.JoinGroupMethod(view,
                                            JoinGroupActivity.this,
                                            grpId, response.body().getName(),
                                            response.body().getAccess(),
                                            "", requestType);
                                    Snackbar.make(getWindow().getDecorView().getRootView(), "Your request has been again.", Snackbar.LENGTH_LONG).show();
                                }
                            } else if (requestType.equals("PASSWORD")) {
                                Intent intent = new Intent(JoinGroupActivity.this,
                                        EnterPasswordActivity.class);
                                intent.putExtra(EXTRA_NEARBY_GROUP_OBJ, groups);
                                /*intent.putExtra("grpId", grpId);
                                Log.d(TAG, "onClick: -> grpid: " + grpId);
                                intent.putExtra("grpName", grpName);
                                intent.putExtra("grpAccess", grpAccess);*/
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            presenter.JoinGroupMethod(view, JoinGroupActivity.this, grpId, response.body().getName(), response.body().getAccess(), "", "");
                        }
                    }
                });
                /*if (response.body().getAccess().equalsIgnoreCase("PRIVATE")) {
                    if (requestType.equals("REQUEST_PASSWORD")) {
                        joinGrp.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(JoinGroupActivity.this);

                                alertDialog.setTitle("Select an option");
                                alertDialog.setMessage("Please select an option whether to join through request, or through password..");
                                alertDialog.setPositiveButton(R.string.request, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //presenter.JoinGroupMethod(JoinGroupActivity.this, grpId, response.body().getName(), response.body().getAccess(), "", "");

                                        dialog.dismiss();
                                    }
                                });
                                alertDialog.setNegativeButton(R.string.password, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        *//*Intent intent = new Intent(JoinGroupActivity.this,
                                                EnterPasswordActivity.class);
                                        intent.putExtra("grpId", grpId);
                                        Log.d(TAG, "onClick: -> grpid: " + grpId);
                                        intent.putExtra("grpName", grpName);
                                        intent.putExtra("grpAccess", grpAccess);

                                        startActivity(intent);*//*
                                        dialog.dismiss();
                                    }
                                });

                                AlertDialog dialog = alertDialog.create();
                                dialog.show();
                            }
                        });
                    } else if (requestType.equals("MANUAL")) {
                        joinGrp.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (joinGrp.getText().toString().equalsIgnoreCase(getString(R.string.joinGroup))) {
                                    joinGrp.setText(R.string.requestAgain);
                                    Log.d(TAG, "onResponse: -> requested");
                            *//*presenter.JoinGroupMethod(
                                            JoinGroupActivity.this,
                                            grpId, response.body().getName(),
                                            response.body().getAccess(),
                                            "", requestType);*//*

                                } else {
                                    Toast.makeText(JoinGroupActivity.this, "you have requested to join group again", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "onResponse: -> requested again");
                                }
                            }
                        });
                    }
                } else {

                }*/
                /*joinGrp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (response.body().getAccess().equals("PRIVATE")) {
                            if (requestType.equals("REQUEST_PASSWORD")) {
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(JoinGroupActivity.this);

                                alertDialog.setTitle("Select an option");
                                alertDialog.setMessage("Please select an option whether to join through request, or through password..");
                                alertDialog.setPositiveButton(R.string.request, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        presenter.JoinGroupMethod(JoinGroupActivity.this, grpId, response.body().getName(), response.body().getAccess(), "", "");

                                        dialog.dismiss();
                                    }
                                });
                                alertDialog.setNegativeButton(R.string.password, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        *//*Intent intent = new Intent(JoinGroupActivity.this,
                                                EnterPasswordActivity.class);
                                        intent.putExtra("grpId", grpId);
                                        Log.d(TAG, "onClick: -> grpid: " + grpId);
                                        intent.putExtra("grpName", grpName);
                                        intent.putExtra("grpAccess", grpAccess);

                                        startActivity(intent);*//*
                                        dialog.dismiss();
                                    }
                                });

                                AlertDialog dialog = alertDialog.create();
                                dialog.show();
                            } else if (requestType.equals("MANUAL")) {
                                if (joinGrp.getText().equals(getString(R.string.joinGroup))) {
                                    *//*presenter.JoinGroupMethod(
                                            JoinGroupActivity.this,
                                            grpId, response.body().getName(),
                                            response.body().getAccess(),
                                            "", requestType);*//*

                                    joinGrp.setText(R.string.requestAgain);
                                } else {

                                }
                            } else if (requestType.equals("PASSWORD")) {
                                *//*Intent intent = new Intent(JoinGroupActivity.this,
                                        EnterPasswordActivity.class);
                                intent.putExtra("grpId", grpId);
                                Log.d(TAG, "onClick: -> grpid: " + grpId);
                                intent.putExtra("grpName", grpName);
                                intent.putExtra("grpAccess", grpAccess);

                                startActivity(intent);*//*
                            }
                        } else {
                            //presenter.JoinGroupMethod(JoinGroupActivity.this, grpId, response.body().getName(), response.body().getAccess(), "", "");
                        }
                    }
                });*/

                PolygonOptions options = new PolygonOptions().strokeWidth(5)
                        .strokeColor(Color.RED).fillColor(Color.GRAY);

                List<GroupInfo.PolygonPoint> polygonPoints = response.body().getPolygonPoints();
                for (int i=0; i<polygonPoints.size(); i++) {
                    LatLng geoPoints = new LatLng(polygonPoints.get(i).getLat(), polygonPoints.get(i)
                            .getLong());
                    mLatLngs.add(geoPoints);
                }

                options.addAll(mLatLngs);
                mGoogleMap.addPolygon(options);

                Double lt = response.body().getBoundryCenter().getLat();
                Double lg = response.body().getBoundryCenter().getLong();

                //CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(lt, lg));
                CameraUpdate zoom = CameraUpdateFactory.newLatLngZoom(new LatLng(lt, lg), 15);

                //mGoogleMap.moveCamera(center);
                mGoogleMap.animateCamera(zoom);

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<GroupInfo> call, Throwable t) {
                progressBar.setVisibility(View.GONE);

            }
        });
    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_LOCATION:
                if (grantResults.length > 0) {
                    boolean courseLoc = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean fineLocPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (courseLoc && fineLocPermission) {

                    } else {
                        Snackbar.make(this.findViewById(android.R.id.content),
                                "Please Grant Permissions to access location",
                                Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        requestPermissions(
                                                new String[]{Manifest.permission
                                                        .ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                                                PERMISSION_LOCATION);
                                    }
                                }).show();
                    }
                }
                break;
        }
    }*/

    @Override
    public void showMessage(View view, String msg) {
        Snackbar.make(getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showAlertMessage(String msg) {
        AlertDialog.Builder alertJoinGrp = new AlertDialog.Builder(this)
                .setTitle("Group Joined")
                .setMessage(msg)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(JoinGroupActivity.this, HomeActivity.class);
                        startActivity(intent);
                    }
                });
        AlertDialog dialog = alertJoinGrp.create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent();

        setResult(RESULT_CANCELED, intent);
        finish();
    }
}
