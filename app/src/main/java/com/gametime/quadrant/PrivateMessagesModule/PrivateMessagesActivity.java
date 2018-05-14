package com.gametime.quadrant.PrivateMessagesModule;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.gametime.quadrant.Adapters.PrivateMessagesAdapter;
import com.gametime.quadrant.Application.MyApp;
import com.gametime.quadrant.Exceptions.GenExceptions;
import com.gametime.quadrant.GroupMessagesModule.GroupMessageActivity;
import com.gametime.quadrant.ImageUploadStatus;
import com.gametime.quadrant.MemberProfileModule.MemberProfileActivity;
import com.gametime.quadrant.Models.GroupImageUpload;
import com.gametime.quadrant.Models.PrivateMessageRealm;
import com.gametime.quadrant.Models.PrivateMessagesList;
import com.gametime.quadrant.Models.PrivateMessagesObj;
import com.gametime.quadrant.Models.QuadrantLoginDetails;
import com.gametime.quadrant.Network.APIClient;
import com.gametime.quadrant.Network.APIInterface;
import com.gametime.quadrant.PermissionsBasePackage.QuadrantPermissionsBaseActivity;
import com.gametime.quadrant.R;
import com.gametime.quadrant.Utils.Constants;
import com.gametime.quadrant.Utils.Network;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.StandardExtensionElement;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.offline.OfflineMessageManager;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.gametime.quadrant.Utils.Constants.EXTRA_GROUP_ID_KEY;
import static com.gametime.quadrant.Utils.Constants.PREF_KEY_JOINED_GROUPS_INFO;
import static com.gametime.quadrant.Utils.Constants.PREF_KEY_LOGGED_IN_USER;
import static com.gametime.quadrant.Utils.Constants.PREF_KEY_PRIVATE_MSG;

public class PrivateMessagesActivity extends QuadrantPermissionsBaseActivity implements PrivateMessagesContract.PrivateMessagesView {
    RecyclerView recyclerView;
    EditText textToSend;
    FloatingActionButton send;
    ImageView chooseImage;
    ArrayList<String> messages, names, times;
    private PrivateMessagesList.Success privateMessages;
    private String APICreds;
    private QuadrantLoginDetails login;
    private static final String TAG = "PrivateMessagesActivity";
    Toolbar toolbar;
    private ChatManager chatManager;
    private EntityBareJid bareJid;
    private PrivateMessagesPresenter presenter;
    private Chat chat;
    ProgressBar progressBar;
    private int PICK_IMAGE_REQUEST = 1;
    //private EntityBareJid ownJid;
    private OfflineMessageManager mOfflineMessageManager;
    private String username;
    private Realm realm;
    private int lastMsg;
    private PrivateMessagesAdapter adapter;
    private ArrayList<PrivateMessageRealm> messageList;
    private PrivateMessageRealm privateMessageRealm;
    private boolean mediaId;
    private XMPPTCPConnection conn1;
    private boolean permissionGrant = false;
    private int status = ImageUploadStatus.NONE.getValue();
    private LinearLayoutManager mLayoutManager;
    private final static String MY_PREFS_NAME = "digestPM";
    Socket socket = null;
    private boolean isFromNotifications = false;
    private PrivateMessagesObj pm;
    private String pmId;
    private String message, title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_messages);

        isFromNotifications = getIntent().getBooleanExtra(getString(R.string.from_notifications), false);
        message = getIntent().getStringExtra(getString(R.string.message));
        title = getIntent().getStringExtra(getString(R.string.title));
        Gson gson = new Gson();
        final String privateMsgs = getSharedPreferences(Constants.PREF_FILE_NAME, MODE_PRIVATE)
                .getString(PREF_KEY_PRIVATE_MSG, "");
        pm = gson.fromJson(privateMsgs, PrivateMessagesObj.class);
        if (!Network.isNetworkAvailable(this)) {
            finishAndRemoveTask();
        }

        //startMyTask(new Authentication());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionGrant = checkReadWritePermissions();

            if (permissionGrant) {
                realm = Realm.getDefaultInstance();
                recyclerView = (RecyclerView) findViewById(R.id.private_msg_recycler_view);
                textToSend = (EditText) findViewById(R.id.privateTextToSend);
                chooseImage = (ImageView) findViewById(R.id.chooseImagePM);
                send = (FloatingActionButton) findViewById(R.id.privateSendButton);
                privateMessages = (PrivateMessagesList.Success) getIntent().getSerializableExtra("privateMessages");
                username = getIntent().getStringExtra("username");
                pmId = getIntent().getStringExtra(EXTRA_GROUP_ID_KEY);
                toolbar = (Toolbar) findViewById(R.id.toolbarPM);
                messageList = new ArrayList<>();

                if (username != null) {
                    toolbar.setTitle(username);
                } else {
                    if (!isFromNotifications) {
                        toolbar.setTitle(privateMessages.getNick());
                    } else {
                        toolbar.setTitle(pm.getNick());
                    }
                }
                toolbar.setTitleMarginStart(150);
                toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
                //toolbar.showOverflowMenu();
                setSupportActionBar(toolbar);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });

                messages = new ArrayList<>();
                names = new ArrayList<>();
                times = new ArrayList<>();
                progressBar = (ProgressBar) findViewById(R.id.progressBar);
                progressBar.setIndeterminate(true);

                APICreds = getSharedPreferences(Constants.PREF_FILE_NAME,
                        Context.MODE_PRIVATE).getString(PREF_KEY_LOGGED_IN_USER, "");
                login = new Gson().fromJson(APICreds, QuadrantLoginDetails.class);

                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
                presenter = new PrivateMessagesPresenter(this);

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

                recyclerView.setLayoutManager(mLayoutManager);
                adapter = new PrivateMessagesAdapter(this, messageList, privateMessages);
                recyclerView.setAdapter(adapter);
                if (pmId.equals(pm.getId().toString())) {
                    final String date = new
                            SimpleDateFormat("MMM dd, hh:mm a",
                            Locale.getDefault()).format(new Date());
                    addChats(message, title, date, false, "", "", messageList, 0);
                }

                chooseImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        startActivityForResult(Intent.createChooser(intent,
                                "Select Picture"), PICK_IMAGE_REQUEST);
                    }
                });

                fetchPrivateMessages(messageList, progressBar);

                try {
                    chatManager = ChatManager.getInstanceFor(conn1);

                    chatManager.addIncomingListener(new IncomingChatMessageListener() {

                        @Override
                        public void newIncomingMessage(final EntityBareJid from, final Message message, final Chat chat) {
                            Log.d(TAG,"------------------Recieved-------------------");

                            final String date = new
                                    SimpleDateFormat("MMM dd, hh:mm a",
                                    Locale.getDefault()).format(new Date());

                            if (message.getBody()
                                    .equalsIgnoreCase("imageUpload")) {
                                mediaId = true;
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
                                            if (!isFromNotifications) {
                                                addChats(message.getBody(),
                                                        privateMessages.getNick(),
                                                        date, mediaId, imageUrl, thumbUrl,
                                                        messageList, -1);
                                            } else {
                                                addChats(message.getBody(),
                                                        pm.getNick(),
                                                        date, mediaId, imageUrl, thumbUrl,
                                                        messageList, -1);
                                            }
                                        }
                                    });
                                } catch (Exception ex) {
                                    GenExceptions.logException(ex);
                                }
                            } else {
                                mediaId = false;

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!isFromNotifications) {
                                            addChats(message.getBody(),
                                                    privateMessages.getNick(),
                                                    date, mediaId, "", "",
                                                    messageList, -1);
                                        } else {
                                            addChats(message.getBody(),
                                                    pm.getNick(),
                                                    date, mediaId, "", "",
                                                    messageList, -1);
                                        }
                                    }
                                });
                            }

                    /*presenter.sendMessage(messages, message.getBody(), names, from.asEntityBareJidString().split("@")[0],
                        times, "2017-11-05 04:21:25", PrivateMessagesActivity.this, recyclerView);
                            Log.d(TAG, "newIncomingMessage: -> message: " + message.getBody()+"\nto: " + message.getTo()+"\nfrom: "+message.getFrom());*/
                        }
                    });

                    if (!isFromNotifications) {
                        Log.d(TAG, "JID-------------------" + privateMessages.getJid());
                        bareJid = JidCreate.entityBareFrom(privateMessages.getJid());
                    }
                    else {
                        Log.d(TAG,"JID-------------------"+pm.getJid());
                        bareJid = JidCreate.entityBareFrom(pm.getJid());
                    }
                    Log.d(TAG,"Own JID-------------------"+login.getXmppUserDetails().getJid());


                    chat = chatManager.chatWith(bareJid);


                } catch (Exception ex) {
                    GenExceptions.fireException(ex);

                }

                //new PrivateMessagesActivity.XmppConnectionTask().execute();

                send.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!TextUtils.isEmpty(textToSend.getText().toString())) {
                            startStepFour(new StepFour());
                            boolean blocked;
                            if (!isFromNotifications && privateMessages.getblock() != null)
                                blocked = pm.getBlock();
                            else
                                blocked = privateMessages.getblock();
                            if (blocked) {
                                Snackbar.make(view, "Please unblock the respective user", Snackbar.LENGTH_LONG).show();

                                textToSend.setEnabled(false);
                            } else {
                                textToSend.setEnabled(true);
                                try {
                                    Message message = new Message();
                                    message.setStanzaId(UUID.randomUUID().toString());
                                    message.setType(Message.Type.chat);
                                    message.setBody(textToSend.getText().toString());

                                    String date = new
                                            SimpleDateFormat("MMM dd, hh:mm a",
                                            Locale.getDefault()).format(new Date());

                                    chat.send(message);

                                    String from = login.getXmppUserDetails().getNick();
                                    addChats(textToSend.getText().toString(),
                                            from, date, false, "",
                                            "", messageList, -1);

                                    //textToSend.setText("");
                                } catch (Exception ex) {
                                    GenExceptions.fireException(ex);
                                }
                            }
                        }

               /* String date = new SimpleDateFormat("yyyy-M-dd HH:MM:SS",
                        Locale.getDefault()).format(new Date());

                presenter.sendMessage(messages, textToSend.getText().toString(),
                        names, login.getXmppUserDetails().getNick(), times, date,
                        PrivateMessagesActivity.this, recyclerView);*/
                    }
                });
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionGrant = checkReadWritePermissions();

            if (permissionGrant) {
                realm = Realm.getDefaultInstance();
                recyclerView = (RecyclerView) findViewById(R.id.private_msg_recycler_view);
                textToSend = (EditText) findViewById(R.id.privateTextToSend);
                chooseImage = (ImageView) findViewById(R.id.chooseImagePM);
                send = (FloatingActionButton) findViewById(R.id.privateSendButton);
                privateMessages = (PrivateMessagesList.Success) getIntent().getSerializableExtra("privateMessages");
                username = getIntent().getStringExtra("username");
                toolbar = (Toolbar) findViewById(R.id.toolbarPM);
                messageList = new ArrayList<>();

                if (username != null) {
                    toolbar.setTitle(username);
                } else {
                    if (!isFromNotifications)
                        toolbar.setTitle(privateMessages.getNick());
                    else
                        toolbar.setTitle(pm.getNick());
                }
                toolbar.setTitleMarginStart(150);
                toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
                toolbar.showOverflowMenu();
                setSupportActionBar(toolbar);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });

                messages = new ArrayList<>();
                names = new ArrayList<>();
                times = new ArrayList<>();
                progressBar = (ProgressBar) findViewById(R.id.progressBar);
                progressBar.setIndeterminate(true);

                APICreds = getSharedPreferences(Constants.PREF_FILE_NAME,
                        Context.MODE_PRIVATE).getString(PREF_KEY_LOGGED_IN_USER, "");
                login = new Gson().fromJson(APICreds, QuadrantLoginDetails.class);

                mLayoutManager = new LinearLayoutManager(this);
                presenter = new PrivateMessagesPresenter(this);

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

                recyclerView.setLayoutManager(mLayoutManager);
                adapter = new PrivateMessagesAdapter(this, messageList, privateMessages);
                recyclerView.setAdapter(adapter);

                chooseImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        startActivityForResult(Intent.createChooser(intent,
                                "Select Picture"), PICK_IMAGE_REQUEST);
                    }
                });

                fetchPrivateMessages(messageList, progressBar);

                /*try {
                    chatManager = ChatManager.getInstanceFor(conn1);
                    chatManager.addIncomingListener(new IncomingChatMessageListener() {
                        @Override
                        public void newIncomingMessage(final EntityBareJid from, final Message message, final Chat chat) {
                            Log.d(TAG,"------------------Recieved-------------------");

                            final String date = new
                                    SimpleDateFormat("MMM dd, hh:mm a",
                                    Locale.getDefault()).format(new Date());

                            if (message.getBody()
                                    .equalsIgnoreCase("imageUpload")) {
                                mediaId = true;
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
                                    *//*runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            addChats(message.getBody(),
                                                    privateMessages.getNick(),
                                                    date, mediaId, imageUrl, thumbUrl,
                                                    messageList);
                                        }
                                    });*//*
                                } catch (Exception ex) {
                                    GenExceptions.logException(ex);
                                }
                            } else {
                                mediaId = false;

                                *//*runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        addChats(message.getBody(),
                                                privateMessages.getNick(),
                                                date, mediaId, "", "",
                                                messageList);
                                    }
                                });*//*
                            }
                    *//*presenter.sendMessage(messages, message.getBody(), names, from.asEntityBareJidString().split("@")[0],
                        times, "2017-11-05 04:21:25", PrivateMessagesActivity.this, recyclerView);
                            Log.d(TAG, "newIncomingMessage: -> message: " + message.getBody()+"\nto: " + message.getTo()+"\nfrom: "+message.getFrom());*//*
                        }
                    });

                    Log.d(TAG,"JID-------------------"+privateMessages.getJid());
                    Log.d(TAG,"Own JID-------------------"+login.getXmppUserDetails().getJid());

                    bareJid = JidCreate.entityBareFrom(privateMessages.getJid());

                    chat = chatManager.chatWith(bareJid);
                } catch (Exception ex) {
                    GenExceptions.fireException(ex);
                }*/

                //new PrivateMessagesActivity.XmppConnectionTask().execute();

                /*send.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (privateMessages.getblock() != null && privateMessages.getblock()) {
                            Snackbar.make(view, "Please unblock the respective user", Snackbar.LENGTH_LONG).show();

                            textToSend.setEnabled(false);
                        } else {
                            textToSend.setEnabled(true);
                            try {
                                Message message = new Message();
                                message.setStanzaId(UUID.randomUUID().toString());
                                message.setType(Message.Type.chat);
                                message.setBody(textToSend.getText().toString());

                                String date = new
                                        SimpleDateFormat("MMM dd, hh:mm a",
                                        Locale.getDefault()).format(new Date());

                                chat.send(message);

                                String from = login.getXmppUserDetails().getNick();
                                addChats(textToSend.getText().toString(),
                                        from, date, false, "",
                                        "", messageList, -1);

                                textToSend.setText("");
                            } catch (Exception ex) {
                                GenExceptions.fireException(ex);
                            }
                        }

               *//* String date = new SimpleDateFormat("yyyy-M-dd HH:MM:SS",
                        Locale.getDefault()).format(new Date());

                presenter.sendMessage(messages, textToSend.getText().toString(),
                        names, login.getXmppUserDetails().getNick(), times, date,
                        PrivateMessagesActivity.this, recyclerView);*//*
                    }
                });*/
            }
        }

        if (!Network.isNetworkAvailable(this)) {
            finishAndRemoveTask();
        }
    }

    private void fetchPrivateMessages(final ArrayList<PrivateMessageRealm> messageList/*, PrivateMessagesAdapter adapter*/, final ProgressBar progressBar) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                String nick;
                if (!isFromNotifications)
                    nick = privateMessages.getNick();
                else
                    nick = pm.getNick();
                final RealmResults<PrivateMessageRealm> realmResults = realm
                        .where(PrivateMessageRealm.class)
                        .equalTo("pm_holder_name",
                                nick)
                        .findAll();

                if (realmResults.size() > 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            messageList.clear();

                            messageList.addAll(realmResults);
                            adapter.notifyDataSetChanged();

                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }

    private void updateChat(final String imageUrl, final String thumbUrl) {
        RealmResults<PrivateMessageRealm> results = realm
                .where(PrivateMessageRealm.class).contains("image_url", "image_")
                .findAll();
        realm.beginTransaction();
        for (PrivateMessageRealm messageRealm : results) {
            messageRealm.setImage_url(imageUrl);
            messageRealm.setThumb_url(thumbUrl);
        }
        realm.commitTransaction();
    }

    @Override
    public void uploadImageChat(final File image, final int position) {
        APIInterface apiInterface = APIClient.getClientWithAuth(this)
                .create(APIInterface.class);

        String date = new
                SimpleDateFormat("MMM dd, hh:mm a",
                Locale.getDefault()).format(new Date());

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                String nick;
                if (!isFromNotifications)
                    nick = privateMessages.getNick();
                else
                    nick = pm.getNick();
                RealmResults<PrivateMessageRealm> realmResults = realm
                        .where(PrivateMessageRealm.class)
                        .equalTo("pm_holder_name",
                                nick)
                        .equalTo("image_url", image.getPath())
                        .equalTo("status", ImageUploadStatus.FAILED.getValue())
                        .findAll();

                for (PrivateMessageRealm messageRealm : realmResults) {
                    messageRealm.deleteFromRealm();
                }
            }
        });

        addChats("imageUpload",
                login.getXmppUserDetails().getNick(),
                date, true, image.getPath(),
                "", messageList, ImageUploadStatus.INPROGRESS.getValue());

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), image);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", image.getName(), requestFile);
        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), "upload_test");

        Call<GroupImageUpload> uploadImage = apiInterface.personalUpload(body, name);
        uploadImage.enqueue(new Callback<GroupImageUpload>() {
            @Override
            public void onResponse(Call<GroupImageUpload> call, Response<GroupImageUpload> response) {
                final String imageUrl = response.body().getSuccess().getImageUri();
                final String thumbnailUrl = response.body().getSuccess().getThumbUri();
                conn1.getUser();
                try {
                    Jid ownJid = JidCreate.entityBareFrom(login.getXmppUserDetails().getJid());

                    Message message = new Message();
                    message.setStanzaId(UUID.randomUUID().toString());
                    message.setType(Message.Type.chat);
                    message.setBody("imageUpload");
                    message.setTo(bareJid);
                    message.setFrom(ownJid);

                    Map<String, String> attr = new HashMap<>();
                    attr.put("image", imageUrl);
                    attr.put("thumbnail", thumbnailUrl);

                    StandardExtensionElement sendImage = StandardExtensionElement.builder("image", "urn:xmpp:image").addAttribute("name", imageUrl).build();
                    StandardExtensionElement sendThumbnail = StandardExtensionElement.builder("thumbnail", "urn:xmpp:thumbnail").addAttribute("thumbnail", thumbnailUrl).build();

                    Collection<ExtensionElement> sendImages = new ArrayList<>();
                    sendImages.add(sendImage);
                    sendImages.add(sendThumbnail);

                    String msg = message.toXML().toString();
                    message.addExtensions(sendImages);
                    /*String xml = "<message to='" + bareJid + "' " +
                            "from='" + ownJid + "' " +
                            "id='" + UUID.randomUUID().toString() + "' " +
                            "type='chat'" +
                            "image='" + imageUrl + "'" +
                            "thumbnail='" + thumbnailUrl + "'><body>imageUpload</body></message>";

                    String msgNew = msg.replace("><body>imageUpload</body></message>", " image='" + imageUrl + "' thumbnail='" + thumbnailUrl + "'><body>imageUpload</body></message>");*/
                    Log.d("::::PM", msg);
                    chat.send(message);

                    /*String date = new
                            SimpleDateFormat("MMM dd, hh:mm a",
                            Locale.getDefault()).format(new Date());*/

                    //updateChat(imageUrl, thumbnailUrl);
                    final String nick;
                    if (!isFromNotifications)
                        nick = privateMessages.getNick();
                    else
                        nick = pm.getNick();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            RealmResults<PrivateMessageRealm> realmResults = realm
                                    .where(PrivateMessageRealm.class)
                                    .equalTo("pm_holder_name",
                                            nick)
                                    .equalTo("status", ImageUploadStatus.INPROGRESS.getValue())
                                    .findAll();

                            for (PrivateMessageRealm messageRealm : realmResults) {
                                messageRealm.deleteFromRealm();
                            }
                        }
                    });

                    String date = new
                            SimpleDateFormat("MMM dd, hh:mm a",
                            Locale.getDefault()).format(new Date());
                    addChats("imageUpload",
                            login.getXmppUserDetails().getNick(),
                            date, true, image.getPath(),
                            "", messageList, ImageUploadStatus.SUCCESS.getValue());
                    /*addChats("imageUpload",
                            login.getXmppUserDetails().getNick(),
                            date, true, image.getPath(),
                            thumbnailUrl, messageList*//*, true*//*);*/
                    /*realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            RealmResults<PrivateMessageRealm> realmResults = realm
                                    .where(PrivateMessageRealm.class)
                                    .equalTo("pm_holder_name",
                                            privateMessages.getNick())
                                    .findAll();
                        }
                    });
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            RealmResults<PrivateMessageRealm> realmResults = realm
                                    .where(PrivateMessageRealm.class)
                                    .equalTo("pm_holder_name",
                                            privateMessages.getNick())
                                    .equalTo("status", ImageUploadStatus.INPROGRESS.getValue())
                                    .findAll();

                            for (PrivateMessageRealm messageRealm : realmResults) {
                                if (realmResults.size() == 1) {
                                    //messageRealm.setMessage_id(messageRealm.getMessage_id());
                                    messageRealm.setStatus(ImageUploadStatus.SUCCESS.getValue());
                                    realm.copyToRealmOrUpdate(messageRealm);
                                }
                            }

                            if (realmResults.size() > 0) {
                                messageList.clear();
                                messageList.addAll(realmResults);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });*/
                } catch (Exception ex) {
                    GenExceptions.logException(ex);
                }
            }

            @Override
            public void onFailure(Call<GroupImageUpload> call, Throwable t) {
                GenExceptions.fireException(t);
                final String nick;
                if (!isFromNotifications)
                    nick = privateMessages.getNick();
                else
                    nick = pm.getNick();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        RealmResults<PrivateMessageRealm> realmResults = realm
                                .where(PrivateMessageRealm.class)
                                .equalTo("pm_holder_name",
                                        nick)
                                .equalTo("status", ImageUploadStatus.INPROGRESS.getValue())
                                .findAll();

                        for (PrivateMessageRealm messageRealm : realmResults) {
                            messageRealm.deleteFromRealm();
                        }
                    }
                });

                String date = new
                        SimpleDateFormat("MMM dd, hh:mm a",
                        Locale.getDefault()).format(new Date());
                addChats("imageUpload",
                        login.getXmppUserDetails().getNick(),
                        date, true, image.getPath(),
                        "", messageList, ImageUploadStatus.FAILED.getValue());
            }
        });
    }

    void addChats(String message, String from, String date, Boolean multimediaIdentifier, String imageUrl, String thumbUrl, final ArrayList<PrivateMessageRealm> messageList, int status) {
        final String nick;
        if (!isFromNotifications)
            nick = privateMessages.getNick();
        else
            nick = pm.getNick();
        realm.beginTransaction();
        PrivateMessageRealm privateMessageRealm = realm
                .createObject(PrivateMessageRealm.class, UUID.randomUUID().toString());
        privateMessageRealm.setMessage(message);
        privateMessageRealm.setMessage_sender(from);
        privateMessageRealm.setMessage_datetime(date);
        privateMessageRealm.setMultimedia_identifier(multimediaIdentifier);
        privateMessageRealm.setPMHolderName(nick);
        privateMessageRealm.setStatus(status);

        if (imageUrl.equals("") && thumbUrl.equals("")) {
            privateMessageRealm.setImage_url("");
            privateMessageRealm.setThumb_url("");
        } else if (imageUrl.contains("image_")) {
            privateMessageRealm.setImage_url(imageUrl);
            privateMessageRealm.setThumb_url("");
        } else if (thumbUrl.equals("")) {
            privateMessageRealm.setImage_url(imageUrl);
            privateMessageRealm.setThumb_url("");
        } else {
            privateMessageRealm.setImage_url(login.getResourceHostDomain() + imageUrl);
            privateMessageRealm.setThumb_url(login.getResourceHostDomain() + thumbUrl);
        }

        realm.commitTransaction();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<PrivateMessageRealm> realmResults = realm
                        .where(PrivateMessageRealm.class)
                        .equalTo("pm_holder_name",
                                nick)
                        .findAll();

                messageList.clear();
                messageList.addAll(realmResults);

                mLayoutManager.setStackFromEnd(true);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private class XMPPIncomingListener implements IncomingChatMessageListener {
        @Override
        public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
            Log.d(TAG, "newIncomingMessage: -> Message from: " + message.getFrom().toString());
            Log.d(TAG, "newIncomingMessage: -> Message to: " + message.getTo().toString());
            Log.d(TAG, "newIncomingMessage: -> Message body: " + message.getBody().toString());
        }
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
                int id;
                String nick, jid;
                if (!isFromNotifications) {
                    id = privateMessages.getId();
                    nick = privateMessages.getNick();
                    jid = privateMessages.getJid();
                }
                else {
                    id = pm.getId();
                    nick = pm.getNick();
                    jid = pm.getJid();
                }
                String sendMessage3 = "<iq type='set' to='push' id='send'>\n" +
                        "<receiver id='" + id + "' type='user'>\n" +
                        "<message id='" + UUID.randomUUID() + "' uniqueMessageID='19EB6DC4-8CA8-4E67-91C9-E779BE0DC0CD' receiver='" + nick + "' from='" + login.getXmppUserDetails().getJid() + "' to='" + jid + "' type='chat' nick='" + login.getXmppUserDetails().getNick() + "'><body>" + textToSend.getText().toString() + "</body></message>\n" +
                        "</receiver>\n" +
                        "<sender id='" + login.getId() + "'>" + login.getXmppUserDetails().getNick() + "</sender>\n" +
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

                String sendMessage2 = "<iq type='set' to='auth' id='auth2'>\n" +
                        "<query xmlns='quadrant:iq:auth'>\n" +
                        "<jabber-username>" + login.getXmppUserDetails().getJid().split("@")[0] + "</jabber-username>\n" +
                        "<jabber-password>" + login.getXmppUserDetails().getPassword() + "</jabber-password>\n" +
                        "<device-token>" + login.getToken() + "</device-token>\n" +
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean blocked = false;
        if (!isFromNotifications) {
            if (privateMessages.getblock() != null)
                blocked = privateMessages.getblock();
            else
                blocked = false;
        } else {
            if (pm.getBlock() != null)
                blocked = pm.getBlock();
            else
                blocked = false;
        }
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.private_message_menu, menu);
        Log.d(TAG, "onOptionsItemSelected: -> block: " + blocked);
        Toast.makeText(this, "block: " + blocked, Toast.LENGTH_SHORT).show();

        if (blocked) {
            menu.getItem(1).setTitle(getString((R.string.unblock_user)));
        } else {
            menu.getItem(1).setTitle(getString((R.string.block_user)));
        }


        //inflater.notifyAll();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_view_user:
                Intent intent = new Intent(PrivateMessagesActivity.this,
                        MemberProfileActivity.class);
                intent.putExtra("userInfo", privateMessages);
                if (username != null) {
                    intent.putExtra("username", username);
                }

                startActivity(intent);
                break;
            case R.id.menu_block_user:
                int id;
                if (!isFromNotifications) {
                    id = privateMessages.getId();
                } else {
                    id = pm.getId();
                }
                if (item.getTitle().equals(getString(R.string.block_user))) {
                    item.setTitle(getString(R.string.unblock_user));

                    send.setEnabled(false);
                    presenter.blockUser(String.valueOf(id), this);
                } else if (item.getTitle().equals(getString(R.string.unblock_user))) {
                    item.setTitle(getString(R.string.block_user));

                    send.setEnabled(true);
                    presenter.unBlockUser(String.valueOf(id), this);
                }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void successfulMessage(String msg) {

    }

    @Override
    public void makeProgreessBarVisible() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void makeProgreessBarInvisible() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.getData() != null) {
                Uri uri = data.getData();

                File file = new File(getPath(this, uri));
                uploadImage(file);
            } else if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                int currItem = 0;
                while (currItem < count) {
                    Uri uri = data.getClipData().getItemAt(currItem).getUri();
                    currItem = currItem + 1;
                    File file = new File(getPath(this, uri));
                    uploadImage(file);
                }
            }
        }
    }

    private void uploadImage(final File image) {
        APIInterface apiInterface = APIClient.getClientWithAuth(this)
                .create(APIInterface.class);

        String date = new
                SimpleDateFormat("MMM dd, hh:mm a",
                Locale.getDefault()).format(new Date());

        addChats("imageUpload",
                login.getXmppUserDetails().getNick(),
                date, true, image.getPath(),
                "", messageList, ImageUploadStatus.INPROGRESS.getValue());

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), image);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", image.getName(), requestFile);
        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), "upload_test");

        Call<GroupImageUpload> uploadImage = apiInterface.personalUpload(body, name);
        uploadImage.enqueue(new Callback<GroupImageUpload>() {
            @Override
            public void onResponse(Call<GroupImageUpload> call, Response<GroupImageUpload> response) {
                final String imageUrl = response.body().getSuccess().getImageUri();
                final String thumbnailUrl = response.body().getSuccess().getThumbUri();
                conn1.getUser();
                try {
                    Jid ownJid = JidCreate.entityBareFrom(login.getXmppUserDetails().getJid());

                    Message message = new Message();
                    message.setStanzaId(UUID.randomUUID().toString());
                    message.setType(Message.Type.chat);
                    message.setBody("imageUpload");
                    message.setTo(bareJid);
                    message.setFrom(ownJid);

                    Map<String, String> attr = new HashMap<>();
                    attr.put("image", imageUrl);
                    attr.put("thumbnail", thumbnailUrl);

                    StandardExtensionElement sendImage = StandardExtensionElement.builder("image", "urn:xmpp:image").addAttribute("name", imageUrl).build();
                    StandardExtensionElement sendThumbnail = StandardExtensionElement.builder("thumbnail", "urn:xmpp:thumbnail").addAttribute("thumbnail", thumbnailUrl).build();

                    Collection<ExtensionElement> sendImages = new ArrayList<>();
                    sendImages.add(sendImage);
                    sendImages.add(sendThumbnail);

                    String msg = message.toXML().toString();
                    message.addExtensions(sendImages);
                    /*String xml = "<message to='" + bareJid + "' " +
                            "from='" + ownJid + "' " +
                            "id='" + UUID.randomUUID().toString() + "' " +
                            "type='chat'" +
                            "image='" + imageUrl + "'" +
                            "thumbnail='" + thumbnailUrl + "'><body>imageUpload</body></message>";

                    String msgNew = msg.replace("><body>imageUpload</body></message>", " image='" + imageUrl + "' thumbnail='" + thumbnailUrl + "'><body>imageUpload</body></message>");*/
                    Log.d("::::PM", msg);
                    chat.send(message);

                    /*String date = new
                            SimpleDateFormat("MMM dd, hh:mm a",
                            Locale.getDefault()).format(new Date());*/

                    //updateChat(imageUrl, thumbnailUrl);
                    final String nick;
                    if (!isFromNotifications) {
                        nick = privateMessages.getNick();
                    } else {
                        nick = pm.getNick();
                    }
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            RealmResults<PrivateMessageRealm> realmResults = realm
                                    .where(PrivateMessageRealm.class)
                                    .equalTo("pm_holder_name",
                                            nick)
                                    .equalTo("message", "imageUpload")
                                    .equalTo("status", ImageUploadStatus.INPROGRESS.getValue())
                                    .findAll();

                            for (PrivateMessageRealm messageRealm : realmResults) {
                                messageRealm.deleteFromRealm();
                            }
                        }
                    });

                    String date = new
                            SimpleDateFormat("MMM dd, hh:mm a",
                            Locale.getDefault()).format(new Date());
                    addChats("imageUpload",
                            login.getXmppUserDetails().getNick(),
                            date, true, image.getPath(),
                            "", messageList, ImageUploadStatus.SUCCESS.getValue());
                    /*addChats("imageUpload",
                            login.getXmppUserDetails().getNick(),
                            date, true, image.getPath(),
                            thumbnailUrl, messageList*//*, true*//*);*/
                    /*realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            RealmResults<PrivateMessageRealm> realmResults = realm
                                    .where(PrivateMessageRealm.class)
                                    .equalTo("pm_holder_name",
                                            privateMessages.getNick())
                                    .findAll();
                        }
                    });
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            RealmResults<PrivateMessageRealm> realmResults = realm
                                    .where(PrivateMessageRealm.class)
                                    .equalTo("pm_holder_name",
                                            privateMessages.getNick())
                                    .equalTo("status", ImageUploadStatus.INPROGRESS.getValue())
                                    .findAll();

                            for (PrivateMessageRealm messageRealm : realmResults) {
                                if (realmResults.size() == 1) {
                                    //messageRealm.setMessage_id(messageRealm.getMessage_id());
                                    messageRealm.setStatus(ImageUploadStatus.SUCCESS.getValue());
                                    realm.copyToRealmOrUpdate(messageRealm);
                                }
                            }

                            if (realmResults.size() > 0) {
                                messageList.clear();
                                messageList.addAll(realmResults);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });*/
                } catch (Exception ex) {
                    GenExceptions.logException(ex);
                }
            }

            @Override
            public void onFailure(Call<GroupImageUpload> call, Throwable t) {
                GenExceptions.fireException(t);
                final String nick;
                if (!isFromNotifications) {
                    nick = privateMessages.getNick();
                } else {
                    nick = pm.getNick();
                }
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        RealmResults<PrivateMessageRealm> realmResults = realm
                                .where(PrivateMessageRealm.class)
                                .equalTo("pm_holder_name",
                                        nick)
                                .equalTo("status", ImageUploadStatus.INPROGRESS.getValue())
                                .findAll();

                        for (PrivateMessageRealm messageRealm : realmResults) {
                            messageRealm.deleteFromRealm();
                        }
                    }
                });

                String date = new
                        SimpleDateFormat("MMM dd, hh:mm a",
                        Locale.getDefault()).format(new Date());
                addChats("imageUpload",
                        login.getXmppUserDetails().getNick(),
                        date, true, image.getPath(),
                        "", messageList, ImageUploadStatus.FAILED.getValue());
            }
        });
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

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }
}
