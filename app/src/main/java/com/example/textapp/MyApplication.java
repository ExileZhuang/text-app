package com.example.textapp;

import android.app.Application;
import android.content.res.Configuration;

import com.example.textapp.database.NotesDBHelper;

public class MyApplication extends Application {

    private static MyApplication mApp;

    public static MyApplication getInstance(){
        return mApp;
    }

    public void onCreate() {
        super.onCreate();
    }

    public void onTerminate(){
        super.onTerminate();
    }

    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }
}
