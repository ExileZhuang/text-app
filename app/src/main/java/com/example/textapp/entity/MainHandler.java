package com.example.textapp.entity;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.example.textapp.TCPClient.TCPClient;

public class MainHandler extends Handler {

    private TCPClient mClient;

    public MainHandler(TCPClient client){
        super();
        mClient=client;
    }

    public MainHandler(Looper looper,TCPClient client){
        super(looper);
        mClient=client;
    }

    public void handleMessage(Message msg){
        super.handleMessage(msg);
        //对各种Message进行处理:
//        switch(msg){
//        }
    }
}
