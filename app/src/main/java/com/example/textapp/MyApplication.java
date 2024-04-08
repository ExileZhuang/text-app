package com.example.textapp;

import android.app.Application;
import android.content.res.Configuration;

import com.example.textapp.TCPClient.TCPClient;
import com.example.textapp.TCPClient.TCPClientThread;

public class MyApplication extends Application {

    private static MyApplication mApp;

    private TCPClient mClient=null;


    public static MyApplication getInstance(){
        return mApp;
    }

    public void onCreate() {
        super.onCreate();
        mApp=this;
        mClient=new TCPClient();
    }

    public void onTerminate(){
        super.onTerminate();
    }

    public TCPClient getClient(){
        return mClient;
    }

    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }
}
