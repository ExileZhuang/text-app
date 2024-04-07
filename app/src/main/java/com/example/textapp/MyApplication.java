package com.example.textapp;

import android.app.Application;
import android.content.res.Configuration;

import com.example.textapp.TCPClient.TCPClient;

public class MyApplication extends Application {

    private static MyApplication mApp;

    private TCPClient mClient;


    public static MyApplication getInstance(){
        return mApp;
    }

    public void onCreate() {
        super.onCreate();
        mApp=this;
        mClient=new TCPClient();
        mClient.Test();
    }

    public void onTerminate(){
        super.onTerminate();
    }

    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }
}
