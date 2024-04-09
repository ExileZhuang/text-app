package com.example.textapp.TCPClient;

import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.util.JsonReader;
import android.util.Log;

import com.example.textapp.entity.MainHandler;
import com.example.textapp.entity.MessageType;
import com.example.textapp.entity.NetMessage;
import com.example.textapp.entity.ThreadHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class TCPClientThread extends Thread{

    public static final String SERVER_IP="192.168.56.1";

    public static final int SERVER_PORT=8080;

    private BufferedReader input;

    private Socket socket;


    private MainHandler sendHandler;
    //handler发送给主线程消息的handler;

    private ThreadHandler reciveHandler;
    //接受主线程消息的handler;

    public TCPClientThread(MainHandler mainHandler){
        input=null;
        socket=null;
        sendHandler =mainHandler;
        reciveHandler=null;
    }

    public void sendString(String str){
        try{
            BufferedWriter output=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"utf-8"));
            output.write(str+"\n");
            output.flush();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void sendNetMessage(NetMessage message){
        sendString(message.toString());
    }

    public String reciveNetMessage(){
        String msg=null;
        try{
            while((msg=input.readLine())!=null){
                break;
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return msg;
    }

    public void Test(){
        try{
            String content="hello,world";
            sendString(content);
            Log.v("Note","Send Messagge: "+content);
            String ans;
            while((ans=input.readLine())!=null){
                break;
            }
            Log.v("Note","Recive Message:"+ans);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public ThreadHandler getReciveHandler(){
        while(reciveHandler==null){
            //堵塞直到reciveHandler建立;
        }
        return reciveHandler;
    }

    @Override
    public void run() {
        //建立连接;
        try{
            socket=new Socket(SERVER_IP,SERVER_PORT);
            //Log.v("Note","Connect Status:"+String.valueOf(socket.isConnected()));
            input=new BufferedReader(new InputStreamReader(socket.getInputStream(),"utf-8"));
        }catch (Exception e){
            e.printStackTrace();
        }

        //创建Looper用于子、父进程间通信;
        Looper.prepare();
        reciveHandler=new ThreadHandler(Looper.myLooper(),this);



        //不断轮询查看有无消息;
        Looper.loop();
    }


    //向服务器通过发起查询并且获得结果;
    public List<Map<String,String>> QueryToServer(Message msg) {
        Bundle bundle=msg.getData();

        NetMessage sendMessage=new NetMessage();

        String tableName=bundle.getString(MessageType.BUNDLE_KEY_TABLENAME);
        ArrayList<String> queryColumns=bundle.getStringArrayList(MessageType.BUNDLE_KEY_QUERYCOLUMNS);
        ArrayList<String> keys=bundle.getStringArrayList(MessageType.BUNDLE_KEY_SELECTIONKEYS);
        ArrayList<String> values=bundle.getStringArrayList(MessageType.BUNDLE_KEY_SELECTIONSVALUES);

        sendMessage.put(NetMessage.MESSAGE_TYPE,NetMessage.MESSAGE_TYPE_QUERY);
        sendMessage.put(NetMessage.TABLE_NAME,tableName);

        //将queryColumns转变成jsonArray格式;
        JSONArray array=new JSONArray();
        for(String element:queryColumns){
            array.put(element);
        }
        sendMessage.put(NetMessage.QUERYCOLUMNS,array);
        JSONObject json=new JSONObject();
        try{
            for(int i=0;i<keys.size();++i){
                json.put(keys.get(i),values.get(i));
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        sendMessage.put(NetMessage.SELECTIONS,json);
        sendNetMessage(sendMessage);
        Log.v("Note","Send Message:"+sendMessage);

        String reciveMessageStr=reciveNetMessage();
        Log.v("Note","Recive NetMessage:"+reciveMessageStr);

        List<Map<String,String>> results=new ArrayList<Map<String,String>>();
        try{
            NetMessage reciveMessage=new NetMessage(reciveMessageStr);
            if(!reciveMessage.getAnsMessageType().equals(NetMessage.ANSMESSAGE_TYPE_QUERYRESULTS)){
                Log.v("Note","Too busy For Request");
                return results;
            }
            results=reciveMessage.getQueryResultsFromJSONArray(NetMessage.QUERY_RESULTS);
        }catch (Exception e){
            e.printStackTrace();
        }
        return results;
    }
}
