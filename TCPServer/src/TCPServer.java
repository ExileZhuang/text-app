import java.net.*;

public class TCPServer {
    //定义服务器不断接受消息;
    //每次收到请求都开新进程对socket进行处理;
    //ip:192.168.56.1
    //端口:8080

    private static final int ServerPort=8080;
    public static void main(String[] args) throws Exception {
        ServerSocket server=new ServerSocket(ServerPort);
        while(true){
            try{
                Socket socket=server.accept();
                new Thread(new ServerThread(socket)).start();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
