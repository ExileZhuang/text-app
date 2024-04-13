import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TCPServer {
    //定义服务器不断接受消息;
    //每次收到请求都开新进程对socket进行处理;
    //ip:192.168.56.1
    //端口:8080

    private static final int ServerPort=8080;

    private Map<String,ServerThread> QRCodeIdMap=new HashMap<>();

    private int QRCodeId=1;
    //记录每次给的QRId每发一个自增一次;

    public String getQrCodeId(){
        String id=String.valueOf(QRCodeId);
        ++QRCodeId;
        return id;
    }

    public void pushServerThreadIntoMap(String QRCodeId,ServerThread thread){
        QRCodeIdMap.put(QRCodeId,thread);
    }

    public boolean ackQRCodeId(String QRCodeId,String UserId){
        if(QRCodeIdMap.containsKey(QRCodeId)){
            ServerThread thread=QRCodeIdMap.get(QRCodeId);
            thread.sendAuthorizationMessage(UserId);
            return true;
        }
        else{
            return false;
        }
    }
    

    


    public static void main(String[] args) throws Exception {
        ServerSocket server=new ServerSocket(ServerPort);
        TCPServer tcpServer=new TCPServer();
        while(true){
            try{
                Socket socket=server.accept();
                new Thread(new ServerThread(socket,tcpServer)).start();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
