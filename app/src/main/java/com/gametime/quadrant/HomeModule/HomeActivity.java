package com.gametime.quadrant.HomeModule;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.NavigationMenu;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.gametime.quadrant.Application.MyApp;
import com.gametime.quadrant.CreateGroupSelectAreaModule.CreateGroupSelectAreaActivity;
import com.gametime.quadrant.CreatedGroupsModule.CreatedGroupsFragment;
import com.gametime.quadrant.InvitePeopleModule.InvitePeopleActivity;
import com.gametime.quadrant.InvitesModule.InvitesActivity;
import com.gametime.quadrant.JoinedGroupsModule.JoinedGroupsFragment;
import com.gametime.quadrant.LoginModule.LoginActivity;
import com.gametime.quadrant.Models.QuadrantLoginDetails;
import com.gametime.quadrant.NearbyGroupsModule.NearbyGroupsFragment;
import com.gametime.quadrant.PermissionsBasePackage.QuadrantPermissionsBaseActivity;
import com.gametime.quadrant.PrivateMessageUsersModule.PrivateMessageUsersActivity;
import com.gametime.quadrant.R;
import com.gametime.quadrant.RequestsModule.RequestsActivity;
import com.gametime.quadrant.RequestsModule.RequestsFragment;
import com.gametime.quadrant.SettingsModule.SettingsActivity;
import com.gametime.quadrant.Utils.Constants;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.yavski.fabspeeddial.FabSpeedDial;

import static com.gametime.quadrant.Utils.Constants.PREF_FILE_NAME;
import static com.gametime.quadrant.Utils.Constants.PREF_KEY_LOGGED_IN_USER;
import static com.gametime.quadrant.Utils.Network.isNetworkAvailable;

public class HomeActivity extends QuadrantPermissionsBaseActivity implements HomeContract.homeView, NavigationView.OnNavigationItemSelectedListener {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private static final String TAG = "HomeActivity";
    private FabSpeedDial addGroup;
    HomePresenter presenter;
    String username;
    private QuadrantLoginDetails login;
    TextView invites, appTitle;
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        /*if (!isNetworkAvailable(this)) {
            finish();
        }*/
        appUnderUse();
        Log.d(TAG, "Bool value: " + isAppWentToBg);
        if (isAppWentToBg) {
            Toast.makeText(HomeActivity.this, "HomeActivity", Toast.LENGTH_SHORT).show();
        }
        addGroup = findViewById(R.id.addGroupButton);
        Toolbar toolbar = findViewById(R.id.toolbar);

        appTitle = findViewById(R.id.app_title);
        presenter = new HomePresenter(this);
        invites = findViewById(R.id.seeInvites);

        invites.setText("Invites");
        invites.setVisibility(View.GONE);
        presenter.displayLoggedInUsername(username);
        viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager, true);
        setupTabLayout();
        int tabPosition = tabLayout.getSelectedTabPosition();
        TabLayout.Tab tab = tabLayout.getTabAt(tabPosition);

        if (tab.getPosition()==0) {
            appTitle.setText("Near by Groups");
        } else if (tab.getPosition()==1) {
            appTitle.setText("Joined");
        } else if (tab.getPosition()==2) {
            appTitle.setText("My Groups");
        } else if (tab.getPosition()==3) {
            appTitle.setText("Requests");
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText().toString().equalsIgnoreCase("requests")) {
                    invites.setVisibility(View.VISIBLE);
                } else {
                    invites.setVisibility(View.GONE);
                }

                if (tab.getPosition() == 0) {
                    appTitle.setText("Near by groups");
                } else if (tab.getPosition() == 1) {
                    appTitle.setText("Joined");
                } else if (tab.getPosition() == 2) {
                    appTitle.setText("My Groups");
                } else if (tab.getPosition() == 3) {
                    appTitle.setText("Requests");
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        invites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, InvitesActivity.class);
                startActivity(intent);
            }
        });

        String APICreds = getSharedPreferences(Constants.PREF_FILE_NAME,
                Context.MODE_PRIVATE).getString(PREF_KEY_LOGGED_IN_USER, "");
        login = new Gson().fromJson(APICreds, QuadrantLoginDetails
                .class);

        username = login.getXmppUserDetails().getNick();
        presenter.displayLoggedInUsername(username);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        MenuItem menu = navigationView.getMenu().findItem(R.id.nav_username);
        menu.setTitle("Hi, " + login.getXmppUserDetails().getNick());

        addGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: -> addGrp: add btn clicked");

                addGroup.openMenu();
            }
        });
        addGroup.setMenuListener(new FabSpeedDial.MenuListener() {
            @Override
            public boolean onPrepareMenu(NavigationMenu navigationMenu) {
                return true;
            }

            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                int id = menuItem.getItemId();
                Intent intent;
                
                switch (id) {
                    case R.id.action_menu_create_grp:
                        intent = new Intent(HomeActivity.this,
                                CreateGroupSelectAreaActivity.class);

                        startActivityForResult(intent, 3);
                        break;
                    case R.id.action_menu_pm:
                        intent = new Intent(HomeActivity.this,
                                PrivateMessageUsersActivity.class);
                        intent.putExtra("userId", (String.valueOf(login.getId())));

                        startActivityForResult(intent, 1);
                        break;
                    case R.id.action_menu_invite_people:
                        intent = new Intent(HomeActivity.this,
                                InvitePeopleActivity.class);
                        intent.putExtra("hideText", true);

                        startActivityForResult(intent, 1);
                        break;
                }
                
                return true;
            }

            @Override
            public void onMenuClosed() {

            }
        });
        new XmppConnectionTask().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*if (!isNetworkAvailable(this)) {
            finish();
        }*/
        checkLocationPermissions();
    }

    private void setupTabLayout() {
        tabLayout.getTabAt(0).setText("Near");
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_nearby);
        tabLayout.getTabAt(1).setText("Joined Groups");
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_group);
        tabLayout.getTabAt(2).setText("Created Groups");
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_group);
        tabLayout.getTabAt(3).setText("Requests");
        tabLayout.getTabAt(3).setIcon(R.drawable.mygroups);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new NearbyGroupsFragment(), "");
        adapter.addFragment(new JoinedGroupsFragment(), "");
        adapter.addFragment(new CreatedGroupsFragment(), "");
        adapter.addFragment(new RequestsFragment(), "");

        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            Bundle bundle = new Bundle();

            fragment.setArguments(bundle);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_LOCATION:
                if (grantResults.length > 0) {
                    boolean courseLoc = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean fineLocPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (courseLoc && fineLocPermission) {
                        int tabPosition = tabLayout.getSelectedTabPosition();

                        setupViewPager(viewPager);

                        setupTabLayout();
                        TabLayout.Tab tab = tabLayout.getTabAt(tabPosition);
                        if (tab.getPosition()==0) {
                            appTitle.setText("Near by Groups");
                        } else if (tab.getPosition()==1) {
                            appTitle.setText("Joined");
                        } else if (tab.getPosition()==2) {
                            appTitle.setText("My Groups");
                        } else if (tab.getPosition()==3) {
                            appTitle.setText("Requests");
                        }

                        tab.select();
                    } else {
                        Snackbar.make(this.findViewById(android.R.id.content),
                                "Please Grant Permissions to download and upload photos",
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
    }

    @Override
    public void homeSuccessView(String msg) {
        Snackbar.make(getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_LONG).show();
    }

    class XmppConnectionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            ((MyApp)getApplicationContext()).connectToXmPP();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d(TAG, "Xmpp connection completed");
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent;

        switch (id) {
            case R.id.nav_groups:
                /*intent = new Intent(HomeActivity.this, HomeActivity.class);
                startActivity(intent);

                finish();*/
                drawer.closeDrawers();
                break;
            case R.id.nav_create_new_grp:
                intent = new Intent(HomeActivity.this,
                        CreateGroupSelectAreaActivity.class);
                startActivityForResult(intent, 1);

                break;
            case R.id.nav_friends_and_pm:
                intent = new Intent(HomeActivity.this,
                        PrivateMessageUsersActivity.class);
                intent.putExtra("userId", (String.valueOf(login.getId())));
                startActivityForResult(intent, 1);

                break;
            case R.id.nav_inbox:
                intent = new Intent(HomeActivity.this, RequestsActivity.class);
                startActivityForResult(intent, 1);

                break;
            case R.id.nav_settings:
                intent = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivityForResult(intent, 1);

                break;
            case R.id.nav_contact_us:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://quadrantchat.com/contact-quadrant"));
                startActivityForResult(intent, 1);

                break;
            case R.id.nav_logout:
                logout();

                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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

                        Intent intent = new Intent(HomeActivity
                                .this, LoginActivity.class);

                        startActivity(intent);

                        Snackbar.make(getWindow().getDecorView().getRootView(), username + " logged out..", Snackbar.LENGTH_LONG).show();

                        finish();
                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        AlertDialog alertDialog = alertLogout.create();
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                setupViewPager(viewPager);
            }

            if (resultCode == RESULT_CANCELED) {
                int tabPosition = tabLayout.getSelectedTabPosition();

                setupViewPager(viewPager);

                setupTabLayout();
                TabLayout.Tab tab = tabLayout.getTabAt(tabPosition);
                if (tab.getPosition()==0) {
                    appTitle.setText("Near by Groups");
                } else if (tab.getPosition()==1) {
                    appTitle.setText("Joined");
                } else if (tab.getPosition()==2) {
                    appTitle.setText("My Groups");
                } else if (tab.getPosition()==3) {
                    appTitle.setText("Requests");
                }

                tab.select();
                //viewPager.getAdapter().notifyDataSetChanged();
            }
        }
    }
}