package com.example.textapp.TCPClient;

import android.os.Looper;
import android.util.Log;

import com.example.textapp.entity.MainHandler;
import com.example.textapp.entity.MyMessage;
import com.example.textapp.entity.ThreadHandler;

import java.net.*;
import java.io.*;

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

    public void sendMyMessage(MyMessage message){
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
        reciveHandler=new ThreadHandler(Looper.myLooper());

        //Test();


        //不断轮询查看有无消息;
        Looper.loop();
    }
}
