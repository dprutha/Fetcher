package com.mobileprogramming.falcons.fetcher;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.graphics.Typeface;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;


public class MainActivity extends AppCompatActivity {
    static final String kTagMainActivity = "MainActivity";
    private boolean googlePlayReady = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Alarm Function~~~~~~~~~~
        //Broadcast receiver is set to pop the notification
        BroadcastReceiver BR = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                addNotification();
            }
        };
        registerReceiver(BR, new IntentFilter("com.example.filterMe"));

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        //alarm is set at 2pm
        calendar.set(Calendar.HOUR_OF_DAY, 14);

        //broadcast setup
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent broadcastIntent = PendingIntent.getBroadcast( this, 0, new Intent("com.example.filterMe"),0);

        //Alarm manager is set to wake the device up at the set calendar time (2pm right now) and repeat once a day at the same time.
        //using setInexact and RTC formats, so notification will be very low priority. Discuss later
        alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, broadcastIntent);

        //Setup push notifications.
        //Ask registration service to set us up.
        Intent intent = new Intent(this, PushNotificationRegistrationService.class);
        startService(intent);
    }

    //Function for Adding Notification
    //Launches Notification with Fetcher Logo and preset Messages
    //Figure out how to redirect to application
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

    //Function for Removing notification
    //Not used. Notification auto delete on touch
    private void removeNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(0);
    }
}
