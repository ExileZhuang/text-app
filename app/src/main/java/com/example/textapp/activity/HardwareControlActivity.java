package com.example.textapp.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.textapp.R;
import com.example.textapp.clientSocketThread.ClientSocketThread;
import com.example.textapp.clientSocketThread.ClientSocketTools;

import java.io.IOException;

public class HardwareControlActivity extends Activity {
    private ClientSocketThread clientSocketThread;

    private static final String CMD_OPEN_LEAD1="ioctl -d /dev/ledtest 1 1";

    private static final String CMD_OPEN_LEAD2="ioctl -d /dev/ledtest 1 2";

    private static final String CMD_OPEN_LEAD3="ioctl -d /dev/ledtest 1 3";

    private static final String CMD_OPEN_LEAD0="ioctl -d /dev/ledtest 1 0";

    private static final String CMD_OPEN_ZIGBEE ="ioctl -d /dev/ledtest 0 4";

    private static final String CMD_CLOSE_ZIGBEE ="ioctl -d /dev/ledtest 1 4";

    private byte[] buffer={(byte)0xFE,(byte)0xE0,0x08,0x32,0x72,0x00,0x02,0x0A};

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hardwarecontrol);

        try{
            Runtime.getRuntime().exec(CMD_OPEN_LEAD0); //LED2
            Runtime.getRuntime().exec(CMD_OPEN_LEAD1); //LED2
            Runtime.getRuntime().exec(CMD_OPEN_LEAD2); //LED2
            Runtime.getRuntime().exec(CMD_OPEN_LEAD3); //LED4
            Runtime.getRuntime().exec(CMD_OPEN_ZIGBEE); //蜂鸣器
        }catch (IOException e){
            e.printStackTrace();
        }

        new Thread(new Runnable(){
            public void run(){
                clientSocketThread=ClientSocketThread.getClientSocket(ClientSocketTools.getLocalIpAddress(),ClientSocketTools.ServerPort);
            }
        }).start();

        Button button_light_green=findViewById(R.id.Button_Green_LED);
        button_light_green.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buffer[3]=0x44;
                buffer[6]=0x24;
                work();
            }
        });

        Button button_light_red=findViewById(R.id.Button_Red_LED);
        button_light_red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buffer[3]=0x44;
                buffer[6]=0x09;
                work();
            }
        });

        Button button_light_yellow=findViewById(R.id.Button_Yellow_Led);
        button_light_yellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buffer[3]=0x44;
                buffer[6]=0x12;
                work();
            }
        });

        final Button button_motor_clockwise=findViewById(R.id.Button_Clockwise);
        final Button button_motor_counterclockwise=findViewById(R.id.Button_CounterClockwise);
        final Button button_motor_stop=findViewById(R.id.Button_Stop);
        button_motor_clockwise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buffer[3]=0x32;
                buffer[6]=0x03;
                button_motor_stop.setEnabled(true);
                button_motor_counterclockwise.setEnabled(false);
                button_motor_clockwise.setEnabled(false);
                work();
            }
        });
        button_motor_counterclockwise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buffer[3]=0x32;
                buffer[6]=0x02;
                button_motor_counterclockwise.setEnabled(false);
                button_motor_stop.setEnabled(true);
                button_motor_clockwise.setEnabled(false);
                work();
            }
        });
        button_motor_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buffer[3]=0x32;
                buffer[6]=0x01;
                button_motor_stop.setEnabled(false);
                button_motor_clockwise.setEnabled(true);
                button_motor_counterclockwise.setEnabled(true);
                work();
            }
        });

        Button btn_lattice_start=findViewById(R.id.Button_Lattice_Start);
        Button btn_lattice_stop=findViewById(R.id.Button_Lattice_Stop);
        btn_lattice_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buffer[3]=0x12;
                buffer[6]=0x00;
                work();
            }
        });
        btn_lattice_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buffer[3]=0x13;
                buffer[6]=0x00;
                work();
            }
        });
    }

    public void work(){
        try{
            clientSocketThread.getOutputStream().write(buffer);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onDestroy(){
        clientSocketThread=null;
        try{
            Runtime.getRuntime().exec(CMD_CLOSE_ZIGBEE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        super.onDestroy();
    }
}
