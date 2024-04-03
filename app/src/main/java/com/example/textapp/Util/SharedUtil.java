package com.example.textapp.Util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedUtil {
    private static SharedUtil mUtil;
    private SharedPreferences mpref;

    public static String RESTOREDPASSWORD="RESTOREDPASSWORD";
    public static String USERID="USERID";
    public static String PASSWORD="PASSWORD";

    public static SharedUtil getInstance(Context context){
        if(mUtil==null){
            mUtil=new SharedUtil();
            mUtil.mpref=context.getSharedPreferences("user",Context.MODE_PRIVATE);
        }
        return mUtil;
    }

    public void writeString(String key,String value){
        SharedPreferences.Editor edt=mpref.edit();
        edt.putString(key, value);
        edt.apply();
    }

    public String readString(String key,String defaultString){
        return mpref.getString(key,defaultString);
    }

    //更新对应键的值,prevalue->value;
    public void updateString(String key,String value){
        String preValue=readString(key,null);
        if(preValue==null||!preValue.equals(value)){
            writeString(key,value);
        }
    }


    public void writeBoolean(String restorePassword, boolean b) {
        SharedPreferences.Editor edt=mpref.edit();
        edt.putBoolean(restorePassword,b);
        edt.apply();
    }

    public boolean readBoolean(String key,boolean defaultBoolean){
        return mpref.getBoolean(key,defaultBoolean);
    }
}
