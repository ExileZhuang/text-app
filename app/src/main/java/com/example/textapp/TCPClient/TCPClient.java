package com.example.textapp.TCPClient;

import com.example.textapp.entity.MainHandler;
import com.example.textapp.entity.ThreadHandler;

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
        reciveHandler =new MainHandler();
        thread=new TCPClientThread(reciveHandler);
        thread.start();
        sendHandler=thread.getReciveHandler();
    }
}
