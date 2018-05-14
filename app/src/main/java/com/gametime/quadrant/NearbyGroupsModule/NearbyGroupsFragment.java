package com.gametime.quadrant.NearbyGroupsModule;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gametime.quadrant.Adapters.NearbyGroupsAdapter;
import com.gametime.quadrant.Exceptions.GenExceptions;
import com.gametime.quadrant.GroupMessagesModule.GroupMessageActivity;
import com.gametime.quadrant.Location.GPSTracker;
import com.gametime.quadrant.Models.Group;
import com.gametime.quadrant.Models.NearbyGroups;
import com.gametime.quadrant.Models.QuadrantLoginDetails;
import com.gametime.quadrant.Models.Requests;
import com.gametime.quadrant.Network.APIClient;
import com.gametime.quadrant.Network.APIInterface;
import com.gametime.quadrant.R;
import com.gametime.quadrant.SocketHandler;
import com.gametime.quadrant.Utils.Constants;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.gametime.quadrant.Utils.Constants.PREF_KEY_LOGGED_IN_USER;

public class NearbyGroupsFragment extends Fragment {
    private static final String TAG = "NearbyGroupsActivity";
    private ArrayList<Group> nearbyGrpList;
    private NearbyGroupsAdapter nearbyGroupsAdapter;
    GPSTracker gps;
    String username;
    private ProgressBar progressBar;
    private TextView noNearbyGrps;
    private String latLng;
    private Context context;
    private RecyclerView nearbyGroups;

    @Override
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && nearbyGroupsAdapter != null) {
            Log.d(TAG, "setUserVisibleHint: -> in NearbyGroups");
            //nearbyGroupsAdapter.notifyDataSetChanged();
            showNearbyGroups(latLng, nearbyGrpList, context);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle bundle) {
        final View rootView = inflater.inflate(R.layout.activity_nearby_groups, container, false);

        context = rootView.getContext();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(context);
        progressBar = rootView.findViewById(R.id.progressBar);
        nearbyGroups = rootView.findViewById(R.id.recycler_view);
        nearbyGrpList = new ArrayList<>();
        noNearbyGrps = rootView.findViewById(R.id.txtViewNearbyGrpsNoGrps);

        startMyTask(new PreRequisite());
        getNumberOfRequests();
        noNearbyGrps.setVisibility(View.GONE);
        progressBar.bringToFront();
        progressBar.setIndeterminate(true);
        Log.d(TAG, "onCreateView: -> username: " + username);

        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);

        nearbyGroups.setLayoutManager(mLayoutManager);
        nearbyGroupsAdapter = new NearbyGroupsAdapter(rootView.getContext(),
                nearbyGrpList);
        nearbyGroups.setAdapter(nearbyGroupsAdapter);

        latLng = getLocation(rootView.getContext());
        Log.d(TAG, "onCreate: -> loc: " + latLng);

        showNearbyGroups(latLng, nearbyGrpList, rootView.getContext());
        Log.d(TAG, "onResponse: -> list size outside: " +
                nearbyGrpList.size());

        return rootView;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startMyTask(PreRequisite preRequisite) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            preRequisite.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            preRequisite.execute();
        }
    }

    private class PreRequisite extends AsyncTask<Void, Void, String> {
        String response = "";

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
            System.out.println("OnCancelled returned string : " + s);
        }

        @Override
        protected String doInBackground(Void... strings) {
            Socket socket = null;

            try {
                //socket = SocketHandler.getSocket();
                String host = "10.1.81.144";
                int port = 2004;
                InetAddress address = InetAddress.getByName(host);
                socket = new Socket(address, port);

                //Send the message to the server
                OutputStream os = socket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os);
                BufferedWriter bw = new BufferedWriter(osw);

                String sendMessage = "GET / HTTP/1.1"+
                        "Host: 10.1.81.144:2004"+
                        "Sec-WebSocket-Extensions: permessage-deflate; client_max_window_bits"+
                        "Connection: upgrade"+
                        "Sec-WebSocket-Version: 13"+
                        "Origin: http://10.1.81.144:2004"+
                        "Upgrade: websocket"+
                        "Sec-WebSocket-Key: zpOY2PAxy6R4MU+EBIv5QA==";
                bw.write(sendMessage);
                bw.flush();
                System.out.println("Message sent to the server : "+sendMessage);

                //Get the return message from the server
                InputStream is = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String message = br.readLine();
                System.out.println("Message received from the server : " +message);
                os.flush();
                osw.flush();
                bw.flush();

                String sendMessage2 = "<iq type='get' to='auth' id='auth1'><query xmlns='quadrant:iq:auth'/></iq>";
                bw.write(sendMessage2);
                bw.flush();
                System.out.println("2nd message sent to the server : "+sendMessage2);
                //Get the return message from the server
                InputStream is2 = socket.getInputStream();
                InputStreamReader isr2 = new InputStreamReader(is2);
                BufferedReader br2 = new BufferedReader(isr2);
                StringBuffer sb = new StringBuffer();
                int value, count = 0;
                boolean ok = false;
                final char[] endMarker = "</iq>".toCharArray();
                char[] tem = new char[5];
                char[] nodeStart = new char[10];
                String s = "";

                while ((value = br2.read()) != -1) {
                    char c = (char) value;
                    s += c;
                    Pattern p = Pattern.compile("\\<.*?\\>");
                    Matcher m = p.matcher(s);
                    if(m.find())
                        //System.out.println(m.group().subSequence(1, 3));
                    //System.out.println("start of nodes : " +s);
                    if(c == '<'){
						/*if (c != '>' && !ok) {
							nodeStart[count] = c;
							System.out.println("Message received from the server nodeStart : " +nodeStart[count]+"count: " + count);
						} else if (c == '>') {
							nodeStart[count] = '>';
							System.out.println("Message received from the server nodeStart : " +nodeStart[count]+"count: " + count);
							ok = true;
						}*/

                        count = 0;
                        tem = new char[5];

                    }
                    if(count <5){
                        tem[count] = c;
                        //System.out.println("Message received from the server tem : " +tem[count] + "count: ");
                    }



                    sb.append(c);
                    count++;

                    //System.out.println("Message received from the server : " +c);

                    // end?  jump out
                    if (Arrays.equals(tem, endMarker)){
                        //System.out.println("equal");
                        break;
                    }


                    //	System.out.println("Message received from the server : " +sb);

                }
                System.out.println("Message received from the server : " +sb.toString());
                response = sb.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            System.out.println("hiiiiiiiiiiiiiiiiiiiii-------------3" + response);

            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            System.out.println("post exeeeeeeeeeeeeeeee");
            System.out.println("The returned string: " + s);
        }
    }

    private void getNumberOfRequests() {
        APIInterface apiInterface = APIClient.getClientWithAuth(context).create(APIInterface.class);
        Call<Requests> call = apiInterface.fetchAllRequests();
        call.enqueue(new Callback<Requests>() {
            @Override
            public void onResponse(Call<Requests> call, Response<Requests> response) {
                if (response.code() == 200) {
                    if (response.body().getGroups().size() > 0) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context)
                                .setMessage("Hi,\nYou have " + response.body().getGroups().size() + " requests pending")
                                .setCancelable(false)
                                .setPositiveButton(context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        Dialog dialog = alertDialog.create();
                        dialog.show();

                        TextView messageView = dialog.findViewById(android.R.id.message);
                        messageView.setTextSize(18);
                        messageView.setGravity(Gravity.CENTER);
                    }
                } else if (response.code() == 500) {
                    GenExceptions.fireException(new Exception("Something went wrong, please try agin later..."));
                } else {
                    Snackbar.make(nearbyGroups, context.getString(R.string.gen_exception_string), Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Requests> call, Throwable t) {
                Snackbar.make(nearbyGroups, context.getString(R.string.gen_exception_string), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private String getLocation(Context context) {
        gps = new GPSTracker(context);

        if (gps.canGetLocation()) {
            Double lat = gps.getLatitude();
            Double lng = gps.getLongitude();

            return lat + " " + lng;
        } else {
            gps.showSettingsAlert();

            return "error";
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            username = savedInstanceState.getString("username");
        }
    }

    private void showNearbyGroups(String latLng,
                                  final ArrayList<Group> nearbyGrpList,
                                  final Context context) {
        progressBar.setVisibility(View.VISIBLE);
        APIInterface apiInterface = APIClient.getClientWithAuth(getActivity())
                .create(APIInterface.class);
        Call<NearbyGroups> call = apiInterface.getNearbyGroups(latLng);
        call.enqueue(new Callback<NearbyGroups>() {
            @Override
            public void onResponse(Call<NearbyGroups> call, Response<NearbyGroups> response) {
                try {
                    if (/*response.body().getGroups() != null &&*/response.code() == 200) {
                        noNearbyGrps.setVisibility(View.GONE);
                        /*for (int i = 0; i < (response.body().getGroups().size()); i++) {
                            if (i != (response.body().getGroups().size() - 1)) {
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
                        }*/

                        nearbyGrpList.clear();
                        nearbyGrpList.addAll(response.body().getGroups());
                        Log.d(TAG, "onResponse: -> list size: " +
                                nearbyGrpList.size());
                        Collections.sort(nearbyGrpList, new Comparator<Group>() {
                            @Override
                            public int compare(Group group, Group t1) {
                                return group.getJoinedUsersCount().compareTo(t1.getJoinedUsersCount());
                            }
                        });
                        nearbyGroupsAdapter.notifyDataSetChanged();
                    } else if (response.code() != 200) {
                        noNearbyGrps.setVisibility(View.GONE);
                        Log.e(TAG, "There was some problem.....");
                        ((Activity) context).finish();
                    } else {
                        noNearbyGrps.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    noNearbyGrps.setVisibility(View.VISIBLE);
                    GenExceptions.fireException(e);
                    ((Activity) context).finish();
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<NearbyGroups> call, Throwable t) {
                GenExceptions.fireException(t);

                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
