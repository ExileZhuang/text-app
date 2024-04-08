package com.example.textapp.entity;

public class Login_Info {

    public static final String TABLE_LOGIN="login_info";

    public static final String INFO_ID="info_id";

    public static final String TIME="time";

    public static final String DEVICE="device";

    public String user_id;
    public int info_id;
    public String time;
    public String device;

    public Login_Info(String _user_id,int _info_id,String _time,String _device){
        user_id=_user_id;
        info_id=_info_id;
        time=_time;
        device=_device;
    }

    public Login_Info(){
        user_id=null;
    }
}