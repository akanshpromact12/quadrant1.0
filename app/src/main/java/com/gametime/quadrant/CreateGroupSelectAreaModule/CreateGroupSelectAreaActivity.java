package com.gametime.quadrant.CreateGroupSelectAreaModule;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gametime.quadrant.Location.GPSTracker;
import com.gametime.quadrant.PermissionsBasePackage.QuadrantPermissionsBaseActivity;
import com.gametime.quadrant.R;
import com.gametime.quadrant.Utils.Network;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CreateGroupSelectAreaActivity extends QuadrantPermissionsBaseActivity implements OnMapReadyCallback, View.OnTouchListener, CreateGroupSelectAreaContract.SelectAreaView {
    private static final String TAG = "CreateGroupSelect";
    private View mMapShelterView;
    private GestureDetector mGestureDetector;
    private ArrayList<LatLng> mLatlngs = new ArrayList<>();
    private ArrayList<LatLng> mLatlngs1 = new ArrayList<>();
    private PolylineOptions mPolylineOptions;
    private PolygonOptions mPolygonOptions;
    private GoogleMap mGoogleMap;

    private boolean mDrawFinished = false;
    private CreateGroupSelectAreaPresenter presenter;
    private TextView cancel;
    private String strLatLng;
    private ImageView drawButton;
    private RelativeLayout okRel;
    private LatLng lng;
    private ProgressBar progressBar;
    private TextView clickNotify;
    private boolean permissionGrant = true;
    private boolean clearMap = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group_select_area);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionGrant = checkLocationPermissions();
        }

        if (permissionGrant) {
            mMapShelterView = findViewById(R.id.drawer_view);
            drawButton = findViewById(R.id.draw_button);
            mGestureDetector = new GestureDetector(this, new GestureListener());
            mMapShelterView.setOnTouchListener(this);
            presenter = new CreateGroupSelectAreaPresenter(this, this);
            okRel = findViewById(R.id.okCancelRelLayout);
            cancel = findViewById(R.id.mapCancel);
            progressBar = findViewById(R.id.progressBar);
            ImageView backButton = findViewById(R.id.backButtonCreateGroup);
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    setResult(RESULT_CANCELED, intent);

                    finish();
                }
            });

            clickNotify = findViewById(R.id.clickNotify);

            drawButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    drawZone(view);
                }
            });

            okRel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick: -> latlng size: " + mLatlngs.size());
                    if (mLatlngs.size() < 2) {
                        Snackbar.make(view,
                                getString(R.string.select_valid_area),
                                Snackbar.LENGTH_LONG).show();
                    } else if (mLatlngs.size() > 1) {
                        presenter.postDetails(lng,
                                strLatLng,
                                CreateGroupSelectAreaActivity.this);
                    }
                }
            });

            setUpMap();

            if (drawButton.getBackground().getConstantState() == ContextCompat.getDrawable(this, R.drawable.rounded_button).getConstantState()) {
                clearMap = false;
            } else {
                clearMap = true;
            }

            if (clearMap) {
                Toast.makeText(this, "clear the map", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "do not clear the map", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_LOCATION:
                if (grantResults.length > 0) {
                    boolean coarseLocation = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean fineLocation = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (coarseLocation && fineLocation) {
                        mMapShelterView = findViewById(R.id.drawer_view);
                        drawButton = findViewById(R.id.draw_button);
                        mGestureDetector = new GestureDetector(this, new GestureListener());
                        mMapShelterView.setOnTouchListener(this);
                        presenter = new CreateGroupSelectAreaPresenter(this, this);
                        okRel = findViewById(R.id.okCancelRelLayout);
                        cancel = findViewById(R.id.mapCancel);
                        progressBar = findViewById(R.id.progressBar);
                        ImageView backButton = findViewById(R.id.backButtonCreateGroup);
                        backButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent();
                                setResult(RESULT_CANCELED, intent);

                                finish();
                            }
                        });
                        clickNotify = findViewById(R.id.clickNotify);

                        drawButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                drawZone(view);
                            }
                        });

                        okRel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Log.d(TAG, "onClick: -> latlng size: " + mLatlngs.size());
                                if (mLatlngs.size() < 2) {
                                    Snackbar.make(view,
                                            getString(R.string.select_valid_area),
                                            Snackbar.LENGTH_LONG).show();
                                } else if (mLatlngs.size() > 1) {
                                    presenter.postDetails(lng,
                                            strLatLng,
                                            CreateGroupSelectAreaActivity.this);
                                }
                            }
                        });

                        setUpMap();
                    } else {
                        requestPermissions(
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                PERMISSION_READWRITE_STORAGE);
                    }
                }
                break;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int X1 = (int) event.getX();
        int Y1 = (int) event.getY();
        Point point = new Point();
        point.x = X1;
        point.y = Y1;
        LatLng firstGeoPoint = mGoogleMap.getProjection().fromScreenLocation(
                point);
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                mPolylineOptions = new PolylineOptions();
                mPolylineOptions.color(ContextCompat.getColor(this, R.color.mapStrokeColor));
                mPolylineOptions.width(10);
                break;

            case MotionEvent.ACTION_MOVE:
                if (mDrawFinished) {
                    X1 = (int) event.getX();
                    Y1 = (int) event.getY();
                    point = new Point();
                    point.x = X1;
                    point.y = Y1;
                    LatLng geoPoint = mGoogleMap.getProjection()
                            .fromScreenLocation(point);
                    mLatlngs.add(geoPoint);

                    mPolylineOptions.add(geoPoint);
                    if (mLatlngs.size() > 0 && mLatlngs.contains(geoPoint) && geoPoint != mLatlngs.get(0)) {
                        //mLatlngs1.add(geoPoint);
                        Log.d(TAG, "Intersecting points: " + geoPoint);
                    }
                    Log.d(TAG, "Number of intersections: " + mLatlngs1.size());

                    mGoogleMap.addPolyline(mPolylineOptions);
                }
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "Poinnts array size " + mLatlngs.size());

                mLatlngs.add(firstGeoPoint);
                Set<LatLng> lngSet = findDuplicates(mLatlngs);
                Log.d(TAG, "Duplicates: " + lngSet.iterator().next());
                Log.d(TAG, "Original size: " + mLatlngs.size());
                mGoogleMap.clear();
                mPolylineOptions = null;
                mMapShelterView.setVisibility(View.GONE);
                mGoogleMap.getUiSettings().setZoomGesturesEnabled(true);
                mGoogleMap.getUiSettings().setAllGesturesEnabled(true);
                if (mLatlngs.size() > 1) {
                    drawButton.setBackgroundResource(R.drawable.rounded_button_change_color);
                    drawButton.setImageResource(R.drawable.ic_mode_edit_24dp);

                    mPolygonOptions = new PolygonOptions();
                    mPolygonOptions.geodesic(true);
                    mPolygonOptions.addAll(mLatlngs);
                    clearMap = true;
                    Log.d(TAG, "mLatLng size: " + mLatlngs.size() + "\nholes size: " + mPolygonOptions.getHoles().size());
                    mPolygonOptions.strokeColor(ContextCompat.getColor(this, R.color.mapStrokeColor));
                    mPolygonOptions.strokeWidth(14);
                    Polygon polygon = mGoogleMap.addPolygon(mPolygonOptions);
                    Log.d(TAG, "stroke joint type: " + polygon.getStrokeJointType());
                    polygon.setFillColor(ContextCompat.getColor(this, R.color.mapFillColor));
                    mPolygonOptions.addHole(mLatlngs).fillColor(Color.RED);
                    Log.d(TAG, "Holes: " + polygon.getHoles());

                    mDrawFinished = false;
                    strLatLng = "";
                    for (int i = 0; i < mLatlngs.size(); i++) {
                        Log.d(TAG, "onTouch: -> coordinates: " + mLatlngs.get(i).toString());
                        if (i == (mLatlngs.size() - 1)) {
                            strLatLng += mLatlngs.get(i).latitude + " " +
                                    mLatlngs.get(i).longitude;
                        } else {
                            strLatLng += mLatlngs.get(i).latitude + " " +
                                    mLatlngs.get(i).longitude + ",";
                        }
                    }

                    Log.d(TAG, "onTouch: -> latLong string: " +
                            strLatLng);
                    lng = presenter.getCenterPoint(mLatlngs);

                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d(TAG, "onClick: cancel clicked");
                            drawButton.setBackgroundResource(R.drawable.rounded_button_change_color);
                            drawButton.setImageResource(R.drawable.ic_mode_edit_24dp);

                            mLatlngs.clear();
                            mGoogleMap.clear();
                            okRel.setEnabled(true);
                        }
                    });

                    Log.d(TAG, "onTouch: -> central lat: " + lng.latitude);
                    Log.d(TAG, "onTouch: -> central long: " + lng.longitude);

                    presenter.postAreaDetails(lng, CreateGroupSelectAreaActivity.this, progressBar, okRel);
                }
                break;
        }
        return mGestureDetector.onTouchEvent(event);
    }

    private Set<LatLng> findDuplicates(ArrayList<LatLng> mLatlngs) {
        final Set<LatLng> setToReturn = new HashSet<>();
        final Set<LatLng> set1 = new HashSet<>();

        for (LatLng latLngList : mLatlngs) {
            if (!set1.add(latLngList)) {
                setToReturn.add(latLngList);
            }
        }

        return setToReturn;
    }

    public void drawZone(View view) {
        /*if (drawButton.getBackground().getConstantState() == ContextCompat.getDrawable(this, R.drawable.rounded_button).getConstantState()) {
            clearMap = false;
        } else {
            clearMap = true;
        }*/

        if (drawButton.getBackground().getConstantState() == ContextCompat.getDrawable(this, R.drawable.rounded_button).getConstantState()/*!clearMap*/) {
            Toast.makeText(this, "clear the map", Toast.LENGTH_SHORT).show();
            drawButton.setBackgroundResource(R.drawable.rounded_button_change_color);
            drawButton.setImageResource(R.drawable.ic_mode_edit_24dp);

            mLatlngs.clear();
            mGoogleMap.clear();
            okRel.setEnabled(true);
        } else {
            Toast.makeText(this, "do not clear the map", Toast.LENGTH_SHORT).show();
            clickNotify.setVisibility(View.GONE);
            drawButton.setBackgroundResource(R.drawable.rounded_button);
            drawButton.setImageResource(R.drawable.ic_mode_edit_black_24dp);

            mGoogleMap.clear();
            mLatlngs.clear();
            mPolylineOptions = null;
            mPolygonOptions = null;
            mDrawFinished = true;
            mMapShelterView.setVisibility(View.VISIBLE);
            mGoogleMap.getUiSettings().setScrollGesturesEnabled(false);
        }
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            return false;
        }
    }

    private void setUpMap() {
        if (getIntent().hasExtra("activityClose")) {
            boolean activityClose = getIntent().getBooleanExtra("activityClose", false);

            if (activityClose) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);

                finish();
            }
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        progressBar.bringToFront();
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        mGoogleMap = map;
        Log.d(TAG, "onMapReady: The map's ready");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
        GPSTracker gps = new GPSTracker(CreateGroupSelectAreaActivity.this);
        LatLng latLng = new LatLng(gps.getLocation().getLatitude(), gps.getLocation().getLongitude());

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 500));
    }

    @Override
    public void showMessage(String msg) {
        Snackbar.make(getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);

        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!Network.isNetworkAvailable(this)) {
            finishAndRemoveTask();
        }
        checkLocationPermissions();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        presenter.onActivityResult(requestCode, resultCode, data);
    }
}
