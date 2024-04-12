package com.example.textapp.TCPClient;

import android.os.Bundle;
import android.os.Handler;
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

    public static final String SOCKET_THREAD_NAME="SocketThread";
    private Handler sendHandler;
    //主线程中sendHandler即子线程中的receiveHandler,发送消息给子线程;

    private TCPClientThread thread;


    //存储用于query消息接收的queue,用于主线程堵塞运行;
    // 其余不接受消息从子线程返回的消息,发送失败或成功的处理在子线程中完成;
     private Queue<Message> queryQueue;

    //Attention:如果服务器未开启会形成阻塞,app无法运行;
    public TCPClient(){

        //要求服务器打开，不然会堵塞;
        //定义handler线程处理;
        thread=new TCPClientThread(SOCKET_THREAD_NAME);
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
                        //将信息加入对应messageQueue中;
                        queryQueue.add(message1);
                        break;
                    case MessageType.WHAT_INSERT:
                        int cnt1 = 0;
                        while ((!thread.insertValuesToServer(msg)) && cnt1 < 5) {
                            ++cnt1;
                            //发送失败最多五次则跳出;
                        }
                        break;
                    case MessageType.WHAT_UPDATE:
                        int cnt2=0;
                        while((!thread.updateValuesToServer(msg))&&cnt2<5){
                            ++cnt2;
                        }
                        break;
                    case MessageType.WHAT_DELETE:
                        break;
                    default:
                        break;
                }
            }
        };
        thread.setReceiveHandler(sendHandler);

        //初始化各个queue;
        queryQueue=new LinkedList<>();
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
            ArrayList<String> result=new ArrayList<>();
            for(String key:map.keySet()){
                if(i==0) {
                    queryColumns.add(key);
                }
                result.add(map.get(key));
            }
            if(i==0){
                bundle.putStringArrayList(MessageType.BUNDLE_KEY_QUERYCOLUMNS,queryColumns);
            }
            bundle.putStringArrayList(MessageType.BUNDLE_KEY_RESULTINDEX+i,result);
        }
        return bundle;
    }


    //将bundle解析成为List<map<String,String>>即查询结果;
    public List<Map<String,String>> BundleToListMap(Bundle bundle){
        List<Map<String,String>> results=new ArrayList<>();
        int count=bundle.getInt(MessageType.BUNDLE_KEY_RESULTSCOUNT);
        ArrayList<String> columns=bundle.getStringArrayList(MessageType.BUNDLE_KEY_QUERYCOLUMNS);
        for(int i=0;i<count;++i){
            String index=MessageType.BUNDLE_KEY_RESULTINDEX+ i;
            ArrayList<String> values=bundle.getStringArrayList(index);
            Map<String,String> map=new HashMap<>();
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

        Bundle selectionBundle=new Bundle();
        for(String key:selections.keySet()){
            selectionBundle.putString(key,selections.get(key));
        }

        bundle.putBundle(MessageType.BUNDLE_KEY_SELECTIONS,selectionBundle);

        msg.setData(bundle);

        sendHandler.sendMessage(msg);
        while(queryQueue.isEmpty()){
            //当queryQueue为空说明尚未开始,因此阻塞等待;
        }
        Message rcvMessage=queryQueue.poll();

        //将查询结果以Bundle形式返回;
        Bundle dataBundle=rcvMessage.getData();
        //将bundle重新解析成List<Map,Map>;
        List<Map<String,String>> results=BundleToListMap(dataBundle);
        return results;
    }

    public void insertNewValuesToServerTable(String tableName,Map<String,String> Values){
        Message sndMessage=new Message();
        sndMessage.what=MessageType.WHAT_INSERT;
        Bundle bundle=new Bundle();
        bundle.putString(MessageType.BUNDLE_KEY_TABLENAME,tableName);

        Bundle valueBundle=new Bundle();
        for(String key:Values.keySet()){
            valueBundle.putString(key,Values.get(key));
        }
        bundle.putBundle(MessageType.BUNDLE_KEY_VALUES,valueBundle);


        sndMessage.setData(bundle);

        sendHandler.sendMessage(sndMessage);
        //非堵塞运行,发送完插入消息后即不管子线程处理，继续在主线程运行;
    }

    //bundle带有values和selections的bundle传递给子线程(new);
    public void updateValuesToServerTable(String tableName,Map<String,String> values,Map<String,String> selections){
        Message sndMessage=new Message();
        sndMessage.what=MessageType.WHAT_UPDATE;
        Bundle bundle=new Bundle();
        bundle.putString(MessageType.BUNDLE_KEY_TABLENAME,tableName);

        Bundle valueBundle=new Bundle();
        for(String key:values.keySet()){
            valueBundle.putString(key,values.get(key));
        }

        Bundle selectionBundle=new Bundle();
        for(String key:selections.keySet()){
            selectionBundle.putString(key,selections.get(key));
        }

        bundle.putBundle(MessageType.BUNDLE_KEY_VALUES,valueBundle);
        bundle.putBundle(MessageType.BUNDLE_KEY_SELECTIONS,selectionBundle);

        sndMessage.setData(bundle);

        sendHandler.sendMessage(sndMessage);
    }
}
