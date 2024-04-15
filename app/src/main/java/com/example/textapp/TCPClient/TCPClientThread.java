package com.example.textapp.TCPClient;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.example.textapp.entity.MessageType;
import com.example.textapp.entity.NetMessage;
import com.example.textapp.entity.User_Info;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class TCPClientThread extends HandlerThread {

    public static final String SERVER_IP="192.168.56.1";

    public static final int SERVER_PORT=8080;

    private BufferedReader input=null;

    private Socket socket=null;

    //为了使用qrcode对客户端与服务器之间的收发机制进行更改;
    //信息在发送后进行堵塞等待回传消息(之前也是)并放入queue中，发送后等待queue中出现对应消息并获取;
    private final Queue<NetMessage> OrderedNetMessageQueue=new LinkedList<>();

    private Handler mainThreadHandler=null;

    public TCPClientThread(String ThreadName){
        super(ThreadName);
    }

    //终止线程中所有任务;
    public void closeThread(){
        quitSafely();
        try{
            BufferedWriter output=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"utf-8"));
            output.close();
            input.close();
            socket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    //获得主线程的handler用于处理服务器通知性质的消息,从而直接更改主线程的UI等方面;
    public void setMainThreadHandler(Handler handler){
        mainThreadHandler=handler;
    }

    //给主线程发送消息;
    public void sendMessageToMainThread(final Message msg){
        new Thread(new Runnable() {
            @Override
            public void run() {
                mainThreadHandler.sendMessage(msg);
            }
        }).start();
    }

    //向服务器发送字符串;
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
        Log.v("Note","Send Message:"+message);
    }

    //NO USE;
    //暂时废止使用;
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
        Log.v("Note","Receive NetMessage:"+msg);
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
        new Thread(new Runnable() {

            //另开子线程与服务器进行连接,并监听服务器发送的消息;
            @Override
            public void run() {
                try{
                    socket=new Socket(SERVER_IP,SERVER_PORT);
                    //Log.v("Note","Connect Status:"+String.valueOf(socket.isConnected()));
                    input=new BufferedReader(new InputStreamReader(socket.getInputStream(),"utf-8"));
                }catch (Exception e){
                    e.printStackTrace();
                }

                String msgStr=null;
                try{
                    while((msgStr= input.readLine())!=null){
                        Log.v("Note","Receive NetMessage:"+msgStr);
                        NetMessage rcvMessage=new NetMessage(msgStr);
                        //堵塞式等待服务器返回的消息,然后放入OrderedNetMessageQueue,等待被人获取;
                        //遇到通知性服务器消息,对不同类别的消息进行分类处理,发送消息给主线程通知该类消息;
                        String msgType=rcvMessage.getAnsMessageType();
                        switch (msgType){
                            case NetMessage.ANSMESSAGE_TYPE_QUERY:
                            case NetMessage.ANSMESSAGE_TYPE_INSERT:
                            case NetMessage.ANSMESSAGE_TYPE_UPDATE:
                            case NetMessage.ANSMESSAGE_TYPE_DELETE:
                            case NetMessage.ANSMESSAGE_TYPE_GETQRCODEID:
                            case NetMessage.ANSMESSAGE_TYPE_ACKQRCODEID:
                                OrderedNetMessageQueue.offer(rcvMessage);
                                break;
                            case NetMessage.ANSMESSAGE_TYPE_AUTHORIZATION:
                                //处理 NetMessage，将参数带入Message后发送Message到主线程;
                                Message message=new Message();
                                message.what=MessageType.WHAT_QRCODEID_AUTHORIZE;
                                Bundle bundle=new Bundle();
                                bundle.putString(MessageType.BUNDLE_KEY_USERID,rcvMessage.getString(NetMessage.USERID));
                                message.setData(bundle);
                                sendMessageToMainThread(message);
                                break;
                            default:
                                break;
                        }
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
        super.run();
    }


    //向服务器通过发起查询并且获得结果;
    public List<Map<String,String>> QueryToServerDatabase(Message msg) {
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

        while(OrderedNetMessageQueue.isEmpty()){
            //阻塞等待服务器回传消息;
        }

        NetMessage rcvMessage=OrderedNetMessageQueue.poll();

        List<Map<String,String>> results=new ArrayList<>();
        try{
            if(!rcvMessage.getAnsMessageType().equals(NetMessage.ANSMESSAGE_TYPE_QUERY)){
                Log.v("Note","Too busy For Request");
                return results;
            }
            results=rcvMessage.getQueryResultsFromJSONArray(NetMessage.QUERY_RESULTS);
        }catch (Exception e){
            e.printStackTrace();
        }
        return results;
    }

    public boolean insertValuesToServerDataBase(Message msg){
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

        while(OrderedNetMessageQueue.isEmpty())
        {
            //阻塞等待服务器传回消息;
        }
        NetMessage rcvMessage=OrderedNetMessageQueue.poll();
        if(!rcvMessage.getAnsMessageType().equals(NetMessage.ANSMESSAGE_TYPE_INSERT)){
            Log.v("Note","Too Busy For Request");
            return false;
        }
        String status=rcvMessage.getString(NetMessage.STATUS);
        //Log.v("Note",status);
        return status.equals(NetMessage.STATUS_SUCCESS);
    }

    public boolean updateValuesToServerDatabase(Message msg){
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

        while(OrderedNetMessageQueue.isEmpty()){
            //阻塞等待服务器获得消息;
        }
        NetMessage rcvMessage=OrderedNetMessageQueue.poll();

        if(!rcvMessage.getAnsMessageType().equals(NetMessage.ANSMESSAGE_TYPE_UPDATE)){
            Log.v("Note","Too Busy For Request");
            return false;
        }
        String status=rcvMessage.getString(NetMessage.STATUS);
        return status.equals(NetMessage.STATUS_SUCCESS);
    }

    public String getQRCodeIdFromServer(){
        NetMessage sndMessage=new NetMessage();
        sndMessage.put(NetMessage.MESSAGE_TYPE,NetMessage.MESSAGE_TYPE_GETQRCODEID);

        sendNetMessageToServer(sndMessage);

        while(OrderedNetMessageQueue.isEmpty()){
            //堵塞等待服务器返回消息;
        }

        NetMessage rcvMessage=OrderedNetMessageQueue.poll();
        if(!rcvMessage.getAnsMessageType().equals(NetMessage.ANSMESSAGE_TYPE_GETQRCODEID)){
            Log.v("Note","Too Busy For Request");
            return null;
        }
        return rcvMessage.getString(NetMessage.QRCODEID);
    }

    //向服务器发送ack信息，携带当前user_id信息，当有匹配时返回200,失败返回500;
    public boolean ackQRCodeIdToServer(Message msg){
        Bundle bundle=msg.getData();
        String qrcodeId=bundle.getString(MessageType.BUNDLE_KEY_QRCODEID);
        String userId=bundle.getString(User_Info.USER_ID);
        NetMessage sndMessage=new NetMessage();
        sndMessage.put(NetMessage.MESSAGE_TYPE,NetMessage.MESSAGE_TYPE_ACKQRCODEID);
        sndMessage.put(NetMessage.QRCODEID,qrcodeId);
        sndMessage.put(NetMessage.USERID,userId);

        sendNetMessageToServer(sndMessage);

        while(OrderedNetMessageQueue.isEmpty()){
            //阻塞等待服务器返回消息;
        }
        NetMessage rcvMessage=OrderedNetMessageQueue.poll();
        if(rcvMessage.getAnsMessageType().equals(NetMessage.ANSMESSAGE_TYPE_ACKQRCODEID)){
            Log.v("Note","Too Busy For Request");
            return false;
        }
        return rcvMessage.getString(NetMessage.STATUS).equals(NetMessage.STATUS_SUCCESS);
    }
}
