package com.afoodchronicle.chat;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.afoodchronicle.R;
import com.google.firebase.messaging.RemoteMessage;

import static com.afoodchronicle.utilities.Static.FROM_SENDER_ID;
import static com.afoodchronicle.utilities.Static.VISIT_USER_ID;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private android.content.Context mContext;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notification_title = remoteMessage.getNotification().getTitle();
        String notification_body = remoteMessage.getNotification().getBody();
        String click_action = remoteMessage.getNotification().getClickAction();
        String from_sender_id =remoteMessage.getData().get(FROM_SENDER_ID).toString();

        Intent resultIntent = new Intent(click_action);
        resultIntent.putExtra(VISIT_USER_ID, from_sender_id);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                 new NotificationCompat.Builder(this)
                .setContentTitle(notification_title)
                .setContentText(notification_body)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);



        int mNotificationId = (int) System.currentTimeMillis();
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

    }
}
