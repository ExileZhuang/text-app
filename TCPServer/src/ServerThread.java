import java.net.*;
import java.util.*;
import java.io.*;
import org.json.*;


public class ServerThread implements Runnable{


    private Socket socket=null;
    private BufferedReader input=null;
    private SQLHelper mDBHelper;

    public ServerThread(Socket _socket) throws IOException{
        mDBHelper=new SQLHelper();
        socket=_socket;
        input=new BufferedReader(new InputStreamReader(socket.getInputStream(),"utf-8"));
    }

    private void sendString(String str){
        try{
            BufferedWriter output=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"utf-8"));
            output.write(str);
            output.flush();
            //output.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void sendMessage(NetMessage message){
        sendString(message.toString());
    }

    @Override
    public void run() {
        //Test();
        try{
            String content=null;
            while((content=input.readLine())!=null){
                //output.println(mHelper.Test());
                NetMessage message=new NetMessage(content);
                String type=message.getMessageType();
                switch(type){
                    case NetMessage.MESSAGE_TYPE_QUERY:
                        //处理查询的情况;
                        queryMessageProcess(message);
                        break;

                    case NetMessage.MESSAGE_TYPE_INSERT:
                        insertMessageProcess(message);
                        break;

                    case NetMessage.MESSAGE_TYPE_DELETE:
                        deleteMessageProcess(message);
                        break;

                    case NetMessage.MESSAGE_TYPE_UPDATE:
                        updateMessageProcess(message);
                        break;
                    
                    default:break;
                }
            }
            input.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    //测试服务端与客户端可以正常通信;
    private void Test(){
        try{
            String content=null;
            while((content=input.readLine())!=null){
                sendString(content+"\n");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return;
    }

    public void queryMessageProcess(NetMessage message) {
        //从message中获取查询列和查询条件;
        String tableName=message.getString(NetMessage.TABLE_NAME);
        List<String> queryColumns=message.getListString(NetMessage.QUERYCOLUMNS);
        Map<String,Object> selections=message.getMap(NetMessage.SELECTIONS);

        //执行查询及获得查询结果;
        NetMessage ansMessage=new NetMessage();
        ansMessage.put(NetMessage.ANSMESSAGE_TYPE,NetMessage.ANSMESSAGE_TYPE_QUERYRESULTS);
        try{
            List<Map<String,Object>> res=mDBHelper.query(tableName,queryColumns,selections);
            JSONArray array=new JSONArray();
            for(Map<String,Object> m:res){
                JSONObject json=new JSONObject();
                for(String key:m.keySet()){
                    Object val=m.get(key);
                    json.put(key,val);
                }
                array.put(json);
            }
            ansMessage.put(NetMessage.ANSMESSAGE_TYPE_QUERYRESULTS,array);
            sendMessage(ansMessage);
        }catch(Exception e){
            e.printStackTrace();
        }

        //对查询结果进行转化为message并重新发送回客户端;
    }

    public void insertMessageProcess(NetMessage message){
        //获取表名及其插入值;
        String tableName=message.getString(NetMessage.TABLE_NAME);
        Map<String,Object> map=message.getMap(NetMessage.VALUES);

        //定义返回结果;
        NetMessage ansMessage=new NetMessage();
        ansMessage.put(NetMessage.ANSMESSAGE_TYPE,NetMessage.ANSMESSAGE_TYPE_INSERT);
        try{
            boolean success=mDBHelper.insert(tableName,map);
            if(success){
                ansMessage.put(NetMessage.STATUS,NetMessage.STATUS_SUCCESS);
            }
            else{
                ansMessage.put(NetMessage.STATUS,NetMessage.STATUS_FAIL);
            }
            sendMessage(ansMessage);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void updateMessageProcess(NetMessage message){
        String tableName=message.getString(NetMessage.TABLE_NAME);
        Map<String,Object> data=message.getMap(NetMessage.VALUES);
        Map<String,Object> selections=message.getMap(NetMessage.SELECTIONS);

        NetMessage ansMessage=new NetMessage();
        ansMessage.put(NetMessage.ANSMESSAGE_TYPE,NetMessage.ANSMESSAGEE_TYPE_UPDATE);
        try{
            boolean success=mDBHelper.update(tableName,data,selections);
            if(success){
                ansMessage.put(NetMessage.STATUS,NetMessage.STATUS_SUCCESS);
            }
            else{
                ansMessage.put(NetMessage.STATUS,NetMessage.STATUS_FAIL);
            }
            sendMessage(ansMessage);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void deleteMessageProcess(NetMessage message) {
        String tableName=message.getString(NetMessage.TABLE_NAME);
        Map<String,Object> selections=message.getMap(NetMessage.SELECTIONS);

        NetMessage ansMessage=new NetMessage();
        ansMessage.put(NetMessage.ANSMESSAGE_TYPE,NetMessage.ANSMESSAGE_TYPE_DELETE);
        try{
            boolean success=mDBHelper.delete(tableName,selections);
            if(success){
                ansMessage.put(NetMessage.STATUS,NetMessage.STATUS_SUCCESS);
            }
            else{
                ansMessage.put(NetMessage.STATUS,NetMessage.STATUS_FAIL);
            }
            sendMessage(ansMessage);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
