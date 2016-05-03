package com.example.rax.fetcher;

import com.firebase.client.Firebase;

/**
 * Created by rax on 4/29/2016.
 */
public class MyFirebase extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
