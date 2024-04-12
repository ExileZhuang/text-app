package com.example.textapp.TCPClient;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.example.textapp.entity.MessageType;
import com.example.textapp.entity.NetMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TCPClientThread extends HandlerThread {

    public static final String SERVER_IP="192.168.56.1";

    public static final int SERVER_PORT=8080;

    private BufferedReader input;

    private Socket socket;

    private Handler receiveHandler;
    //接受主线程消息的handler;

    public TCPClientThread(String ThreadName){
        super(ThreadName);
        socket=null;
        input=null;
        receiveHandler=null;
    }

    public void setReceiveHandler(Handler handler){
        receiveHandler=handler;
    }

    public void sendStringToServer(String str){
        try{
            BufferedWriter output=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"utf-8"));
            output.write(str+"\n");
            output.flush();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void sendNetMessageToServer(NetMessage message){
        sendStringToServer(message.toString());
    }

    //从服务器接收一条信息;
    public String receiveNetMessage(){
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
            sendStringToServer(content);
            Log.v("Note","Send Message: "+content);
            String ans;
            while((ans=input.readLine())!=null){
                break;
            }
            Log.v("Note","Receive Message:"+ans);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        try{
            socket=new Socket(SERVER_IP,SERVER_PORT);
            //Log.v("Note","Connect Status:"+String.valueOf(socket.isConnected()));
            input=new BufferedReader(new InputStreamReader(socket.getInputStream(),"utf-8"));
        }catch (Exception e){
            e.printStackTrace();
        }
        super.run();
    }


    //向服务器通过发起查询并且获得结果;
    public List<Map<String,String>> QueryToServer(Message msg) {
        Bundle bundle=msg.getData();

        NetMessage sendMessage=new NetMessage();

        String tableName=bundle.getString(MessageType.BUNDLE_KEY_TABLENAME);
        ArrayList<String> queryColumns=bundle.getStringArrayList(MessageType.BUNDLE_KEY_QUERYCOLUMNS);
        Bundle selectionBundle=bundle.getBundle(MessageType.BUNDLE_KEY_SELECTIONS);

        sendMessage.put(NetMessage.MESSAGE_TYPE,NetMessage.MESSAGE_TYPE_QUERY);
        sendMessage.put(NetMessage.TABLE_NAME,tableName);

        //将queryColumns转变成jsonArray格式;
        JSONArray array=new JSONArray();
        for(String element:queryColumns){
            array.put(element);
        }
        sendMessage.put(NetMessage.QUERYCOLUMNS,array);

        JSONObject selectionJson=new JSONObject();
        try{
            for(String key:selectionBundle.keySet()){
                selectionJson.put(key,selectionBundle.getString(key));
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        sendMessage.put(NetMessage.SELECTIONS,selectionJson);

        sendNetMessageToServer(sendMessage);
        Log.v("Note","Send Message:"+sendMessage);

        String receiveMessageStr= receiveNetMessage();
        Log.v("Note","Receive NetMessage:"+receiveMessageStr);

        List<Map<String,String>> results=new ArrayList<Map<String,String>>();
        try{
            NetMessage receiveMessage=new NetMessage(receiveMessageStr);
            if(!receiveMessage.getAnsMessageType().equals(NetMessage.ANSMESSAGE_TYPE_QUERY)){
                Log.v("Note","Too busy For Request");
                return results;
            }
            results=receiveMessage.getQueryResultsFromJSONArray(NetMessage.QUERY_RESULTS);
        }catch (Exception e){
            e.printStackTrace();
        }
        return results;
    }

    public boolean insertValuesToServer(Message msg){
        Bundle bundle=msg.getData();

        Bundle values=bundle.getBundle(MessageType.BUNDLE_KEY_VALUES);
        String tableName=bundle.getString(MessageType.BUNDLE_KEY_TABLENAME);

        NetMessage sendMessage=new NetMessage();

        sendMessage.put(NetMessage.MESSAGE_TYPE,NetMessage.MESSAGE_TYPE_INSERT);
        sendMessage.put(NetMessage.TABLE_NAME,tableName);

        JSONObject json=new JSONObject();

        try{
            for(String key:values.keySet()){
                json.put(key,values.get(key));
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        sendMessage.put(NetMessage.VALUES,json);

        sendNetMessageToServer(sendMessage);
        Log.v("Note","Send Message:"+sendMessage);

        String rcvMessageStr= receiveNetMessage();
        Log.v("Note","Receive Message:"+rcvMessageStr);

        NetMessage rcvMessage=new NetMessage(rcvMessageStr);
        if(!rcvMessage.getAnsMessageType().equals(NetMessage.ANSMESSAGE_TYPE_INSERT)){
            Log.v("Note","Too Busy For Request");
            return false;
        }
        String status=rcvMessage.getString(NetMessage.STATUS);
        //Log.v("Note",status);
        return status.equals(NetMessage.STATUS_SUCCESS);
    }

    public boolean updateValuesToServer(Message msg){
        NetMessage sndMessage=new NetMessage();
        sndMessage.put(NetMessage.MESSAGE_TYPE,NetMessage.MESSAGE_TYPE_UPDATE);

        Bundle bundle=msg.getData();
        Bundle valueBundle=bundle.getBundle(MessageType.BUNDLE_KEY_VALUES);
        Bundle selectionBundle=bundle.getBundle(MessageType.BUNDLE_KEY_SELECTIONS);

        sndMessage.put(NetMessage.TABLE_NAME,bundle.getString(MessageType.BUNDLE_KEY_TABLENAME));

        JSONObject valueJson=new JSONObject();
        JSONObject selectionJson=new JSONObject();
        try{
            for(String key:valueBundle.keySet()){
                valueJson.put(key,valueBundle.getString(key));
            }
            for(String key:selectionBundle.keySet()){
                selectionJson.put(key,selectionBundle.getString(key));
            }
        }catch (JSONException e){
            e.printStackTrace();
        }

        sndMessage.put(NetMessage.VALUES,valueJson);
        sndMessage.put(NetMessage.SELECTIONS,selectionJson);

        sendNetMessageToServer(sndMessage);
        Log.v("Note","Send Message:"+sndMessage);

        String rcvMessageStr= receiveNetMessage();
        Log.v("Note","Receive Message:"+rcvMessageStr);

        NetMessage rcvMessage=new NetMessage(rcvMessageStr);
        if(!rcvMessage.getAnsMessageType().equals(NetMessage.ANSMESSAGE_TYPE_UPDATE)){
            Log.v("Note","Too Busy For Request");
            return false;
        }
        String status=rcvMessage.getString(NetMessage.STATUS);
        return status.equals(NetMessage.STATUS_SUCCESS);
    }
}
