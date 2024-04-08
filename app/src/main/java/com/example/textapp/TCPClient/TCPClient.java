package com.example.textapp.TCPClient;

import android.os.Bundle;
import android.os.Message;

import com.example.textapp.entity.MainHandler;
import com.example.textapp.entity.MessageType;
import com.example.textapp.entity.ThreadHandler;
import com.example.textapp.entity.User_Info;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//用于实现TCPClientThread与Application UI等主线程之间的接口;
//封装handler实现两者间通信;
public class TCPClient {

    private MainHandler reciveHandler;
    //主线程中reciveHandler即子线程中的sendHandler,接受由子线程发送的消息;

    private ThreadHandler sendHandler;
    //主线程中sendhandler即子线程中的reciveHandler,发送消息给子线程;

    private TCPClientThread thread;

    //Attention:如果服务器未开启会形成阻塞,app无法运行;
    public TCPClient(){
        reciveHandler =new MainHandler(this);
        thread=new TCPClientThread(reciveHandler);
        thread.start();
        sendHandler=thread.getReciveHandler();
    }

    //发送消息给客户端线程:需要查询服务器中指定UserId中的相关信息;
    public void sendQueryPasswordByUserId(String UserId) {
        Map<String,String> selections=new HashMap<>();
        selections.put(User_Info.USER_ID,UserId);
        ArrayList<String> queryColumn=new ArrayList<>();
        queryColumn.add(User_Info.PASSWORD);
        sendQueryColumnsBySelectionsToTable(User_Info.TABLE_USER,queryColumn, selections);
    }

    //bundle中数据:tableName,queryColumns,(keys,values)=>selections
    public void sendQueryColumnsBySelectionsToTable(String TableName, ArrayList<String> queryColumns, Map<String,String> selections){
        Message msg=new Message();
        msg.what= MessageType.WHAT_QUERY;
        Bundle bundle=new Bundle();
        bundle.putString(MessageType.BUNDLE_KEY_TABLENAME, TableName);
        bundle.putStringArrayList(MessageType.BUNDLE_KEY_QUERYCOLUMNS,queryColumns);
        ArrayList<String> keys=new ArrayList<String>();
        ArrayList<String> values=new ArrayList<String>();
        for(String key:selections.keySet()){
            keys.add(key);
            values.add(selections.get(key));
        }
        bundle.putStringArrayList(MessageType.BUNDLE_KEY_SELECTIONKEYS, keys);
        bundle.putStringArrayList(MessageType.BUNDLE_KEY_SELECTIONSVALUES,values);
        msg.setData(bundle);

        sendHandler.sendMessage(msg);
    }
}
