package com.gametime.quadrant;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.gametime.quadrant.Utils.Constants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class fcmMessageService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseMessaging";

    public fcmMessageService() {

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            if (true) {
                scheduleJob();
            } else {
                handleNow();
            }
        }

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();
        String clickAction = remoteMessage.getNotification().getClickAction();
        String groupId = remoteMessage.getData().get("group_id");
        String userId = remoteMessage.getData().get("id");
        Log.d(TAG, "Click action: " + remoteMessage.getData().get("click_action"));
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
        ComponentName componentName = activityManager.getRunningTasks(1).get(0).topActivity;
        if (componentName != null) {
            if (componentName.toString().contains("GroupMessageActivity")) {
                Log.d(TAG, "Current activity name: Group Messages");
            } else if (componentName.toString().contains("PrivateMessagesActivity")) {
                Log.d(TAG, "Current activity name: Some other activity");
                sendNotification(body, title, remoteMessage.getData().get("click_action"), userId, true);
            } else {
                Log.d(TAG, "Current activity name: Some other activity");
                sendNotification(body, title, remoteMessage.getData().get("click_action"), groupId, false);
            }
        }
        else {
            Log.d(TAG, "No activity running currently.....");
            sendNotification(body, title, clickAction, groupId, false);
        }
    }

    private void sendNotification(String body, String title, String clickAction, String groupId, boolean isPrivateChats) {
        Intent intent = new Intent(clickAction);
        intent.putExtra(getString(R.string.from_notifications), true);
        intent.putExtra(Constants.EXTRA_GROUP_ID_KEY, groupId);
        if (isPrivateChats) {
            intent.putExtra("message", body);
            intent.putExtra("title", title);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(body);
        notificationBuilder.setSmallIcon(R.drawable.quadrant_logo);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }


    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    private void scheduleJob() {
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job myJob = dispatcher.newJobBuilder()
                .setService(MyJobService.class)
                .setTag("my-job-tag")
                .build();
        dispatcher.schedule(myJob);
    }


}
