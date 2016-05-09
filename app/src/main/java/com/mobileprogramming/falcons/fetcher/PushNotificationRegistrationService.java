package com.mobileprogramming.falcons.fetcher;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class PushNotificationRegistrationService extends IntentService {
    private static final String kPushNotificationRegistrationServiceTag = "PushNotificationRegServ";
    private static final int kDefaultPort = 7263;

    private void postToServer(int port, String instanceID) {
        try {
            //Connect to the server on the given port.
            Socket connection = new Socket(getString(R.string.server_id), port);
            //Setup buffered streams to interface with server.
            PrintWriter serverOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(connection.getOutputStream())), true);
            BufferedReader serverIn = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            //Once connection is confirmed, send our instance ID as formatted JSON.
            String payload = "{ \"client_id\" : \"" + instanceID + "\" }";
            if(serverOut == null || serverOut.checkError()) {
                //No uplink to server, abort.
                Log.e(kPushNotificationRegistrationServiceTag, "No uplink to server, can't subscribe for push notifications. Aborting!");
                return;
            }
            //Otherwise send the payload out.
            serverOut.println(payload);
            serverOut.flush();
            Log.d(kPushNotificationRegistrationServiceTag, "Sent push notification subscription request...");

            boolean gotResponse = false;
            //Wait for the response.
            while(!gotResponse) {
                String msg = serverIn.readLine();
                //Did we get a non-empty response?
                if(msg != null) {
                    //If so, parse it.
                    if(msg == "True") {
                        //We succeeded!
                        Log.d(kPushNotificationRegistrationServiceTag, "Subscription request accepted");
                    }
                    else {
                        //Subscription failed.
                        Log.d(kPushNotificationRegistrationServiceTag, "Subscription request rejected! can't subscribe for push notifications!");
                    }
                }
            }

            //Flush and close I/O links.
            serverOut.flush();
            serverOut.close();
            serverIn.close();
            //Disconnect from the server.
            connection.close();
        }
        catch(Exception e) {
            //Log failure to open socket.
            Log.e(kPushNotificationRegistrationServiceTag, "Unknown error occurred while initializing push notifications. Can't subscribe for push notifications!");
        }
    }

    public PushNotificationRegistrationService() {
        super(kPushNotificationRegistrationServiceTag);
    }

    /*
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }*/

    /**
     * This method is invoked on the worker thread with a request to process.
     * Only one Intent is processed at a time, but the processing happens on a
     * worker thread that runs independently from other application logic.
     * So, if this code takes a long time, it will hold up other requests to
     * the same IntentService, but it will not hold up anything else.
     * When all requests have been handled, the IntentService stops itself,
     * so you should not call {@link #stopSelf}.
     *
     * @param intent The value passed to {@link
     *               Context#startService(Intent)}.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        //Get our instance ID.
        InstanceID instanceID = InstanceID.getInstance(this);
        //Get our token from that.
        try {
            String token = instanceID.getToken(getString(R.string.server_id),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            //Now send this to the server...
            postToServer(kDefaultPort, token);
        }
        catch (Exception e) {
            //Something bad happened.
            Log.e(kPushNotificationRegistrationServiceTag, "Couldn't get new instance ID! Can't subscribe to push notifications!");
        }
    }
}
