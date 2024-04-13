package com.example.textapp;

import android.app.Application;
import android.content.res.Configuration;

import com.example.textapp.TCPClient.TCPClient;
import com.example.textapp.database.NotesDBHelper;

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
        mClient.closeClient();
        NotesDBHelper mDBHelper= NotesDBHelper.getInstance(this);
        mDBHelper.closeLink();
        mDBHelper.close();
        super.onTerminate();
    }

    public TCPClient getClient(){
        return mClient;
    }

    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }
}
