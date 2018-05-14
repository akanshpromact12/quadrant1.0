package com.gametime.quadrant.RequestsModule;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gametime.quadrant.BaseActivity;
import com.gametime.quadrant.Exceptions.GenExceptions;
import com.gametime.quadrant.HomeModule.HomeActivity;
import com.gametime.quadrant.InvitesModule.InvitesActivity;
import com.gametime.quadrant.Models.QuadrantLoginDetails;
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
 * Created by Akansh on 01-12-2017.
 */

public class RequestsActivity extends BaseActivity implements RequestsContract.RequestsView/*, NavigationView.OnNavigationItemSelectedListener*/ {
    RequestsPresenter presenter;
    RecyclerView recyclerView;
    TextView title, noRequestsFound;
    Toolbar toolbar;
    private String APICreds;
    private QuadrantLoginDetails login;
    private ProgressBar progressBar;
    private ImageButton back;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        if (!Network.isNetworkAvailable(this)) {
            finishAndRemoveTask();
        }

        back = findViewById(R.id.backButtonRequests);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.bringToFront();
        progressBar.setIndeterminate(true);
        presenter = new RequestsPresenter(this);
        recyclerView = (RecyclerView) findViewById(R.id.requestsRecyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        title = (TextView) findViewById(R.id.mytext);
        noRequestsFound = (TextView) findViewById(R.id.noRequestsFound);
        toolbar = (Toolbar) findViewById(R.id.toolbarRequests);
        //toolbar.setTitle("Requests");
        /*toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);

                finish();
            }
        });*/
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);

                finish();
            }
        });
        toolbar.showOverflowMenu();
        setSupportActionBar(toolbar);

        /*DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_requests);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();*/

        APICreds = getSharedPreferences(Constants.PREF_FILE_NAME,
                Context.MODE_PRIVATE).getString(PREF_KEY_LOGGED_IN_USER, "");
        login = new Gson().fromJson(APICreds, QuadrantLoginDetails.class);

        /*NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_requests);
        navigationView.setNavigationItemSelectedListener(this);

        MenuItem menu = navigationView.getMenu().findItem(R.id.nav_username);
        menu.setTitle("Hi, " + login.getXmppUserDetails().getNick());*/

        title.setText("Requests");
        noRequestsFound.setText("No requests found");

        presenter.checkAllRequests(this, recyclerView, noRequestsFound);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!Network.isNetworkAvailable(this)) {
            finishAndRemoveTask();
        }
    }

    @Override
    public void viewSuccessMessage(String msg) {
        Snackbar.make(getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void performOprnOnReq(final String action, Integer reqId, final Context context, final View view, final Integer position) {
        APIInterface apiInterface = APIClient.getClientWithAuth(context)
                .create(APIInterface.class);
        Call<ResponseBody> call = apiInterface.AcceptRejectRequests(action, reqId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    Snackbar.make(view, "Request has been accepted",
                            Snackbar.LENGTH_LONG).show();

                    Intent intent = new Intent(context, HomeActivity.class);
                    context.startActivity(intent);
                } else {
                    Snackbar.make(view,
                            "Some problem was encountered while " +
                                    "accepting the request. Please try " +
                                    "again after some time.",
                            Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                GenExceptions.fireException(t);
            }
        });
    }

    /*@Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent;

        switch (id) {
            case R.id.nav_groups:
                intent = new Intent(RequestsActivity.this, HomeActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_create_new_grp:
                intent = new Intent(RequestsActivity.this,
                        CreateGroupSelectAreaActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_friends_and_pm:
                intent = new Intent(RequestsActivity.this,
                        PrivateMessageUsersActivity.class);
                intent.putExtra("userId", (String.valueOf(login.getId())));

                startActivity(intent);
                break;
            case R.id.nav_inbox:
                intent = new Intent(RequestsActivity.this, RequestsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_settings:
                intent = new Intent(RequestsActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_contact_us:
                //go to contact Us Page
                break;
            case R.id.nav_logout:
                logout();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_requests);
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

                        Intent intent = new Intent(RequestsActivity
                                .this, LoginActivity.class);

                        startActivity(intent);

                        Snackbar.make(getWindow().getDecorView().getRootView(), login.getXmppUserDetails().getNick() + " logged out..", Snackbar.LENGTH_LONG).show();

                        finish();
                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(RequestsActivity.this,
                                "clicked no to logout",
                                Toast.LENGTH_SHORT).show();
                    }
                });
        AlertDialog alertDialog = alertLogout.create();
        alertDialog.show();
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_invite_people, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_invite_friends:
                Intent intent = new Intent(RequestsActivity
                        .this, InvitesActivity.class);
                startActivity(intent);

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void progressBarVisibility(int visibility) {
        progressBar.setVisibility(visibility);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);

        finish();
    }
}