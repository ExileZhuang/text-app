package com.example.textapp.TCPClient;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.textapp.entity.MessageType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

//用于实现TCPClientThread与Application UI等主线程之间的接口;
//封装handler实现两者间通信;
public class TCPClient {

    private Handler receiveHandler;
    //主线程中reciveHandler即子线程中的sendHandler,接受由子线程发送的消息;

    private Handler sendHandler;
    //主线程中sendhandler即子线程中的reciveHandler,发送消息给子线程;

    private TCPClientThread thread;


    //存储用于query消息接收的queue;以下同理;
    private Queue<Message> queryQueue;

    private Queue<Message> insertQueue;

    private Queue<Message> deleteQueue;

    private Queue<Message> updateQueue;

    //Attention:如果服务器未开启会形成阻塞,app无法运行;
    public TCPClient(){
        //获得receiveHandler并处理;
        receiveHandler=new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                pushMessageIntoQueue(msg);
            }
        };

        //要求服务器打开，不然会堵塞;
        //定义handler线程处理;
        thread=new TCPClientThread(receiveHandler,"SocketThread");
        thread.start();


        //实现sendHandler;
        //子线程对于收到信息的处理;
        sendHandler=new Handler(thread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                //对各种Message进行处理:
                Log.v("Note", "SocketThread Get Message:" + msg.what);
                switch (msg.what) {
                    case MessageType.WHAT_QUERY:
                        //sendTestMessageToMainThread();
                        //获取查询结果;
                        List<Map<String, String>> results = thread.QueryToServer(msg);
                        //对于查询结果通过message携带bundle返回给主线程;
                        //bundle由特定函数将results转化为bundle;
                        Message message1 = new Message();
                        message1.what = MessageType.WHAT_QUERY;
                        Bundle bundle = ListMapToBundle(results);
                        message1.setData(bundle);

                        Log.v("Note", "Send To MainThread:" + message1.what);

                        //发送消息给主线程handler告知结果;
                        thread.sendMessageToMainThread(message1);
                        break;
                    case MessageType.WHAT_INSERT:
                        int cnt = 0;
                        while ((!thread.insertValuesToServer(msg)) && cnt < 5) {
                            cnt++;
                            //发送失败最多五次则跳出;
                        }
                        Message message2 = new Message();
                        message2.what = MessageType.WHAT_INSERT;
                        thread.sendMessageToMainThread(message2);
                        break;
                    case MessageType.WHAT_DELETE:
                        break;
                    case MessageType.WHAT_UPDATE:
                        break;
                    default:
                        break;
                }
            }
        };
        thread.setReceiveHandler(sendHandler);

        //初始化各个queue;
        queryQueue=new LinkedList<Message>();
        insertQueue=new LinkedList<Message>();
        updateQueue=new LinkedList<Message>();
        deleteQueue=new LinkedList<Message>();
    }


    //将查询结果list<Map<String,String>>变为bundle形式;
    //bundle的格式:"ResultsCount":Cnt;
    //              "QueryColumns:[columns1,columns2]
    //              "result0":[String,String...]
    //              "result1":[String,String...]
    public Bundle ListMapToBundle(List<Map<String,String>> lmap){
        Bundle bundle=new Bundle();
        int count=lmap.size();
        bundle.putInt(MessageType.BUNDLE_KEY_RESULTSCOUNT,count);
        ArrayList<String> queryColumns=new ArrayList<>();
        for(int i=0;i<count;++i){
            Map<String,String> map=lmap.get(i);
            ArrayList<String> result=new ArrayList<String>();
            for(String key:map.keySet()){
                if(i==0) {
                    queryColumns.add(key);
                }
                result.add(map.get(key));
            }
            if(i==0){
                bundle.putStringArrayList(MessageType.BUNDLE_KEY_QUERYCOLUMNS,queryColumns);
            }
            bundle.putStringArrayList(MessageType.BUNDLE_KEY_RESULTINDEX+String.valueOf(i),result);
        }
        return bundle;
    }


    //将指定message加入队列中并使发送消息给子线程的函数阻塞等待结果;
    public void pushMessageIntoQueue(Message msg){
        Log.v("Note","Push Message");
        switch(msg.what){
            case MessageType.WHAT_QUERY:
                Log.v("Note","QueryQueue Get Message");
                queryQueue.offer(msg);
                break;
            case MessageType.WHAT_INSERT:
                insertQueue.offer(msg);
                break;
            case MessageType.WHAT_DELETE:
                deleteQueue.offer(msg);
                break;
            case MessageType.WHAT_UPDATE:
                updateQueue.offer(msg);
                break;
            default:break;
        }
    }

    //将bundle解析成为List<map<String,String>>即查询结果;
    public List<Map<String,String>> BundleToListMap(Bundle bundle){
        List<Map<String,String>> results=new ArrayList<Map<String,String>>();
        int count=bundle.getInt(MessageType.BUNDLE_KEY_RESULTSCOUNT);
        ArrayList<String> columns=bundle.getStringArrayList(MessageType.BUNDLE_KEY_QUERYCOLUMNS);
        for(int i=0;i<count;++i){
            String index=MessageType.BUNDLE_KEY_RESULTINDEX+String.valueOf(i);
            ArrayList<String> values=bundle.getStringArrayList(index);
            Map<String,String> map=new HashMap<String,String>();
            for(int j=0;j<values.size();++j){
                map.put(columns.get(j),values.get(j));
            }
            results.add(map);
        }
        return results;
    }

    //bundle中数据:tableName,queryColumns,(keys,values)=>selections
    //发送给子线程让子线程向服务器发起查询并返回查询结果;
    public List<Map<String,String>> sendQueryColumnsBySelectionsToTable(String TableName, ArrayList<String> queryColumns, Map<String,String> selections){
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
        //Log.v("Note","Waiting For Result");
        while(queryQueue.isEmpty()){
            //当queryQueue为空说明尚未开始,因此阻塞等待;
        }
        //Log.v("Note","Get Result And Continue");
        Message rcvMessage=queryQueue.poll();

        //将查询结果以Bundle形式返回;
        Bundle dataBundle=rcvMessage.getData();
        //将bundle重新解析成List<Map,Map>;
        List<Map<String,String>> results=BundleToListMap(dataBundle);
        return results;
    }

    public void inserNewValuesToTable(String tableName,Map<String,String> Values){
        Message sndMessage=new Message();
        sndMessage.what=MessageType.WHAT_INSERT;
        Bundle bundle=new Bundle();
        bundle.putString(MessageType.BUNDLE_KEY_TABLENAME,tableName);

        //将values转变为bundle形式并发送;
        ArrayList<String> keys=new ArrayList<String>();
        ArrayList<String> values=new ArrayList<String>();
        for(String key:Values.keySet()){
            keys.add(key);
            values.add(Values.get(key));
        }
        bundle.putStringArrayList(MessageType.BUNDLE_KEY_KEYS,keys);
        bundle.putStringArrayList(MessageType.BUNDLE_KEY_VALUES,values);
        sndMessage.setData(bundle);

        sendHandler.sendMessage(sndMessage);
        while(insertQueue.isEmpty()){
            //等待插入结果讯息;
        }
        //获取返回讯息;
        Message rcvMessage=insertQueue.poll();
    }
}
