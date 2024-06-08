package com.example.textapp.activity;

import android.app.Activity;
import android.os.Bundle;

import com.example.textapp.R;
import com.example.textapp.clientSocketThread.ClientSocketThread;
import com.example.textapp.clientSocketThread.ClientSocketTools;

import java.io.IOException;

public class HardwareControlActivity extends Activity {
    private ClientSocketThread clientSocketThread;

    private static final String CMD_OPEN_LEAD2="ioctl -d /dev/ledtest 1 1";

    private static final String CMD_OPEN_LEAD4="ioctl -d /dev/ledtest 1 3";

    private static final String CMD_READY_ZIGBEE="ioctl -d /dev/ledtest 0 4";

    private static final String CMD_OPEN_ZIGBEE="ioctl -d /dev/ledtest 1 4";

    private byte[] buffer={(byte)0xFE,(byte)0xE0,0x08,0x32,0x72,0x00,0x02,0x0A};

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hardwarecontrol);

        try{
            Runtime.getRuntime().exec(CMD_OPEN_LEAD2); //LED2
            Runtime.getRuntime().exec(CMD_OPEN_LEAD4); //LED4
            Runtime.getRuntime().exec(CMD_READY_ZIGBEE); //蜂鸣器
            Runtime.getRuntime().exec(CMD_OPEN_ZIGBEE);
        }catch (IOException e){
            e.printStackTrace();
        }

        new Thread(new Runnable(){
            public void run(){
                clientSocketThread=ClientSocketThread.getClientSocket(ClientSocketTools.getLocalIpAddress(),ClientSocketTools.ServerPort);
            }
        }).start();
    }

    public void onDestroy(){
        clientSocketThread=null;
        super.onDestroy();
    }
}
