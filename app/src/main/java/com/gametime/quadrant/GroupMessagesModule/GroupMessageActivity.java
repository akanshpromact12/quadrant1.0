package com.gametime.quadrant.GroupMessagesModule;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gametime.quadrant.Adapters.ChatsAdapter;
import com.gametime.quadrant.Application.MyApp;
import com.gametime.quadrant.Exceptions.GenExceptions;
import com.gametime.quadrant.GroupMemberListModule.GroupMemberListActivity;
import com.gametime.quadrant.JoinGroupModule.JoinGroupActivity;
import com.gametime.quadrant.Models.ChatsRealm;
import com.gametime.quadrant.Models.CreatedGroups;
import com.gametime.quadrant.Models.Group;
import com.gametime.quadrant.Models.GroupMessageRealm;
import com.gametime.quadrant.Models.GroupsPrivate;
import com.gametime.quadrant.Models.JoinedGroups;
import com.gametime.quadrant.Models.MessageParams;
import com.gametime.quadrant.Models.MessageSuccess;
import com.gametime.quadrant.Models.Messages;
import com.gametime.quadrant.Models.QuadrantLoginDetails;
import com.gametime.quadrant.Network.APIClient;
import com.gametime.quadrant.Network.APIInterface;
import com.gametime.quadrant.PermissionsBasePackage.QuadrantPermissionsBaseActivity;
import com.gametime.quadrant.R;
import com.gametime.quadrant.SocketHandler;
import com.gametime.quadrant.Utils.Constants;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.gametime.quadrant.PrivateMessagesModule.PrivateMessagesActivity.getDataColumn;
import static com.gametime.quadrant.PrivateMessagesModule.PrivateMessagesActivity.isDownloadsDocument;
import static com.gametime.quadrant.PrivateMessagesModule.PrivateMessagesActivity.isExternalStorageDocument;
import static com.gametime.quadrant.PrivateMessagesModule.PrivateMessagesActivity.isMediaDocument;
import static com.gametime.quadrant.Utils.Constants.EXTRA_GROUP_NAME_KEY;
import static com.gametime.quadrant.Utils.Constants.PREF_KEY_JOINED_GROUPS_INFO;
import static com.gametime.quadrant.Utils.Constants.PREF_KEY_LOGGED_IN_USER;
import static com.gametime.quadrant.Utils.Network.isNetworkAvailable;

public class GroupMessageActivity extends QuadrantPermissionsBaseActivity implements GroupMessagesContract.GroupMessageView {
    private static final String TAG = "GroupMessageActivity";
    private RecyclerView recyclerView;
    private String groupName;
    private ImageView membersOfGrp, backButton, selectImage;
    private TextView adminOfGroup, groupNamePartOfGrp, NoOfMembers;
    private EditText textToSend;
    private FloatingActionButton send;
    private GroupMessagePresenter presenter;
    private String groupJid;
    private MultiUserChat muc;
    private Group groups;
    private JoinedGroups.Groups joinedGroups;
    private CreatedGroups.Groupsd createdGroups;
    private QuadrantLoginDetails login;
    private String id;
    private int PICK_IMAGE_REQUEST = 1;
    private XMPPTCPConnection conn1;
    private List<MessageSuccess> messageList = new ArrayList<>();
    public ChatsAdapter messagesAdapter;
    private boolean permissionGrant = false;
    private String access;
    private boolean joined;
    private int usersCount;
    private String groupid;
    private String admin = "";
    private boolean loadAdminMessages = false;
    private List<GroupMessageRealm> msgRealm = new ArrayList<>();
    private Realm realm;
    private LinearLayoutManager mLayoutManager;
    private boolean loading = false;
    private boolean loadMoreMsg = false;
    private View progressLayout;
    private ArrayList<String> stanzaId = new ArrayList<>();
    private SocketHandler socketHandler;
    private static final String event = "New Message";
    private final static String MY_PREFS_NAME = "digest";
    Socket socket = null;
    private int size = 0;
    private boolean isFromNotifications = false;

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    {
        try {
            socket = SocketHandler.getSocket();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_message);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionGrant = checkReadWritePermissions();
        }

        System.out.println("Before Authentication Group Messages");

        //startMyTask(new Authentication());
        /*new Thread(new Runnable() {
            String response;
            @Override
            public void run() {
                Socket socket = null;

                try {
                    socket = SocketHandler.getSocket();

                    //Send the message to the server
                    OutputStream os = socket.getOutputStream();
                    OutputStreamWriter osw = new OutputStreamWriter(os);
                    BufferedWriter bw = new BufferedWriter(osw);

                    String sendMessage = "<iq type='set' to='auth' id='auth2'>\n" +
                            "<query xmlns='quadrant:iq:auth'>\n" +
                            "<jabber-username>" + login.getXmppUserDetails().getNick() + "</jabber-username>\n" +
                            "<jabber-password>" + login.getXmppUserDetails().getPassword() + "</jabber-password>\n" +
                            "<device-token>" + UUID.randomUUID() + "</device-token>\n" +
                            "<user-id>" + login.getId() + "</user-id>\n" +
                            "<device-type>Android</device-type>\n" +
                            "</query>\n" +
                            "</iq>";
                    bw.write(sendMessage);
                    bw.flush();
                    System.out.println("Message sent to the server : "+sendMessage);
                    //Get the return message from the server
                *//*InputStream is = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                //System.out.println("Message after" + isr.read());
                BufferedReader br = new BufferedReader(isr);
                System.out.println("Message before");
                //String message = br.readLine();

                String str;
                //StringBuilder stringBuilder = new StringBuilder();
                while ((str = br.readLine()) != null) {
                    //stringBuilder.append(str);
                    System.out.println("The message for the hand shake: " + str);
                }
                os.close();
                osw.close();
                bw.close();
                is.close();
                isr.close();
                br.close();*//*
                   *//* ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(
                            1024);
                    byte[] buffer = new byte[1024];

                    int bytesRead;
                    InputStream inputStream = socket.getInputStream();*//*

			*//*
             * notice: inputStream.read() will block if no data return
			 *//*
                    *//*while ((bytesRead = inputStream.read(buffer)) != -1) {
                        byteArrayOutputStream.write(buffer, 0, bytesRead);
                        response += byteArrayOutputStream.toString("UTF-8");
                    }*//*
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    Log.d(TAG, "Some error occurred: " + e.getMessage());
                } finally {
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();*/
        /*try {
            *//*String sendMessage = "<iq type='get' to='auth' id='auth1'><query xmlns='quadrant:iq:auth'/></iq>";
            SendReceiveFromNetwork sendToNet = new SendReceiveFromNetwork(sendMessage);
            String textReceived = sendToNet.startMyTask();
            Log.d(TAG, "Text Received: " + textReceived);*//*
            Log.e("result socket connect", String.valueOf(socket.connected()));

            Log.d(TAG, "Socket ready to send....");
            final String sendMessage = "<iq type='get' to='auth' id='auth1'><query xmlns='quadrant:iq:auth'/></iq>";
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
        //new Authentication().execute();
        System.out.println("After Authentication Group Messages");
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Toast.makeText(this, "in Group messages", Toast.LENGTH_SHORT).show();
        progressLayout = findViewById(R.id.progressLayout);
        socketHandler = new SocketHandler();
        recyclerView = (RecyclerView) findViewById(R.id.msg_recycler_view);
        groupName = getIntent().getStringExtra(EXTRA_GROUP_NAME_KEY);
        membersOfGrp = (ImageView) findViewById(R.id.membersOfGrp);
        adminOfGroup = (TextView) findViewById(R.id.adminOfGroup);
        groupNamePartOfGrp = (TextView) findViewById(R.id.groupNamePartOfGrp);
        NoOfMembers = (TextView) findViewById(R.id.NoOfMembers);
        textToSend = (EditText) findViewById(R.id.textToSend);
        send = (FloatingActionButton) findViewById(R.id.sendButton);
        mLayoutManager = new LinearLayoutManager(this);
        groups = (Group) getIntent().getSerializableExtra("groups");
        joinedGroups = (JoinedGroups.Groups) getIntent().getSerializableExtra("joinedGroups");
        createdGroups = (CreatedGroups.Groupsd) getIntent().getSerializableExtra("createdGroups");
        isFromNotifications = getIntent().getBooleanExtra(getString(R.string.from_notifications), false);
        backButton = (ImageView) findViewById(R.id.backButton);
        selectImage = (ImageView) findViewById(R.id.selectImage);
        messagesAdapter = new ChatsAdapter(GroupMessageActivity.this, msgRealm, false, "");
        recyclerView.setAdapter(messagesAdapter);
        groupid = ""+getIntent().getIntExtra(Constants.EXTRA_GROUP_ID_KEY, 0);
        realm = Realm.getDefaultInstance();
        presenter = new GroupMessagePresenter(this, this, realm, msgRealm, messagesAdapter);
        Gson gson = new Gson();
        final String joinedGrps = getSharedPreferences(Constants.PREF_FILE_NAME, MODE_PRIVATE)
                .getString(PREF_KEY_JOINED_GROUPS_INFO, "");
        GroupsPrivate groupsPrivate = gson.fromJson(joinedGrps, GroupsPrivate.class);

        /*final MemberListParams memberListParams = new MemberListParams(groupid);

        APIInterface apiInterface = APIClient.getClientWithAuth(this)
                .create(APIInterface.class);
        Call<GroupMemberList> call = apiInterface.getAllMembersOfGrp(memberListParams);
        call.enqueue(new Callback<GroupMemberList>() {
            @Override
            public void onResponse(Call<GroupMemberList> call, Response<GroupMemberList> response) {
                if (response.body().getSuccess() != null) {
                    for (int i=0; i<response.body().getSuccess().size(); i++) {
                        if (response.body().getSuccess().get(i).getRole().equalsIgnoreCase("admin")) {
                            admin = response.body().getSuccess().get(i).getNick();
                        }
                    }
                } else {
                    Toast.makeText(GroupMessageActivity.this, "some Error occured..", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GroupMemberList> call, Throwable t) {

            }
        });*/

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int itemCount = recyclerView.getLayoutManager().getItemCount();
                int lastPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition() + 1;

                if (!loading && itemCount == lastPosition && msgRealm != null && !loadMoreMsg) {
                    progressLayout.setVisibility(View.VISIBLE);
                    Log.d(TAG, "getOlderMessages");
                    Toast.makeText(GroupMessageActivity.this, "getOlderMessages", Toast.LENGTH_SHORT).show();

                    if (stanzaId.size() > 0) {
                        getNewMessages(stanzaId.get(stanzaId.size()-1));
                    } else {
                        progressLayout.setVisibility(View.GONE);
                    }

                    //loading = true;
                }
            }
        });

        if (!isFromNotifications) {
            if (groups != null) {
                groupJid = groups.getRoomJid();
                groupid = groups.getId().toString();
                groupName = groups.getName();
                //getJabberRoomStatus(this, "join", String.valueOf(groups.getId()));
                Log.d(TAG, "onCreate: -> group: " + groups.getName());
            } else if (joinedGroups != null) {
                groupJid = joinedGroups.getRoomJid();
                groupid = joinedGroups.getId().toString();
                groupName = joinedGroups.getName();
                getJabberRoomStatus(this, "join", String.valueOf(joinedGroups.getId()));
                Log.d(TAG, "onCreate: -> group: " + joinedGroups.getName());
            } else if (createdGroups != null) {
                groupJid = createdGroups.getRoomJid();
                groupid = createdGroups.getId().toString();
                groupName = createdGroups.getName();
                Log.d(TAG, "onCreate: -> group: " + createdGroups.getName());
            }
        } else {
            //realm.beginTransaction();
            /*realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<ChatsRealm> realmResults = realm
                            .where(ChatsRealm.class).findAll();

                    if (realmResults.size() > 0) {
                        groupJid = realmResults.get(0).getRoomJid();
                        groupid = realmResults.get(0).getId().toString();
                        groupName = realmResults.get(0).getName();
                    }
                }
            });*/
            if (groupsPrivate != null) {
                groupJid = groupsPrivate.getRoomJid();
                groupid = groupsPrivate.getId().toString();
                groupName = groupsPrivate.getName();
            }
        }

        /*realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.delete(GroupMessageRealm.class);
                msgRealm.clear();
                if (msgRealm.size() > 0) {
                    getMessagesLocally();
                }
            }
        });*/
        if (!isNetworkAvailable(this))
            fetchGroupMessages(msgRealm, groupJid);
        else {
            deleteRecords();
            getMessagesLocally();
        }
        /*if (msgRealm.size() > 0) {
            getNewMessages(msgRealm.get(msgRealm.size()-1).getStanzId());
        }*/

        Log.d(TAG, "onCreate: -> groupJid: " + groupJid);
        Log.d(TAG, "onCreate: -> In message...");

        Log.d(TAG, "onCreate: -> outside try");
        String APICreds = getSharedPreferences(Constants.PREF_FILE_NAME,
                Context.MODE_PRIVATE).getString(PREF_KEY_LOGGED_IN_USER, "");
        login = new Gson().fromJson(APICreds, QuadrantLoginDetails.class);

        membersOfGrp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupMessageActivity.this, GroupMemberListActivity.class);

                if (groups != null) {
                    intent.putExtra("groups", (Serializable) groups);
                    Log.d(TAG, "onClick: -> groups, id: " + groups.getId());
                } else if (joinedGroups != null) {
                    intent.putExtra("joinedGroups", (Serializable) joinedGroups);
                    Log.d(TAG, "onClick: -> joinedGroups, id: " + joinedGroups.getId());
                } else if (createdGroups != null) {
                    intent.putExtra("createdGroups", (Serializable) createdGroups);
                    Log.d(TAG, "onClick: -> createdGroups, id: " + createdGroups.getId());
                }

                startActivity(intent);
            }
        });

        conn1 = ((MyApp) getApplicationContext()).getXMPPConnection();
        if (conn1 != null) {
            Log.d(TAG, "onCreate: -> conn1 isn't null");
            Log.d(TAG, "onCreate: -> connected: "+conn1.isConnected());
            Log.d(TAG, "onCreate: -> auth: " + conn1.isAuthenticated());

            send.setClickable(true);
        } else {
            Log.d(TAG, "onCreate: -> conn1 is null");
            send.setClickable(false);
        }

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        try {
            MultiUserChatManager mucManager = MultiUserChatManager.getInstanceFor(conn1);
            EntityBareJid bareJid = JidCreate.entityBareFrom(groupJid);
            muc = mucManager.getMultiUserChat(bareJid);
            Resourcepart resourcepart = Resourcepart.from(login
                    .getXmppUserDetails().getNick());
            muc.join(resourcepart);
        } catch (Exception ex) {
            GenExceptions.logException(ex);
        }

        muc.addMessageListener(messageListener);
        Log.d(TAG, "size: " + msgRealm.size());
        //if (msgRealm.size() == 0) {
            //muc.addMessageListener(messageListener);
        //}

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!textToSend.getText().toString().trim().isEmpty()) {
                    try {
                        startStepFour(new StepFour());
                        muc.sendMessage(textToSend.getText().toString());
                        final String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                                Locale.getDefault()).format(new Date());
                        Log.d("::::Group",  "Body: " + muc.nextMessage().getBody());
                        /*addMessages(login.getXmppUserDetails().getNick(), textToSend.getText().toString(),
                                stanzaid+"", date, login.getXmppUserDetails().getNick(), "xml",
                                textToSend.getText().toString(),
                                "", "", "", groupJid);*/
                        try {
                            addMessages(login.getXmppUserDetails().getNick(), textToSend.getText().toString(),
                                    "sjfsdnjfdn", date, login.getXmppUserDetails().getNick(), "xml",
                                    textToSend.getText().toString(),
                                    "", "", "", groupJid);
                            /*realm.beginTransaction();
                            GroupMessageRealm groupMessageRealm = realm.createObject(GroupMessageRealm.class, UUID.randomUUID().toString());
                            groupMessageRealm.setNick(login.getXmppUserDetails().getNick());
                            groupMessageRealm.setBody(textToSend.getText().toString());
                            groupMessageRealm.setStanzId("sjfsdnjfdn");
                            groupMessageRealm.setTime(date);
                            groupMessageRealm.setPeer(login.getXmppUserDetails().getJid().toString());
                            groupMessageRealm.setXml(muc.nextMessage().toXML().toString());
                            groupMessageRealm.setText(textToSend.getText().toString());
                            groupMessageRealm.setThumbnailUrl("");
                            groupMessageRealm.setImageUrl("");
                            groupMessageRealm.setImageName("");
                            groupMessageRealm.setGroupJid(groupJid);
                            realm.commitTransaction();
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    RealmResults<GroupMessageRealm> realmResults = realm.where(GroupMessageRealm.class)
                                            .equalTo("groupJid", groupJid)
                                            .findAll();

                                    msgRealm.add(0, realmResults.last());
                                    messagesAdapter.notifyDataSetChanged();
                                }
                            });*/
                        } catch (Exception ex) {
                            realm.cancelTransaction();
                        }
                        /*addMessages(login.getXmppUserDetails().getNick(),
                        textToSend.getText().toString(),
                                muc.nextMessage().getStanzaId(),
                                date, login.getXmppUserDetails().getJid().toString(),
                                muc.nextMessage().toXML().toString(),
                                textToSend.getText().toString(),
                                "",
                                "",
                                "",
                                groupJid);*/
                        /*realm.beginTransaction();
                        GroupMessageRealm groupMessageRealm = realm.createObject(GroupMessageRealm.class, UUID.randomUUID().toString());
                        groupMessageRealm.setNick(login.getXmppUserDetails().getNick());
                        groupMessageRealm.setBody(textToSend.getText().toString());
                        groupMessageRealm.setStanzId(muc.nextMessage().getStanzaId());
                        groupMessageRealm.setTime(date);
                        groupMessageRealm.setPeer(login.getXmppUserDetails().getJid().toString());
                        groupMessageRealm.setXml(muc.nextMessage().toXML().toString());
                        groupMessageRealm.setText(textToSend.getText().toString());
                        groupMessageRealm.setThumbnailUrl("");
                        groupMessageRealm.setImageUrl("");
                        groupMessageRealm.setImageName("");
                        groupMessageRealm.setGroupJid(groupJid);
                        realm.commitTransaction();
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                RealmResults<GroupMessageRealm> realmResults = realm.where(GroupMessageRealm.class)
                                        .equalTo("groupJid", groupJid)
                                        .findAll();

                                msgRealm.add(0, realmResults.last());
                                messagesAdapter.notifyDataSetChanged();
                            }
                        });*/
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "onClick: -> send button clicked...");
                    //textToSend.setText("");
                }
            }
        });

        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(mLayoutManager);

        groupNamePartOfGrp.setText(groupName);
        access = "";
        joined = false;
        usersCount = 0;

        if (groups != null) {
            access = groups.getAccess();
            joined = groups.getJoined();
            usersCount = groups.getJoinedUsersCount();
        } else if (joinedGroups != null) {
            access = joinedGroups.getAccess();
            joined = joinedGroups.getJoined();
            usersCount = joinedGroups.getJoinedUsersCount();
        } else if (createdGroups != null) {
            access = createdGroups.getAccess();
            joined = createdGroups.getJoined();
            usersCount = createdGroups.getJoinedUsersCount();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionGrant = checkReadWritePermissions();
        }

        if (permissionGrant) {
            loadMessages(access, joined, usersCount);
        }

        if (!joined && adminOfGroup.getText().equals("Join")) {
            adminOfGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int id = 0;

                    if (groups != null) {
                        id = groups.getId();
                    } else if (joinedGroups != null) {
                        id = joinedGroups.getId();
                    } else if (createdGroups != null) {
                        id = createdGroups.getId();
                    }
                    Intent intent = new Intent(GroupMessageActivity.this,
                            JoinGroupActivity.class);

                    intent.putExtra(Constants.EXTRA_GROUP_ID_KEY, String.valueOf(id));
                    startActivity(intent);
                }
            });
        } else {
            adminOfGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadAdminMessages = !loadAdminMessages;
                }
            });
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (joinedGroups != null) {
                    getJabberRoomStatus(GroupMessageActivity.this, "left", String.valueOf(joinedGroups.getId()));
                }

                Intent intent = new Intent();
                intent.putExtra("msg", "hello");
                setResult(RESULT_CANCELED, intent);

                finish();
            }
        });
    }

    private void deleteRecords() {
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startStepFour(StepFour stepFour) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            stepFour.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            stepFour.execute();
        }
    }

    private class StepFour extends AsyncTask<Void, Void, String> {
        String response = "";
        String APICreds = getSharedPreferences(Constants.PREF_FILE_NAME,
                Context.MODE_PRIVATE).getString(PREF_KEY_LOGGED_IN_USER, "");
        QuadrantLoginDetails login = new Gson().fromJson(APICreds, QuadrantLoginDetails.class);

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
            System.out.println("OnCancelled returned string : " + s);
        }

        @Override
        protected String doInBackground(Void... strings) {
            String registrationToken = FirebaseInstanceId.getInstance().getToken();
            Log.d(TAG, "FCM Registration Token: " + registrationToken);

            try {
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

                String sendMessage2 = "<iq type='set' to='auth' id='auth2'>\n" +
                        "<query xmlns='quadrant:iq:auth'>\n" +
                        "<jabber-username>" + login.getXmppUserDetails().getJid().split("@")[0] + "</jabber-username>\n" +
                        "<jabber-password>" + login.getXmppUserDetails().getPassword() + "</jabber-password>\n" +
                        "<device-token>" + registrationToken + "</device-token>\n" +
                        "<user-id>" + login.getId() + "</user-id>\n" +
                        "<device-type>Android</device-type>\n" +
                        "</query>\n" +
                        "</iq>\n";
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

                String sendMessage3 = "<iq type='set' to='push' id='send'>\n" +
                                "<receiver id='" + groupid + "' type='group'>\n" +
                                "<message id='" + UUID.randomUUID() + "' uniqueMessageID='19EB6DC4-8CA8-4E67-91C9-E779BE0DC0CD' chatRoomID='" + muc.getRoom() + "' to='" + muc.getRoom() + "' type='groupchat'><body>" + textToSend.getText().toString() + "</body></message>\n" +
                                "</receiver>\n" +
                                "<sender name='" + login.getXmppUserDetails().getNick() + "'>" + groupName + "</sender>\n" +
                                "</iq>\n";
                bw.write(sendMessage3);
                bw.flush();
                System.out.println("2nd message sent to the server : "+sendMessage3);
                //Get the return message from the server
                InputStream is3 = socket.getInputStream();
                InputStreamReader isr3 = new InputStreamReader(is3);
                BufferedReader br3 = new BufferedReader(isr3);
                StringBuffer sb1 = new StringBuffer();
                int value1, count1 = 0;
                boolean ok1 = false;
                final char[] endMarker1 = "</iq>".toCharArray();
                char[] temp = new char[5];
                char[] nodeStart1 = new char[10];
                String s1 = "";

                while ((value1 = br3.read()) != -1) {
                    char ch = (char) value1;
                    s1 += ch;
                    Pattern p = Pattern.compile("\\<.*?\\>");
                    Matcher m = p.matcher(s1);
                    if(m.find())
                        //System.out.println(m.group().subSequence(1, 3));
                        //System.out.println("start of nodes : " +s);
                        if(ch == '<'){
                            count1 = 0;
                            temp = new char[5];

                        }
                    if(count1 <5){
                        temp[count1] = ch;
                        Log.d(TAG, "char 1 " + ch);
                    }

                    for (char aTemp : temp) {
                        Log.d(TAG, "char 2 " + String.valueOf(aTemp));
                    }
                    Log.d(TAG, "char 3 " + ch);
                    sb1.append(ch);
                    count1++;

                    if (Arrays.equals(temp, endMarker1)){
                        Log.d(TAG, "equal");
                        break;
                    } else {
                        Log.d(TAG, "not equal");
                    }
                }
                System.out.println("Message received from the server : " +sb1.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                Log.d(TAG, "Some error occurred: " + e.getMessage());
            } /*finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }*/

            System.out.println("hiiiiiiiiiiiiiiiiiiiii-------------3" + response);

            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            textToSend.setText("");
            System.out.println("post exeeeeeeeeeeeeeeee");
            System.out.println("The returned string: " + s);
            String digest;

            if (!s.equals("")) {
                Document doc = convertStringToXML(s);
                NodeList nodes = doc.getElementsByTagName("query");
                if (nodes.getLength() > 0) {
                    Element err = (Element) nodes.item(0);
                    System.out.println(err.getElementsByTagName("digest")
                            .item(0)
                            .getTextContent());
                    SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putString("digest", err.getElementsByTagName("digest").item(0).getTextContent());
                    editor.apply();
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startMyTask(Authentication authentication) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            authentication.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            authentication.execute();
        }
    }

    private class Authentication extends AsyncTask<Void, Void, String> {
        String response = "";
        String APICreds = getSharedPreferences(Constants.PREF_FILE_NAME,
                Context.MODE_PRIVATE).getString(PREF_KEY_LOGGED_IN_USER, "");
        QuadrantLoginDetails login = new Gson().fromJson(APICreds, QuadrantLoginDetails.class);

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
            System.out.println("OnCancelled returned string : " + s);
        }

        @Override
        protected String doInBackground(Void... strings) {
            String registrationToken = FirebaseInstanceId.getInstance().getToken();
            Log.d(TAG, "FCM Registration Token: " + registrationToken);

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

                String sendMessage2 = "<iq type='set' to='auth' id='auth2'>\n" +
                        "<query xmlns='quadrant:iq:auth'>\n" +
                        "<jabber-username>" + login.getXmppUserDetails().getJid().split("@")[0] + "</jabber-username>\n" +
                        "<jabber-password>" + login.getXmppUserDetails().getPassword() + "</jabber-password>\n" +
                        "<device-token>" + registrationToken + "</device-token>\n" +
                        "<user-id>" + login.getId() + "</user-id>\n" +
                        "<device-type>Android</device-type>\n" +
                        "</query>\n" +
                        "</iq>\n";
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
            } catch (Exception e) {
                Log.d(TAG, "Some error occurred: " + e.getMessage());
            }/* finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }*/

            System.out.println("hiiiiiiiiiiiiiiiiiiiii-------------3" + response);

            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            System.out.println("post exeeeeeeeeeeeeeeee");
            System.out.println("The returned string: " + s);
            String digest;

            if (!s.equals("")) {
                Document doc = convertStringToXML(s);
                NodeList nodes = doc.getElementsByTagName("query");
                if (nodes.getLength() > 0) {
                    Element err = (Element) nodes.item(0);
                    System.out.println(err.getElementsByTagName("digest")
                            .item(0)
                            .getTextContent());
                    SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putString("digest", err.getElementsByTagName("digest").item(0).getTextContent());
                    editor.apply();
                }
            }
        }
    }

    private static Document convertStringToXML(String xmlString) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();

            return builder.parse(new InputSource(new StringReader(xmlString)));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private void fetchGroupMessages(final List<GroupMessageRealm> messageList, final String jid) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                final RealmResults<GroupMessageRealm> realmResults = realm
                        .where(GroupMessageRealm.class)
                        .equalTo("groupJid", jid)
                        .findAllSorted("time", Sort.DESCENDING);

                Log.d(TAG, "GroupJid :" + jid);
                if (realmResults.size() > 0) {
                    messageList.clear();
                    messageList.addAll(realmResults);
                    messagesAdapter.notifyDataSetChanged();
                    //Log.d(TAG, "msgRealm size: " + msgRealm.size()+"\ncurrent position: "+(((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition() + 1));
                    //recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                    mLayoutManager.scrollToPositionWithOffset(0, 0);
                    //recyclerView.smoothScrollToPosition(0);
                } else if (realmResults.size() == 0) {
                    realm.delete(GroupMessageRealm.class);
                    msgRealm.clear();
                    //getMessagesLocally();
                }
            }
        });
    }

    private void loadMessages(String access, Boolean joined, int usersCount) {
        switch (access) {
            case "PUBLIC":
                if (joined) {
                    Log.d(TAG, "onCreate: -> public and joined");
                    membersOfGrp.setVisibility(View.VISIBLE);
                    NoOfMembers.setVisibility(View.VISIBLE);
                    NoOfMembers.setText(String.valueOf(usersCount));
                    send.setBackgroundColor(Color.GREEN);
                    send.setClickable(true);
                } else {
                    Log.d(TAG, "onCreate: -> public");
                    membersOfGrp.setVisibility(View.GONE);
                    NoOfMembers.setVisibility(View.GONE);
                    adminOfGroup.setText("Join");
                    adminOfGroup.setGravity(Gravity.END);
                    send.setClickable(false);
                }
                /*Toast.makeText(this, "send clickable: "+
                        send.isClickable(), Toast.LENGTH_SHORT).show();*/
                //getMessagesLocally();
                break;
            case "PRIVATE":
                Log.d(TAG, "onCreate: -> private");
                membersOfGrp.setVisibility(View.VISIBLE);
                NoOfMembers.setVisibility(View.VISIBLE);
                NoOfMembers.setText(String.valueOf(usersCount));
                adminOfGroup.setText("admin");

                //getMessagesLocally();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!isNetworkAvailable(this)) {
            finishAndRemoveTask();
        }
        checkReadWritePermissions();
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
                        loadMessages(access, joined, usersCount);
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
        }
    }

    private void getJabberRoomStatus(Context context, final String status, String id) {
        APIInterface apiInterface = APIClient.getClientWithAuth(context)
                .create(APIInterface.class);
        Call<ResponseBody> call = apiInterface.getJabberRoomStatus(status, id);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (status.equals("join")) {
                    Log.d(TAG, "onResponse: -> Room joined successfully");
                } else {
                    Log.d(TAG, "onResponse: -> Room left successfully");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "onResponse: -> Room status not updated");
            }
        });
    }

    private void getMessagesLocally() {
        final MessageParams params = new MessageParams(groupJid, 20);
        
        APIInterface apiInterface = APIClient.getClientWithAuth(this)
                .create(APIInterface.class);
        Call<Messages> call = apiInterface.getAllMessages(params);
        call.enqueue(new Callback<Messages>() {
            @Override
            public void onResponse(Call<Messages> call, final Response<Messages> response) {
                for (int i=0; i<response.body().getSuccess().size(); i++) {
                    try {
                        DocumentBuilder documentBuilder = DocumentBuilderFactory
                                .newInstance().newDocumentBuilder();
                        Document parse = documentBuilder.parse(new ByteArrayInputStream(response.body()
                                .getSuccess().get(i).getXml().getBytes()));

                        Log.d(TAG, "onResponse: -> response: " + response.body()
                                .getSuccess().get(i).getXml());
                        Log.d(TAG, "onResponse: -> message: " + parse
                                .getFirstChild().getTextContent() + " sender's name: "+response.body()
                                .getSuccess().get(i).getNick() + " time: " +
                                response.body().getSuccess().get(i).getTime());

                        Log.d(TAG, "msgRealm size: " + msgRealm.size());
                        stanzaId.add(response.body().getSuccess().get(i).getStanzId());
                        addMessages(response.body().getSuccess().get(i).getNick(),
                                response.body().getSuccess().get(i).getBody(),
                                response.body().getSuccess().get(i).getStanzId(),
                                response.body().getSuccess().get(i).getTime(),
                                response.body().getSuccess().get(i).getPeer(),
                                response.body().getSuccess().get(i).getXml(),
                                response.body().getSuccess().get(i).getText(),
                                response.body().getSuccess().get(i).getThumbnailUrl(),
                                response.body().getSuccess().get(i).getImageUrl(),
                                response.body().getSuccess().get(i).getImageName(), groupJid);
                        int size = getRealmSize();
                        Log.d(TAG, "size of the realm: " + size + "\nsize of the response: " +
                                response.body().getSuccess().size());
                        recyclerView.scrollToPosition(0);
                        //messageList.clear();
                        /*if (loadAdminMessages) {
                            Log.d("::::GroupMessages", "admin name: " + admin);
                            Log.d("::::GroupMessages", "admin name from list: " + response.body().getSuccess().get(i).getNick());
                            if (response.body().getSuccess().get(i).getNick().equalsIgnoreCase(admin)) {
                                messageList.addAll(response.body().getSuccess());
                                Log.d("::::GroupMessages", "name: ");
                            }
                        } else {

                        }*/
                        //messageList.addAll(response.body().getSuccess());
                        /*messagesAdapter = new ChatsAdapter(GroupMessageActivity.this, messageList, loadAdminMessages, admin);
                        recyclerView.setAdapter(messagesAdapter);*/
                        //presenter.updateAdapter(messageList, GroupMessageActivity.this, recyclerView);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Messages> call, Throwable t) {
                t.printStackTrace();
                GenExceptions.fireException(t);
            }
        });
    }

    private int getRealmSize() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<GroupMessageRealm> realmResults = realm.where(GroupMessageRealm.class)
                        .findAll();

                size = realmResults.size();
            }
        });

        return size;
    }

    private void getNewMessages(final String stanzId) {
        loading = true;
        final MessageParams params = new MessageParams(groupJid, 20, stanzId);

        APIInterface apiInterface = APIClient.getClientWithAuth(this)
                .create(APIInterface.class);
        Call<Messages> call = apiInterface.getAllMessages(params);
        call.enqueue(new Callback<Messages>() {
            @Override
            public void onResponse(Call<Messages> call, final Response<Messages> response) {
                progressLayout.setVisibility(View.GONE);
                if (response.body().getSuccess() != null) {
                    if (response.body().getSuccess().size() > 0) {
                        loadMoreMsg = false;
                        stanzaId.clear();
                        for (int i = 0; i < response.body().getSuccess().size(); i++) {
                            try {
                                DocumentBuilder documentBuilder = DocumentBuilderFactory
                                        .newInstance().newDocumentBuilder();
                                Document parse = documentBuilder.parse(new ByteArrayInputStream(response.body()
                                        .getSuccess().get(i).getXml().getBytes()));

                                Log.d(TAG, "After Timestamp -> New Messages :" + response.body()
                                        .getSuccess().get(i).getXml());
                                Log.d(TAG, "After Timestamp -> New Messages :" + parse
                                        .getFirstChild().getTextContent() + " sender's name: " + response.body()
                                        .getSuccess().get(i).getNick() + " time: " +
                                        response.body().getSuccess().get(i).getTime());
                                stanzaId.add(response.body().getSuccess().get(i).getStanzId());
                                addMessages(response.body().getSuccess().get(i).getNick(),
                                        response.body().getSuccess().get(i).getBody(),
                                        response.body().getSuccess().get(i).getStanzId(),
                                        response.body().getSuccess().get(i).getTime(),
                                        response.body().getSuccess().get(i).getPeer(),
                                        response.body().getSuccess().get(i).getXml(),
                                        response.body().getSuccess().get(i).getText(),
                                        response.body().getSuccess().get(i).getThumbnailUrl(),
                                        response.body().getSuccess().get(i).getImageUrl(),
                                        response.body().getSuccess().get(i).getImageName(), groupJid);

                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    } else {
                        loadMoreMsg = true;
                    }
                    loading = false;
                }
            }

            @Override
            public void onFailure(Call<Messages> call, Throwable t) {
                progressLayout.setVisibility(View.GONE);
                t.printStackTrace();
                GenExceptions.fireException(t);
                loading = false;
            }
        });
    }

    public void addMessages(String nick, String body, String stanzId, String time, String peer, String xml, String text, String thumbnailUrl, String imageUrl, String imageName, final String groupJid) {
        realm.beginTransaction();
        //realm.deleteAll();
        GroupMessageRealm groupMessageRealm = realm.createObject(GroupMessageRealm.class, UUID.randomUUID().toString());
        groupMessageRealm.setNick(nick);
        groupMessageRealm.setBody(body);
        groupMessageRealm.setStanzId(stanzId);
        groupMessageRealm.setTime(time);
        groupMessageRealm.setPeer(peer);
        groupMessageRealm.setXml(xml);
        groupMessageRealm.setText(text);
        groupMessageRealm.setThumbnailUrl(thumbnailUrl);
        groupMessageRealm.setImageUrl(imageUrl);
        groupMessageRealm.setImageName(imageName);
        groupMessageRealm.setGroupJid(groupJid);
        realm.commitTransaction();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<GroupMessageRealm> realmResults = realm.where(GroupMessageRealm.class)
                        .equalTo("groupJid", groupJid)
                        .findAllSorted("time", Sort.DESCENDING);

                msgRealm.clear();
                //msgRealm.add(0, realmResults.last());
                msgRealm.addAll(realmResults);
                for (int i=0; i<msgRealm.size(); i++) {
                    Log.d(TAG, "elements: " + msgRealm.get(i).getBody());
                }

                //mLayoutManager.setStackFromEnd(true);
                messagesAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void successfulMessage(String msg) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (joinedGroups != null) {
            getJabberRoomStatus(GroupMessageActivity.this, "left", String.valueOf(joinedGroups.getId()));
        }

        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //muc.removeMessageListener(messageListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                int currItem = 0;
                while (currItem < count){
                    Uri uri = data.getClipData().getItemAt(currItem).getUri();
                    File file = new File(getPath(GroupMessageActivity.this, uri));
                    currItem = currItem + 1;
                    String date = new SimpleDateFormat("yyyy-M-dd HH:MM:SS",
                            Locale.getDefault()).format(new Date());
                    if (groups != null) {
                        presenter.uploadImage(GroupMessageActivity.this, file, String.valueOf(groups.getId()), date, recyclerView, conn1, groups.getRoomJid(), login, msgRealm);
                    } else if (joinedGroups != null) {
                        presenter.uploadImage(GroupMessageActivity.this, file, String.valueOf(joinedGroups.getId()), date, recyclerView, conn1, joinedGroups.getRoomJid(), login, msgRealm);
                    } else if (createdGroups != null) {
                        presenter.uploadImage(GroupMessageActivity.this, file, String.valueOf(createdGroups.getId()), date, recyclerView, conn1, createdGroups.getRoomJid(), login, msgRealm);
                    }
                }
            } else if (data.getData() != null) {
                Uri uri = data.getData();

                File file = new File(getPath(GroupMessageActivity.this, uri));

                String date = new SimpleDateFormat("yyyy-M-dd HH:MM:SS",
                        Locale.getDefault()).format(new Date());
                if (groups != null) {
                    presenter.uploadImage(GroupMessageActivity.this, file, String.valueOf(groups.getId()), date, recyclerView, conn1, groups.getRoomJid(), login, msgRealm);
                } else if (joinedGroups != null) {
                    presenter.uploadImage(GroupMessageActivity.this, file, String.valueOf(joinedGroups.getId()), date, recyclerView, conn1, joinedGroups.getRoomJid(), login, msgRealm);
                } else if (createdGroups != null) {
                    presenter.uploadImage(GroupMessageActivity.this, file, String.valueOf(createdGroups.getId()), date, recyclerView, conn1, createdGroups.getRoomJid(), login, msgRealm);
                }
            }
            /*new Thread(new Runnable() {
                @Override
                public void run() {
                    *//*Uri uri = data.getData();


                    File file = new File(getPath(GroupMessageActivity.this, uri));

                    String date = new SimpleDateFormat("yyyy-M-dd HH:MM:SS",
                            Locale.getDefault()).format(new Date());
                    if (groups != null) {
                        presenter.uploadImage(GroupMessageActivity.this, file, String.valueOf(groups.getId()), date, recyclerView, conn1, groups.getRoomJid(), login, msgRealm);
                    } else if (joinedGroups != null) {
                        presenter.uploadImage(GroupMessageActivity.this, file, String.valueOf(joinedGroups.getId()), date, recyclerView, conn1, joinedGroups.getRoomJid(), login, msgRealm);
                    } else if (createdGroups != null) {
                        presenter.uploadImage(GroupMessageActivity.this, file, String.valueOf(createdGroups.getId()), date, recyclerView, conn1, createdGroups.getRoomJid(), login, msgRealm);
                    }*//*
                }
            }).start();*/
            Log.d(TAG, "in onActivityResult....");
            /*Uri uri = data.getData();

            File file = new File(getPath(GroupMessageActivity.this, uri));

            String date = new SimpleDateFormat("yyyy-M-dd HH:MM:SS",
                    Locale.getDefault()).format(new Date());
            Boolean uploaded = false;
            if (groups != null) {
                uploaded = presenter.uploadImage(GroupMessageActivity.this, file, String.valueOf(groups.getId()), date, recyclerView, conn1, groups.getRoomJid(), login, msgRealm);
            } else if (joinedGroups != null) {
                uploaded = presenter.uploadImage(GroupMessageActivity.this, file, String.valueOf(joinedGroups.getId()), date, recyclerView, conn1, joinedGroups.getRoomJid(), login, msgRealm);
            } else if (createdGroups != null) {
                uploaded = presenter.uploadImage(GroupMessageActivity.this, file, String.valueOf(createdGroups.getId()), date, recyclerView, conn1, createdGroups.getRoomJid(), login, msgRealm);
            }

            if (uploaded) {
                Log.d(TAG, "upload was successful");
            } else {
                Log.d(TAG, "upload was not successful");
            }*/
            /*Uri uri = data.getData();

            File file = new File(getPath(GroupMessageActivity.this, uri));

            String date = new SimpleDateFormat("yyyy-M-dd HH:MM:SS",
                    Locale.getDefault()).format(new Date());
            *//*addMessages(login.getXmppUserDetails().getNick(), "imageUpload", );
            MessageSuccess messageSuccess = new MessageSuccess("dfsfsdf", login.getXmppUserDetails().getNick(), "imageUpload", "sfsdfsdsv", date, id, "<xml></xml>", file.getPath(), file.getPath(), file.getName());
            messageList.add(0, messageSuccess);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    NotifyAdapter(messagesAdapter, messageList);
                }
            });*//*
            if (groups != null) {
                presenter.uploadImage(GroupMessageActivity.this, file, String.valueOf(groups.getId()), date, recyclerView, conn1, groups.getRoomJid(), login, msgRealm);
            } else if (joinedGroups != null) {
                presenter.uploadImage(GroupMessageActivity.this, file, String.valueOf(joinedGroups.getId()), date, recyclerView, conn1, joinedGroups.getRoomJid(), login, msgRealm);
            } else if (createdGroups != null) {
                presenter.uploadImage(GroupMessageActivity.this, file, String.valueOf(createdGroups.getId()), date, recyclerView, conn1, createdGroups.getRoomJid(), login, msgRealm);
            }*/
        }
    }

    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    private void NotifyAdapter(/*ChatsAdapter messagesAdapter, List<MessageSuccess> messageList*/) {
        recyclerView.scrollToPosition(0);
        //messagesAdapter.notifyItemInserted(messageList.size() - 1);
        //recyclerView.smoothScrollToPosition(0);
    }

    MessageListener messageListener = new MessageListener() {
        @Override
        public void processMessage(final Message message) {
            /*Log.d(TAG, "processMessage: ------------------Receivied-----------------");
            final String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault()).format(new Date());
            Log.d(TAG, "msgRealm size: " + msgRealm.size());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    *//*String xmlStanza = message.getExtension("stanza-id", "urn:xmpp:sid:0").toXML().toString();
                    String xmlImage = message.getExtension("image", "urn:xmpp:image").toXML().toString();
                    String xmlThumb = message.getExtension("thumbnail", "urn:xmpp:thumbnail").toXML().toString();
                    try {
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder docBuilder = factory.newDocumentBuilder();
                        StringReader stringReaderStanza = new StringReader(xmlStanza);
                        InputSource inputSourceStanza = new InputSource(stringReaderStanza);

                        Document docStanzaId = docBuilder.parse(inputSourceStanza);

                        int stanzaIdItem = docStanzaId.getChildNodes().item(0).getAttributes().getLength();
                        stanzaId.add(docStanzaId.getChildNodes().item(0).getAttributes().item(stanzaIdItem - 1).getNodeValue());

                        final String thumbUrl, imageUrl;
                        StringReader stringReaderImage = new StringReader(xmlImage);
                        StringReader stringReaderThumb = new StringReader(xmlThumb);
                        InputSource inputSourceImage = new InputSource(stringReaderImage);
                        InputSource inputSourceThumb = new InputSource(stringReaderThumb);

                        Document docImage = docBuilder.parse(inputSourceImage);
                        Document docThumb = docBuilder.parse(inputSourceThumb);

                        int imageItem = docImage.getChildNodes().item(0).getAttributes().getLength();
                        imageUrl = docImage.getChildNodes().item(0).getAttributes().item(imageItem-1).getNodeValue();
                        int thumbItem = docThumb.getChildNodes().item(0).getAttributes().getLength();
                        thumbUrl = docThumb.getChildNodes().item(0).getAttributes().item(thumbItem-1).getNodeValue();

                        if (imageUrl.equalsIgnoreCase("") && thumbUrl.equalsIgnoreCase("")) {
                            Log.d(TAG, "normal text message");
                            addMessages(login.getXmppUserDetails().getNick(), message.getBody(), stanzaId.get(0), date,
                                    message.getFrom().toString(), message.toXML().toString(), message.getBody(),
                                    "", "", "", groupJid);
                        } else {
                            Log.d(TAG, "Image message");
                            addMessages(login.getXmppUserDetails().getNick(), message.getBody(), stanzaId.get(0), date,
                                    message.getFrom().toString(), message.toXML().toString(), message.getBody(),
                                    thumbUrl, imageUrl, "", groupJid);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }*//*
                    Log.d(TAG, "normal text message");
                    addMessages(login.getXmppUserDetails().getNick(), message.getBody(), stanzaId.get(0), date,
                            message.getFrom().toString(), message.toXML().toString(), message.getBody(),
                            "", "", "", groupJid);
                    *//*realm.beginTransaction();
                    if (message.getBody().equalsIgnoreCase("imageUpload")) {
                        String xmlImage = message.getExtension("image", "urn:xmpp:image").toXML().toString();
                        String xmlThumb = message.getExtension("thumbnail", "urn:xmpp:thumbnail").toXML().toString();

                        try {
                            final String thumbUrl, imageUrl;
                            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                            DocumentBuilder docBuilder = factory.newDocumentBuilder();
                            StringReader stringReaderImage = new StringReader(xmlImage);
                            StringReader stringReaderThumb = new StringReader(xmlThumb);
                            InputSource inputSourceImage = new InputSource(stringReaderImage);
                            InputSource inputSourceThumb = new InputSource(stringReaderThumb);

                            Document docImage = docBuilder.parse(inputSourceImage);
                            Document docThumb = docBuilder.parse(inputSourceThumb);

                            int imageItem = docImage.getChildNodes().item(0).getAttributes().getLength();
                            imageUrl = docImage.getChildNodes().item(0).getAttributes().item(imageItem-1).getNodeValue();
                            int thumbItem = docThumb.getChildNodes().item(0).getAttributes().getLength();
                            thumbUrl = docThumb.getChildNodes().item(0).getAttributes().item(thumbItem-1).getNodeValue();

                            GroupMessageRealm groupMessageRealm = realm.createObject(GroupMessageRealm.class, UUID.randomUUID().toString());
                            groupMessageRealm.setNick(login.getXmppUserDetails().getNick());
                            groupMessageRealm.setBody(message.getBody());
                            groupMessageRealm.setStanzId(message.setStanzaId());
                            groupMessageRealm.setTime(date);
                            groupMessageRealm.setPeer(message.getFrom().toString());
                            groupMessageRealm.setXml(message.toXML().toString());
                            groupMessageRealm.setText("");
                            groupMessageRealm.setThumbnailUrl(thumbUrl);
                            groupMessageRealm.setImageUrl(imageUrl);
                            groupMessageRealm.setImageName("image");
                            groupMessageRealm.setGroupJid(groupJid);
                        } catch (Exception ex) {
                            GenExceptions.logException(ex);
                        }
                    } else {
                        GroupMessageRealm groupMessageRealm = realm.createObject(GroupMessageRealm.class, UUID.randomUUID().toString());
                        groupMessageRealm.setNick(login.getXmppUserDetails().getNick());
                        groupMessageRealm.setBody(message.getBody());
                        groupMessageRealm.setStanzId(message.setStanzaId());
                        groupMessageRealm.setTime(date);
                        groupMessageRealm.setPeer(message.getFrom().toString());
                        groupMessageRealm.setXml(message.toXML().toString());
                        groupMessageRealm.setText(message.getBody());
                        groupMessageRealm.setThumbnailUrl("");
                        groupMessageRealm.setImageUrl("");
                        groupMessageRealm.setImageName("");
                        groupMessageRealm.setGroupJid(groupJid);
                    }
                    realm.commitTransaction();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            RealmResults<GroupMessageRealm> realmResults = realm.where(GroupMessageRealm.class)
                                    .equalTo("groupJid", groupJid)
                                    .findAll();

                            msgRealm.addAll(realmResults);
                            //msgRealm.add(0, realmResults.last());
                            messagesAdapter.notifyDataSetChanged();
                        }
                    });*//*
                }
            });
            *//*MessageSuccess messageSuccess = new MessageSuccess(message.getPacketID(), login.getXmppUserDetails().getNick(), message.getBody(), message.getStanzaId(), date, message.getFrom().toString(), message.toXML().toString(), textToSend.getText().toString());
            messageList.add(0, messageSuccess);*//*
            *//*runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addMessages(login.getXmppUserDetails().getNick(), message.getBody(), message.getStanzaId(), date,
                            message.getFrom().toString(), message.toXML().toString(), textToSend.getText().toString(),
                            "", "", "", groupJid);
                    NotifyAdapter();
                }
            });*/
            Log.d(TAG, "processMessage: ------------------Receivied-----------------");
            Log.d(TAG, "Message: " + message.toXML());
            final String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault()).format(new Date());
            /*if (message.getBody()
                    .equalsIgnoreCase("imageUpload")) {
                String xmlImage = message.getExtension("image", "urn:xmpp:image").toXML().toString();
                String xmlThumb = message.getExtension("thumbnail", "urn:xmpp:thumbnail").toXML().toString();

                try {
                    final String thumbUrl, imageUrl;
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder docBuilder = factory.newDocumentBuilder();
                    StringReader stringReaderImage = new StringReader(xmlImage);
                    StringReader stringReaderThumb = new StringReader(xmlThumb);
                    InputSource inputSourceImage = new InputSource(stringReaderImage);
                    InputSource inputSourceThumb = new InputSource(stringReaderThumb);

                    Document docImage = docBuilder.parse(inputSourceImage);
                    Document docThumb = docBuilder.parse(inputSourceThumb);

                    int imageItem = docImage.getChildNodes().item(0).getAttributes().getLength();
                    imageUrl = docImage.getChildNodes().item(0).getAttributes().item(imageItem-1).getNodeValue();
                    int thumbItem = docThumb.getChildNodes().item(0).getAttributes().getLength();
                    thumbUrl = docThumb.getChildNodes().item(0).getAttributes().item(thumbItem-1).getNodeValue();

                    Log.d(TAG, "newIncomingMessage: thumb image");
                    //imageUrl = doc.getChildNodes().item(1).getAttributes().item(doc.getChildNodes().item(1).getAttributes().getLength()-1).getNodeName();
                    //thumbUrl = doc.getChildNodes().item(2).getAttributes().item(doc.getChildNodes().item(1).getAttributes().getLength()-1).getNodeName();
                    //int item = doc.getChildNodes().item(0).getAttributes().getLength();
                    //Log.d(TAG, "newIncomingMessage: -> item length: " + item);
                    //int thumbItem = doc.getChildNodes().item(1).getAttributes().getLength();
                    //thumbUrl = doc.getChildNodes().item(1).getAttributes().item(thumbItem-1).getNodeValue();
                    //imageUrl = doc.getChildNodes().item(0).getAttributes().item(item-1).getNodeValue();
                    //doc.getElementsByTagName("name").item();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addMessages(login.getXmppUserDetails().getNick(), message.getBody(), message.getStanzaId(), date,
                                    message.getFrom().toString(), message.toXML().toString(), message.getBody(),
                                    imageUrl, thumbUrl, "", groupJid);
                        }
                    });
                } catch (Exception ex) {
                    GenExceptions.logException(ex);
                }
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addMessages(login.getXmppUserDetails().getNick(), message.getBody(), message.getStanzaId(), date,
                                message.getFrom().toString(), message.toXML().toString(), message.getBody(),
                                "", "", "", groupJid);
                    }
                });
            }*/
            /*runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addMessages(login.getXmppUserDetails().getNick(), message.getBody(), message.getStanzaId(), date,
                            message.getFrom().toString(), message.toXML().toString(),  message.getBody(),
                            "", "", "", groupJid);
                    NotifyAdapter();
                }
            });*/
        }
    };
}