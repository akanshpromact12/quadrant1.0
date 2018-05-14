package com.gametime.quadrant.Application;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.gametime.quadrant.Models.QuadrantLoginDetails;
import com.gametime.quadrant.Utils.Constants;
import com.google.gson.Gson;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.impl.JidCreate;

import java.net.InetAddress;

import io.realm.Realm;
import io.realm.RealmConfiguration;

import static com.gametime.quadrant.Utils.Constants.PREF_KEY_LOGGED_IN_USER;

public class MyApp extends Application {
    private static final String TAG = "MyApp";
    private XMPPTCPConnection conn1;

    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(getApplicationContext());
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .name(Realm.DEFAULT_REALM_NAME)
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
    }

    public void connectToXmPP() {
        try {
            String APICreds = getSharedPreferences(Constants.PREF_FILE_NAME,
                    Context.MODE_PRIVATE).getString(PREF_KEY_LOGGED_IN_USER, "");
            QuadrantLoginDetails login = new Gson().fromJson(APICreds, QuadrantLoginDetails.class);

            String jid = login.getXmppUserDetails().getJid();
            Log.d(TAG, "On Create JID = " + jid);
            String password = login.getXmppUserDetails().getPassword();
            String host = jid.split("@")[1];
            String username = jid.split("@")[0];
            Log.d(TAG, "onCreate: -> inside try");

            Log.d(TAG, "On Create Address = " + InetAddress.getByName(host));
            DomainBareJid serviceName = JidCreate.domainBareFrom(jid);
            XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration
                    .builder()
                    .setUsernameAndPassword(username, password)
                    .setXmppDomain(serviceName)
                    .setHostAddress(InetAddress.getByName(host))
                    //.setHost("10.1.81.144")
                    .setConnectTimeout(60000)
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .setDebuggerEnabled(true)// to view what's happening in detail
                    .setSendPresence(true)
                    .build();

            conn1 = new XMPPTCPConnection(config);
            conn1.setUseStreamManagement(true);
            conn1.setFromMode(XMPPConnection.FromMode.USER);

            // Enable automatic reconnection
            ReconnectionManager.getInstanceFor(conn1).enableAutomaticReconnection();

            conn1.connect();

            if (conn1.isConnected()) {
                Log.d(TAG, "onCreate: -> XMPP connection successful");
            }
            conn1.login();

            if (conn1.isAuthenticated()) {
                Log.d(TAG, "onCreate: -> Connection is authentic");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public XMPPTCPConnection getXMPPConnection(){
        return conn1;
    }
}
