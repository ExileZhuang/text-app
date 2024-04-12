package com.example.textapp.entity;

import java.util.Map;

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

    public Login_Info(Map<String,String> map){
        for(String key:map.keySet()){
            if(key.equals(User_Info.USER_ID)){
                user_id=map.get(key);
            }
            else if(key.equals(DEVICE)){
                device=map.get(key);
            }
            else if(key.equals(TIME)){
                time=map.get(TIME);
            }
            else if(key.equals(INFO_ID)){
                info_id=Integer.parseInt(map.get(key));
            }
            else{
                continue;
            }
        }
    }

    public Login_Info(){
        user_id=null;
    }
}
