package com.example.textapp.TCPClient;

import android.util.Log;

import com.example.textapp.entity.MyMessage;

import java.net.*;
import java.io.*;

public class TCPClient{

    public static final String SERVER_IP="192.168.56.1";

    public static final int SERVER_PORT=8080;

    private BufferedReader input=null;

    private Socket socket;

    public TCPClient(){
        try{
            socket=new Socket(SERVER_IP,SERVER_PORT);
            //Log.v("Note","Connect Status"+String.valueOf(socket.isConnected()));
            input=new BufferedReader(new InputStreamReader(socket.getInputStream(),"utf-8"));
        }catch (Exception e){
            e.printStackTrace();
        }
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

    public void sendMyMessage(MyMessage message){
        sendString(message.toString());
    }

    public void Test(){
        try{
            String content="hello,world\n";
            sendString(content);
            String ans;
            while((ans=input.readLine())!=null){
                Log.v("Note","Recive Message:"+ans);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
