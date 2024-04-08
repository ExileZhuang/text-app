package com.example.textapp.entity;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.example.textapp.TCPClient.TCPClient;
import com.example.textapp.TCPClient.TCPClientThread;

public class ThreadHandler extends Handler {

    private TCPClientThread mThread;

    public ThreadHandler(TCPClientThread thread){
        super();
        mThread=thread;
    }

    public ThreadHandler(Looper looper,TCPClientThread thread){
        super(looper);
        mThread=thread;
    }

    public void handleMessage(Message msg){
        super.handleMessage(msg);
        //对各种Message进行处理:
        switch(msg.what){
            case MessageType.WHAT_QUERY:
                mThread.QueryToServer(msg);
                break;
            case MessageType.WHAT_INSERT:
                break;
        }
    }
}
