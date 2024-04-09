package com.example.textapp.entity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.example.textapp.TCPClient.TCPClient;
import com.example.textapp.TCPClient.TCPClientThread;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    //将查询结果list<Map<String,String>>变为bundle形式;
    //bundle的格式:"ResultsCount":Cnt;
    //              "QueryColums:[columns1,columns2]
    //              "result0":[String,String...]
    //              "result1":[String,String...]
    public Bundle ListMapToBundle(List<Map<String,String>> lmap){
        Bundle bundle=new Bundle();
        int count=lmap.size();
        bundle.putInt(MessageType.BUNDLE_KEY_RESULTSCOUNT,count);
        ArrayList<String> queryColumns=new ArrayList<>();
        for(int i=0;i<count;++i){
            Map<String,String> map=lmap.get(i);
            if(i==0){
                for(String key:map.keySet()){
                    
                }
            }
        }
        return bundle;
    }

    public void handleMessage(Message msg){
        super.handleMessage(msg);
        //对各种Message进行处理:
        switch(msg.what){
            case MessageType.WHAT_QUERY:
                //获取查询结果;
                List<Map<String,String>> results=mThread.QueryToServer(msg);
                //对于查询结果通过message携带bundle返回给主线程;
                Message message=new Message();
                message.what=MessageType.WHAT_QUERY;
                Bundle bundle=new Bundle();
                //
                break;
            case MessageType.WHAT_INSERT:
                break;
        }
    }
}
