package com.gametime.quadrant.SettingsModule;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gametime.quadrant.BaseActivity;
import com.gametime.quadrant.Exceptions.GenExceptions;
import com.gametime.quadrant.Models.QuadrantLoginDetails;
import com.gametime.quadrant.Models.UserProfile;
import com.gametime.quadrant.Models.UserProfileLocation;
import com.gametime.quadrant.Models.UserProfileStatus;
import com.gametime.quadrant.Network.APIClient;
import com.gametime.quadrant.Network.APIInterface;
import com.gametime.quadrant.R;
import com.gametime.quadrant.Utils.Constants;
import com.gametime.quadrant.Utils.Network;
import com.google.gson.Gson;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.gametime.quadrant.Utils.Constants.PREF_KEY_LOGGED_IN_USER;

/**
 * Created by Akansh on 06-12-2017.
 */

public class SettingsActivity extends BaseActivity/* implements NavigationView.OnNavigationItemSelectedListener*/ {
    private TextView edit, status, name, title, mobile, email;
    private SwitchCompat showLocation;
    private EditText statusEdit;
    private static final String TAG = "SettingsActivity";
    private QuadrantLoginDetails login;
    private ProgressBar progressBar;
    private ImageButton back;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (!Network.isNetworkAvailable(this)) {
            finishAndRemoveTask();
        }

        back = findViewById(R.id.backButtonSettings);
        name = (TextView) findViewById(R.id.nameOfUser);
        edit = (TextView) findViewById(R.id.editButtonSettings);
        status = (TextView) findViewById(R.id.statusSettings);
        showLocation = (SwitchCompat) findViewById(R.id.showLocSwitch);
        mobile = (TextView) findViewById(R.id.mobileTextBox);
        email = (TextView) findViewById(R.id.emailTextBox);
        title = (TextView) findViewById(R.id.mytext);
        statusEdit = (EditText)findViewById(R.id.statusEditSettings);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        title.setText("Settings");
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);

                finish();
            }
        });
        showLocation.setClickable(false);
        statusEdit.setVisibility(View.GONE);

        /*DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_settings);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();*/

        String APICreds = getSharedPreferences(Constants.PREF_FILE_NAME,
                Context.MODE_PRIVATE).getString(PREF_KEY_LOGGED_IN_USER, "");
        login = new Gson().fromJson(APICreds, QuadrantLoginDetails.class);

        /*NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_settings);
        navigationView.setNavigationItemSelectedListener(this);

        MenuItem menu = navigationView.getMenu().findItem(R.id.nav_username);
        menu.setTitle("Hi, " + login.getXmppUserDetails().getNick());*/

        getUserProfileInfo(this, name, email, mobile, status, showLocation, statusEdit, progressBar);

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkEditName(SettingsActivity.this, name, email, mobile, status, showLocation, statusEdit, progressBar);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!Network.isNetworkAvailable(this)) {
            finishAndRemoveTask();
        }
    }

    private void getUserProfileInfo(final Context context, final TextView name, final TextView email, final TextView mobile, final TextView status, final SwitchCompat showLocation, final EditText statusEdit, final ProgressBar progressBar) {
        Boolean locationFlag = getSharedPreferences("locationFlag",
                Context.MODE_PRIVATE).getBoolean("location", false);
        if (locationFlag) {
            showLocation.setChecked(true);
        } else {
            showLocation.setChecked(false);
        }

        APIInterface apiInterface = APIClient.getClientWithAuth(context)
                .create(APIInterface.class);
        Call<UserProfile> callUserProfile = apiInterface.getUserProfile();
        callUserProfile.enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                name.setText(response.body().getNick());
                email.setText(response.body().getEmail());
                mobile.setText(response.body().getPhoneNo());
                if (response.body().getProfileStatus() == null) {
                    status.setText("Status..");
                    statusEdit.setHint("Status..");
                } else {
                    try {
                        byte[] data = Base64.decode(response.body()
                                .getProfileStatus(), Base64.DEFAULT);
                        String statusText = new String(data, "UTF-8");

                        status.setText(statusText);
                        statusEdit.setText(statusText);
                    } catch (Exception ex) {
                        GenExceptions.fireException(ex);
                    }
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    void checkEditName(Context context, TextView name, TextView email, TextView mobile, TextView status, SwitchCompat showLocation, EditText statusEdit, ProgressBar progressBar) {
        if (edit.getText().equals("Done")) {
            edit.setText("Edit");

            progressBar.setIndeterminate(true);
            progressBar.setVisibility(View.VISIBLE);
            showLocation.setEnabled(false);
            sendUserProfileDetails(context, name, status, statusEdit, showLocation, progressBar, email, mobile);

        } else {
            edit.setText("Done");
            this.status.setVisibility(View.GONE);
            this.statusEdit.setVisibility(View.VISIBLE);
            this.showLocation.setClickable(true);
        }
    }

    void sendUserProfileDetails(final Context context, final TextView name, final TextView statusTxt, final EditText status, final SwitchCompat location, final ProgressBar progressBar, final TextView email, final TextView mobile) {
        SharedPreferences sharedPrefs = getSharedPreferences("locationFlag", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        if (location.isChecked()) {
            editor.putBoolean("location", true).apply();
        } else {
            editor.putBoolean("location", false).apply();
        }

        APIInterface apiInterface = APIClient.getClientWithAuth(context)
                .create(APIInterface.class);
        String fieldStatus = "content";
        String fieldLocation = "locationStatus";
        String statusText = "";

        try {
            byte[] data = status.getText().toString().getBytes("UTF-8");
            statusText = Base64.encodeToString(data, Base64.DEFAULT);
        } catch (Exception ex) {
            GenExceptions.fireException(ex);
        }
        UserProfileStatus profileStatus = new UserProfileStatus(name.getText().toString(), statusText);
        Call<ResponseBody> callStatusInfo = apiInterface.sendStatusInfo(fieldStatus, profileStatus);

        String loc = "";
        if (location.isChecked()) {
            loc = "VISIBLE";
        } else {
            loc = "HIDDEN";
        }
        UserProfileLocation profileLocation = new UserProfileLocation(loc);
        Call<ResponseBody> callLocationInfo = apiInterface.sendLocationInfo(fieldLocation, profileLocation);

        callStatusInfo.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "onResponse: -> status was updated successfully");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "onResponse: -> there was some problem. Please try again..");
            }
        });

        callLocationInfo.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Toast.makeText(context, "details editted successfully", Toast.LENGTH_SHORT).show();
                statusTxt.setVisibility(View.VISIBLE);
                statusTxt.setText("Status..");
                status.setVisibility(View.GONE);
                location.setClickable(false);
                getUserProfileInfo(context, name, email, mobile, statusTxt, location, status, progressBar);

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    /*@Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent;

        switch (id) {
            case R.id.nav_groups:
                intent = new Intent(SettingsActivity.this, HomeActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_create_new_grp:
                intent = new Intent(SettingsActivity.this,
                        CreateGroupSelectAreaActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_friends_and_pm:
                intent = new Intent(SettingsActivity.this,
                        PrivateMessageUsersActivity.class);
                intent.putExtra("userId", (String.valueOf(login.getId())));

                startActivity(intent);
                break;
            case R.id.nav_inbox:
                intent = new Intent(SettingsActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_settings:
                intent = new Intent(SettingsActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_contact_us:
                //go to contact Us Page
                break;
            case R.id.nav_logout:
                logout();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_settings);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {
        AlertDialog.Builder alertLogout = new AlertDialog.Builder(this)
                .setTitle("Logout from app")
                .setMessage("Are you sure you want to logout of the app?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences sharedPrefs = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPrefs.edit();
                        editor.clear().apply();

                        LoginManager.getInstance().logOut();

                        Intent intent = new Intent(SettingsActivity
                                .this, LoginActivity.class);

                        startActivity(intent);

                        Snackbar.make(getWindow().getDecorView().getRootView(), login.getXmppUserDetails().getNick() + " logged out..", Snackbar.LENGTH_LONG).show();

                        finish();
                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(SettingsActivity.this,
                                "clicked no to logout",
                                Toast.LENGTH_SHORT).show();
                    }
                });
        AlertDialog alertDialog = alertLogout.create();
        alertDialog.show();
    }*/

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);

        finish();
    }
}