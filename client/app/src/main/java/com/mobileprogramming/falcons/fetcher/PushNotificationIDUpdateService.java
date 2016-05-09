package com.mobileprogramming.falcons.fetcher;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.android.gms.iid.InstanceIDListenerService;

public class PushNotificationIDUpdateService extends InstanceIDListenerService {

    @Override
    public void onTokenRefresh() {
        //Ask registration service to reregister us.
        Intent intent = new Intent(this, PushNotificationRegistrationService.class);
        startService(intent);
    }
}
