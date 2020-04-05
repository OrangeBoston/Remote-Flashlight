package com.orangeboston.remoteflashlight;

import android.app.Application;
import android.content.Context;

import com.orangeboston.remoteflashlight.utils.SharePreferenceManager;

import cn.jpush.im.android.api.JMessageClient;

public class MyApplication extends Application {

    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        JMessageClient.init(getApplicationContext(), true);
        SharePreferenceManager.init(getApplicationContext(), "remoteflashlight");
    }
}