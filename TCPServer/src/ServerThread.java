import java.net.*;
import java.util.*;
import java.io.*;
import org.json.*;


public class ServerThread implements Runnable{


    private Socket socket=null;
    private BufferedReader input=null;
    private SQLHelper mDBHelper;
    private TCPServer mServer;

    public ServerThread(Socket _socket,TCPServer _TcpServer) throws IOException{
        mDBHelper=new SQLHelper();
        socket=_socket;
        mServer=_TcpServer;
        input=new BufferedReader(new InputStreamReader(socket.getInputStream(),"utf-8"));
    }

    private void sendString(String str){
        try{
            BufferedWriter output=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"utf-8"));
            output.write(str+"\n");
            output.flush();
            //output.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void sendNetMessage(NetMessage message){
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

                    case NetMessage.MESSAGE_TYPE_GETQRCODEID:
                        GetQRCodeIdMessageProcess();
                        break;
                    
                    case NetMessage.MESSAGE_TYPE_ACKQRCODEID:
                        ackQRCodeIdMessageProcess(message);
                        break;

                    default:
                        break;
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
        //将json转为map;
        Map<String,String> selections=message.getMap(NetMessage.SELECTIONS);

        //执行查询及获得查询结果;
        NetMessage ansMessage=new NetMessage();
        ansMessage.put(NetMessage.ANSMESSAGE_TYPE,NetMessage.ANSMESSAGE_TYPE_QUERY);
        try{
            List<Map<String,String>> res=mDBHelper.query(tableName,queryColumns,selections);
            JSONArray array=new JSONArray();
            for(Map<String,String> m:res){
                JSONObject json=new JSONObject();
                for(String key:m.keySet()){
                    String val=m.get(key);
                    json.put(key,val);
                }
                array.put(json);
            }
            ansMessage.put(NetMessage.QUERY_RESULTS,array);
            sendNetMessage(ansMessage);
        }catch(Exception e){
            e.printStackTrace();
        }

        //对查询结果进行转化为message并重新发送回客户端;
    }

    public void insertMessageProcess(NetMessage message){
        //获取表名及其插入值;
        String tableName=message.getString(NetMessage.TABLE_NAME);
        Map<String,String> map=message.getMap(NetMessage.VALUES);

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
            sendNetMessage(ansMessage);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void updateMessageProcess(NetMessage message){
        String tableName=message.getString(NetMessage.TABLE_NAME);
        Map<String,String> data=message.getMap(NetMessage.VALUES);
        Map<String,String> selections=message.getMap(NetMessage.SELECTIONS);

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
            sendNetMessage(ansMessage);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void deleteMessageProcess(NetMessage message) {
        String tableName=message.getString(NetMessage.TABLE_NAME);
        Map<String,String> selections=message.getMap(NetMessage.SELECTIONS);

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
            sendNetMessage(ansMessage);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void GetQRCodeIdMessageProcess(){
        String qrCodeId=mServer.getQrCodeId();

        //将qrcodeId及对应线程thread放入Server端的哈希表中;
        mServer.pushServerThreadIntoMap(qrCodeId, this);

        NetMessage ansMessage=new NetMessage();
        ansMessage.put(NetMessage.ANSMESSAGE_TYPE,NetMessage.ANSMESSAGE_TYPE_GETQRCODEID);
        ansMessage.put(NetMessage.QRCODEID,qrCodeId);
        try{
            sendNetMessage(ansMessage);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //对上传上来的QRCodeId进行确认现在是否存在仍在进行通信的线程;
    //有则让该线程对对应客户端发送信息进行登录;
    //同时对本线程的远程客户端做出响应：表示登录成功与否;
    public void ackQRCodeIdMessageProcess(NetMessage msg){
        String qrcodeId=msg.getString(NetMessage.QRCODEID);
        String userId=msg.getString(NetMessage.USERID);
        boolean flag=mServer.ackQRCodeId(qrcodeId,userId);

        //返回给扫码登录设备表示是否可以对另一台机器扫码登录成功;
        NetMessage rpdMessage=new NetMessage();
        rpdMessage.put(NetMessage.ANSMESSAGE_TYPE,NetMessage.ANSMESSAGE_TYPE_ACKQRCODEID);
        if(flag){
            rpdMessage.put(NetMessage.STATUS,NetMessage.STATUS_SUCCESS);
        }
        else{
            rpdMessage.put(NetMessage.STATUS,NetMessage.STATUS_FAIL);
        }

        try{
            sendNetMessage(rpdMessage);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void sendAuthorizationMessage(String UserId){
        NetMessage sndMessage=new NetMessage();
        sndMessage.put(NetMessage.ANSMESSAGE_TYPE,NetMessage.ANSMESSAGE_TYPE_AUTHORIZATION);
        sndMessage.put(NetMessage.USERID,UserId);
        try{
            sendNetMessage(sndMessage);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
