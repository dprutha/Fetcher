package com.mobileprogramming.falcons.fetcher;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;

import com.google.android.gms.gcm.GcmListenerService;

public class PushNotificationListenerService extends GcmListenerService {
    private static final String kPushNotificationListenerServiceTag = "PushNotificationListenerService";

    private void addNotification() {
        NotificationCompat.Builder builder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Your Daily Fetcher Report is Ready!")
                        .setContentText("Click Here to View")
                        .setAutoCancel(true);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

    @Override
    public void onMessageReceived(String from, Bundle data) {
        //Server doesn't really send any data, so just notify.
        addNotification();
    }
}
