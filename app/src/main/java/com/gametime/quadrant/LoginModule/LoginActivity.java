package com.gametime.quadrant.LoginModule;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.gametime.quadrant.Exceptions.GenExceptions;
import com.gametime.quadrant.HomeModule.HomeActivity;
import com.gametime.quadrant.Location.GPSTracker;
import com.gametime.quadrant.Models.NearbyGroups;
import com.gametime.quadrant.Models.QuadrantLoginDetails;
import com.gametime.quadrant.Network.APIClient;
import com.gametime.quadrant.Network.APIInterface;
import com.gametime.quadrant.PermissionsBasePackage.QuadrantPermissionsBaseActivity;
import com.gametime.quadrant.R;
import com.gametime.quadrant.SendReceiveFromNetwork;
import com.gametime.quadrant.SocketHandler;
import com.gametime.quadrant.Utils.Constants;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.gametime.quadrant.Utils.Constants.PREF_KEY_LOGGED_IN_USER;
import static com.gametime.quadrant.Utils.Network.isNetworkAvailable;

public class LoginActivity extends QuadrantPermissionsBaseActivity implements LoginContract.loginView {
    private static final String TAG = "LoginActivity";
    ImageView fbLogin;
    CallbackManager callbackManager;
    String username;
    LoginPresenter loginPresenter;
    public  static final int PERMISSIONS_MULTIPLE_REQUEST = 123;
    private ProgressBar progressDialog;
    private GPSTracker gps;
    private double lat;
    private double lng;
    private Socket socket;

    /*{
        try {
            IO.Options options = new IO.Options();
            options.timeout = 99999999;
            socket = IO.socket("http://10.1.81.144:2004", options);
            SocketHandler.setSocket(socket);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermissions();
        }
        initializeControls();
        progressDialog.setIndeterminate(true);
        progressDialog.setVisibility(View.GONE);

        Gson gson = new Gson();
        final String APICreds = getSharedPreferences(Constants.PREF_FILE_NAME,
                Context.MODE_PRIVATE).getString(PREF_KEY_LOGGED_IN_USER, "");
        QuadrantLoginDetails login = gson.fromJson(APICreds, QuadrantLoginDetails.class);

        if (!APICreds.isEmpty() ) {
            boolean permissionGrant = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permissionGrant = checkLocationPermissions();
            }

            if (isNetworkAvailable(LoginActivity.this)) {
                if (permissionGrant) {
                    loginToApp(login);
                }
            } else {
                Toast.makeText(LoginActivity.this, "Please enable your internet connection", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        Log.d(TAG, "onCreate: -> XAccessToken: " + APICreds);
        fbLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean permissionGrant = checkLocationPermissions();

                if (isNetworkAvailable(LoginActivity.this)) {
                    if (permissionGrant) {
                        LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile", "user_friends"));
                        loginPresenter.loginWithFB(callbackManager, LoginActivity.this);
                        //establishSocketConn();
                        /*try {
                            socket.connect();
                            Log.e("result socket connect", String.valueOf(socket.connected()));

                            Log.d(TAG, "Socket ready to send....");
                            final String sendMessage = "GET / HTTP/1.1\n" +
                                    "Host: 10.1.81.144:2004\n" +
                                    "Sec-WebSocket-Extensions: permessage-deflate; client_max_window_bits\n" +
                                    "Connection: upgrade\n" +
                                    "Sec-WebSocket-Version: 13\n" +
                                    "Origin: http://10.1.81.144:2004\n" +
                                    "Upgrade: websocket\n" +
                                    "Sec-WebSocket-Key: zpOY2PAxy6R4MU+EBIv5QA==\n";
                            socket.emit("message", sendMessage);
                            socket.on(com.github.nkzawa.socketio.client.Socket.EVENT_CONNECT, new Emitter.Listener() {
                                @Override
                                public void call(Object... args) {
                                    socket.emit("foo", "message sent: " + sendMessage);
                                }
                            }).on("message", new Emitter.Listener() {
                                @Override
                                public void call(Object... args) {
                                    Log.d(TAG, "Received back some data");
                                }
                            }).on(com.github.nkzawa.socketio.client.Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                                @Override
                                public void call(Object... args) {  }
                            }).on(com.github.nkzawa.socketio.client.Socket.EVENT_ERROR, new Emitter.Listener() {
                                @Override
                                public void call(Object... args) {
                                    Log.d(TAG, "There was some issue in the socket connection. Please try again later...");
                                }
                            }).on(com.github.nkzawa.socketio.client.Socket.EVENT_MESSAGE, new Emitter.Listener() {
                                @Override
                                public void call(Object... args) {
                                    Log.d(TAG, "Some message: " + args[0]);
                                }
                            });
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }*/
                        //startMyTask(new EstablishConn());
                        //new EstablishConn().execute();
                    }
                } else {
                    finish();
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startMyTask(EstablishConn establishConn) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                establishConn.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                establishConn.execute();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private class EstablishConn extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... strings) {
            try {
                InetAddress host = InetAddress.getByName("10.1.81.144");
                int port = 2004;
                socket = new Socket(host, port);
                SocketHandler.setSocket(socket);
                //Send the message to the server
                OutputStream os = socket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os);
                BufferedWriter bw = new BufferedWriter(osw);

                String sendMessage = "GET / HTTP/1.1\n" +
                        "Host: 10.1.81.144:2004\n" +
                        "Sec-WebSocket-Extensions: permessage-deflate; client_max_window_bits\n" +
                        "Connection: upgrade\n" +
                        "Sec-WebSocket-Version: 13\n" +
                        "Origin: http://10.1.81.144:2004\n" +
                        "Upgrade: websocket\n" +
                        "Sec-WebSocket-Key: zpOY2PAxy6R4MU+EBIv5QA==\n";
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
                while ((str = br.readLine()) != null) {
                    //stringBuilder.append(str);
                    System.out.println("The message for the hand shake: " + str);
                }
                os.flush();
                osw.flush();
                bw.flush();
                //Log.d(TAG, "Message received from the server : " + stringBuilder);
                //System.out.println("Message received from the server : " +message);
                //Thread.sleep(1000000);
            } catch (Exception ex) {
                Log.d(TAG, "There was some problem");
                ex.printStackTrace();
            }/*finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }*/

            return null;
        }
    }

    private String getLocation(Context context) {
        gps = new GPSTracker(context);

        if (gps.canGetLocation()) {
            lat = gps.getLatitude();
            lng = gps.getLongitude();

            return lat + " " + lng;
        } else {
            gps.showSettingsAlert();

            return "error";
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_READWRITE_STORAGE:
                if (grantResults.length > 0) {
                    boolean readPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (readPermission && writePermission) {

                    } else {
                        Snackbar.make(this.findViewById(android.R.id.content),
                                "Please Grant Permissions to download and upload photos",
                                Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        requestPermissions(
                                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                PERMISSION_READWRITE_STORAGE);
                                    }
                                }).show();
                    }
                }
                break;
            case PERMISSION_LOCATION:
                if (grantResults.length > 0) {
                    boolean courseLoc = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean fineLocPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (courseLoc && fineLocPermission) {

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

    private void loginToApp(final QuadrantLoginDetails login) {
        String latLng = getLocation(LoginActivity.this);

        /*try {
            socket.connect();
            Log.e("result socket connect", String.valueOf(socket.connected()));

            Log.d(TAG, "Socket ready to send....");
            final String sendMessage = "GET / HTTP/1.1\n" +
                    "Host: 10.1.81.144:2004\n" +
                    "Sec-WebSocket-Extensions: permessage-deflate; client_max_window_bits\n" +
                    "Connection: upgrade\n" +
                    "Sec-WebSocket-Version: 13\n" +
                    "Origin: http://10.1.81.144:2004\n" +
                    "Upgrade: websocket\n" +
                    "Sec-WebSocket-Key: zpOY2PAxy6R4MU+EBIv5QA==\n";
            socket.emit("message", sendMessage);
            socket.on(com.github.nkzawa.socketio.client.Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    socket.emit("foo", "Sending test message....");
                }
            }).on("message", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, "Received back some data");
                }
            }).on(com.github.nkzawa.socketio.client.Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {  }
            }).on(com.github.nkzawa.socketio.client.Socket.EVENT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, "There was some issue in the socket connection. Please try again later...");
                }
            }).on(com.github.nkzawa.socketio.client.Socket.EVENT_MESSAGE, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, "Some message: " + args[0]);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }*/
        //startMyTask(new EstablishConn());
        progressDialog.setVisibility(View.VISIBLE);
        APIInterface apiInterface = APIClient.getClientWithAuth(LoginActivity.this)
                .create(APIInterface.class);
        Call<NearbyGroups> call = apiInterface.getNearbyGroups(latLng);
        call.enqueue(new Callback<NearbyGroups>() {
            @Override
            public void onResponse(Call<NearbyGroups> call, Response<NearbyGroups> response) {
                try {
                    if (login != null) {
                        Intent intent = new Intent(LoginActivity.this,
                                HomeActivity.class);
                        intent.putExtra("username", login.getXmppUserDetails().getJid());
                        Log.d(TAG, "Device token: " + login.getToken());

                        startActivity(intent);
                        //finish();

                        progressDialog.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    GenExceptions.fireException(e);
                    finish();

                    progressDialog.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<NearbyGroups> call, Throwable t) {
                GenExceptions.fireException(t);
                progressDialog.setVisibility(View.GONE);
            }
        });
    }

    /*@TargetApi(Build.VERSION_CODES.M)
    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) + ContextCompat
                .checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) + ContextCompat
                .checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) + ContextCompat
                .checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale
                    (this, Manifest.permission.ACCESS_COARSE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale
                            (this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale
                            (this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale
                            (this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Snackbar.make(this.findViewById(android.R.id.content),
                        "Please Grant Permissions to upload profile photo",
                        Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                        new View.OnClickListener() {
                            @TargetApi(Build.VERSION_CODES.M)
                            @Override
                            public void onClick(View v) {
                                requestPermissions(
                                        new String[]{Manifest.permission
                                                .ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        PERMISSIONS_MULTIPLE_REQUEST);
                            }
                        }).show();
            } else {
                requestPermissions(
                        new String[]{Manifest.permission
                                .ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_MULTIPLE_REQUEST);
            }

            return false;
        } else {
            return true;
        }
    }*/

    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_MULTIPLE_REQUEST:
                if (grantResults.length > 0) {
                    boolean fineLocPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean courseLoc = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if(fineLocPermission && courseLoc)
                    {
                        // write your logic here
                    } else {
                        Snackbar.make(this.findViewById(android.R.id.content),
                                "Please Grant Permissions to upload profile photo",
                                Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        requestPermissions(
                                                new String[]{Manifest.permission
                                                        .ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                PERMISSIONS_MULTIPLE_REQUEST);
                                    }
                                }).show();
                    }
                }
                break;
        }
    }*/

    public void initializeControls() {
        callbackManager = CallbackManager.Factory.create();
        fbLogin = (ImageView) findViewById(R.id.FB_LoginButton);
        progressDialog = (ProgressBar) findViewById(R.id.progressBar);

        loginPresenter = new LoginPresenter(this, LoginActivity.this);
    }

    @Override
    public void loginSuccessView(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        Snackbar.make(getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        moveTaskToBack(true);
        finish();
    }

    @Override
    public void progreessBarVisibility(int visibility) {
        progressDialog.setVisibility(visibility);
    }
}
