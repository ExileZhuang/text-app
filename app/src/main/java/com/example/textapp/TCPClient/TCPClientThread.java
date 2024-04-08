package com.example.textapp.TCPClient;

import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.example.textapp.entity.MainHandler;
import com.example.textapp.entity.MessageType;
import com.example.textapp.entity.NetMessage;
import com.example.textapp.entity.ThreadHandler;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
            output.write(str);
            output.flush();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void sendNetMessage(NetMessage message){
        sendString(message.toString());
    }

    public void Test(){
        try{
            String content="hello,world\n";
            sendString(content);
            String ans;
            Log.v("Note","Send Messagge: "+content);
            while((ans=input.readLine())!=null){
                Log.v("Note","Recive Message: "+ans);
            }
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

        //Test();


        //不断轮询查看有无消息;
        Looper.loop();
    }

    public void QueryToServer(Message msg) {
        Bundle bundle=msg.getData();
        NetMessage sendMessage=new NetMessage();

        String tableName=bundle.getString(MessageType.BUNDLE_KEY_TABLENAME);
        ArrayList<String> queryColumns=bundle.getStringArrayList(MessageType.BUNDLE_KEY_QUERYCOLUMNS);
        ArrayList<String> keys=bundle.getStringArrayList(MessageType.BUNDLE_KEY_SELECTIONKEYS);
        ArrayList<String> values=bundle.getStringArrayList(MessageType.BUNDLE_KEY_SELECTIONSVALUES);

        sendMessage.put(NetMessage.MESSAGE_TYPE,NetMessage.MESSAGE_TYPE_QUERY);
        sendMessage.put(NetMessage.TABLE_NAME,tableName);
        sendMessage.put(NetMessage.QUERYCOLUMNS,queryColumns);
        Map<String,String> m=new HashMap<>();
        for(int i=0;i<keys.size();++i){
            m.put(keys.get(i),values.get(i));
        }
        sendMessage.put(NetMessage.SELECTIONS,m);
        Log.v("Note",sendMessage.toString());
        sendNetMessage(sendMessage);
    }
}
