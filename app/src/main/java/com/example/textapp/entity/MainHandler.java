package com.example.textapp.entity;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class MainHandler extends Handler {

    public MainHandler(){
        super();
    }

    public MainHandler(Looper looper){
        super(looper);
    }

    public void handleMessage(Message msg){
        super.handleMessage(msg);
        //对各种Message进行处理:
//        switch(msg){
//        }
    }
}
