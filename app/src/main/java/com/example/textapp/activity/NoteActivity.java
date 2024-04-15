package com.example.textapp.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.textapp.MyApplication;
import com.example.textapp.R;
import com.example.textapp.TCPClient.TCPClient;
import com.example.textapp.database.NotesDBHelper;
import com.example.textapp.entity.Login_Info;
import com.example.textapp.entity.Note;
import com.example.textapp.entity.User_Info;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class NoteActivity extends AppCompatActivity {

    private NotesDBHelper mDBHelper;

    private String DEFAULT_USER_ID;

    private TCPClient mClient;

    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_note);

        mDBHelper=NotesDBHelper.getInstance(this);
        mDBHelper.openReadLink();
        mDBHelper.openWriteLink();

        //获取客户端;
        mClient= MyApplication.getInstance().getClient();

        //获取传入的userId;
        Intent intent=getIntent();
        DEFAULT_USER_ID=intent.getStringExtra(User_Info.USER_ID);

        //初始加载Note页面;
        loadNoteLayout();
    }

    //加载用户信息相关内容;
    private void loadUserInfoLayout() {
        View ViewUserInfo= LayoutInflater.from(this).inflate(R.layout.layout_user_info, null);
        LinearLayout layout=findViewById(R.id.linearLayout_content);
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        layout.removeAllViews();
        layout.addView(ViewUserInfo,params);

        //重新对layout_note_title进行设置;
        LinearLayout title_layout=findViewById(R.id.linearLayout_note_title);

        ImageView icon=title_layout.findViewById(R.id.image_user_icon);
        icon.setImageResource(R.drawable.text_icon);
        //icon设置点击回到layout_note;
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadNoteLayout();
            }
        });


        //获取各组件;
        TextView tv_userId=layout.findViewById(R.id.textView_user_info_UserId);
        tv_userId.setText(DEFAULT_USER_ID);

        EditText et_password=layout.findViewById(R.id.editText_user_info_password);
        EditText et_name=layout.findViewById(R.id.editText_user_info_name);
        EditText et_age=layout.findViewById(R.id.editText_user_info_age);
        RadioGroup rg_gender=layout.findViewById(R.id.radioGroup_user_info_gender);
        ListView lv_loginInfo=layout.findViewById(R.id.listView_user_info_loginInfo);

        //从库中查询(由于已经成功登录所以在本地库中一定会有对应信息)对应信息并展示;
        User_Info info=mDBHelper.queryUserInfoByUserId(DEFAULT_USER_ID);

        et_password.setText(info.password);
        et_name.setText(info.name);
        et_age.setText(String.valueOf(info.age));
        if(info.gender.equals(User_Info.GENDER_MALE)){
            RadioButton rb_male=rg_gender.findViewById(R.id.radioButton_user_info_male);
            rb_male.setChecked(true);
        }
        else{
            RadioButton rb_female=rg_gender.findViewById(R.id.radioButton_user_info_female);
            rb_female.setChecked(true);
        }

        //从库中找到该用户最近登录的五次设备;
        ArrayList<String> queryColumns=new ArrayList<>();
        queryColumns.add(Login_Info.DEVICE);
        Map<String,String> selection=new HashMap<>();
        selection.put(User_Info.USER_ID,DEFAULT_USER_ID);
        List<Map<String,String>> queryResults=mClient.sendQueryColumnsBySelectionsToServerTable(Login_Info.TABLE_LOGIN,queryColumns,selection);

        //从本地数据库中查找最近登录的五次结果;
        List<Login_Info> listLoginInfo=mDBHelper.queryLoginInfoByUserId(DEFAULT_USER_ID);
        if(listLoginInfo.size()>5){
            listLoginInfo=listLoginInfo.subList(0,5);
        }

        //找出服务器数据库中与本地数据库中设备不同的部分，然后将设备不同的部分分批次多次读取;
        Set<String> set1=new HashSet<>(),set2=new HashSet<>();
        for(Map<String,String> m:queryResults){
            set1.add(m.get(Login_Info.DEVICE));
        }
        for(Login_Info i:listLoginInfo){
            set2.add(i.device);
        }

        queryColumns.add(Login_Info.TIME);
        //当服务器与本地的设备型号有不同时;
        if(!set1.equals(set2)){
            for(String element:set1){
                if(set2.contains(element)){
                    set1.remove(element);
                }
            }
            ArrayList<String> recDevice = new ArrayList<>(set1);
            for(String device:recDevice){
                Map<String,String> tempSelection=selection;
                tempSelection.put(Login_Info.DEVICE,device);
                List<Map<String,String>> singleResults=mClient.sendQueryColumnsBySelectionsToServerTable(Login_Info.TABLE_LOGIN,
                        queryColumns,tempSelection);
                for(Map<String,String> m:singleResults){
                    Login_Info newInfo=new Login_Info(m);
                    listLoginInfo.add(newInfo);
                }
            }

            //对于全部设备的所有登录时间，进行筛选排序在最新五次登录;
            Collections.sort(listLoginInfo, new Comparator<Login_Info>() {
                @Override
                public int compare(Login_Info o1, Login_Info o2) {
                    SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                    Date date1=null,date2=null;
                    try {
                        date1=formatter.parse(o1.time);
                        date2=formatter.parse(o2.time);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if(date1!=null&&date2!=null){
                        if(date1.equals(date2)){
                            return 0;
                        }
                        else if(date1.after(date2)){
                            return 1;
                        }
                        else{
                            return -1;
                        }
                    }
                    else{
                        return 0;
                    }
                }
            });

            listLoginInfo=listLoginInfo.subList(0,5);
        }


        //本地数据需要与服务器数据进行合并并选取最新的五次;
        List<String> listData=new ArrayList<>();
        for(Login_Info lInfo:listLoginInfo){
            listData.add(lInfo.time+" "+lInfo.device);
        }
        ArrayAdapter<String> adapter=new ArrayAdapter<>(NoteActivity.this,
                android.R.layout.simple_list_item_1,listData);
        lv_loginInfo.setAdapter(adapter);

        //对于用户修改了的password等属性进行修改和保存;
        Button btn_save=findViewById(R.id.button_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_password=findViewById(R.id.editText_user_info_password);
                EditText et_name=findViewById(R.id.editText_user_info_name);
                EditText et_age=findViewById(R.id.editText_user_info_age);
                RadioGroup rg_gender=findViewById(R.id.radioGroup_user_info_gender);
                int rb_checked=rg_gender.getCheckedRadioButtonId();

                User_Info info=new User_Info();
                info.user_id=DEFAULT_USER_ID;
                info.password=et_password.getText().toString();
                info.age=Integer.parseInt(et_age.getText().toString());
                info.name=et_name.getText().toString();
                if(rb_checked==R.id.radioButton_user_info_male){
                    info.gender=User_Info.GENDER_MALE;
                }
                else{
                    info.gender=User_Info.GENDER_FEMALE;
                }

                User_Info preInfo=mDBHelper.queryUserInfoByUserId(DEFAULT_USER_ID);
                ContentValues values=new ContentValues();
                boolean flag=false;
                if(!info.password.equals(preInfo.password)){
                    values.put(User_Info.PASSWORD,info.password);
                    flag=true;
                }
                if(info.age!=preInfo.age){
                    values.put(User_Info.AGE,info.age);
                    flag=true;
                }
                if(!info.name.equals(preInfo.name)){
                    values.put(User_Info.NAME,info.name);
                    flag=true;
                }
                if(!info.gender.equals(preInfo.gender)){
                    values.put(User_Info.GENDER,info.gender);
                    flag=true;
                }
                if(flag){
                    //本地数据库更新;
                    mDBHelper.updateUserInfoByUserId(DEFAULT_USER_ID,values);

                    //服务器更新相关数据;
                    Map<String,String> valuesMap=new HashMap<>();
                    for(String key:values.keySet()){
                        valuesMap.put(key,values.getAsString(key));
                    }
                    Map<String,String> selection=new HashMap<>();
                    selection.put(User_Info.USER_ID,DEFAULT_USER_ID);

                    mClient.updateValuesToServerTable(User_Info.TABLE_USER,valuesMap,selection);

                    Toast.makeText(getApplicationContext(),"信息保存成功",Toast.LENGTH_LONG).show();

                }
            }
        });

        Button ScanQRCode=title_layout.findViewById(R.id.button_scanQRCode);
        ScanQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    //加载Note相关内容;
    private void loadNoteLayout(){

        View ViewUserInfo= LayoutInflater.from(this).inflate(R.layout.layout_note_content, null);
        LinearLayout layout=findViewById(R.id.linearLayout_content);
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        layout.removeAllViews();
        layout.addView(ViewUserInfo,params);

        //重新设置linearLayout_title
        LinearLayout title_layout=findViewById(R.id.linearLayout_note_title);
        ImageView icon=title_layout.findViewById(R.id.image_user_icon);
        icon.setImageResource(R.drawable.user_icon);

        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUserInfoLayout();
            }
        });

        //从本地数据库中加载该用户最新的笔记内容;
        Note note=mDBHelper.queryNoteByUserId(DEFAULT_USER_ID);
        EditText et_content=layout.findViewById(R.id.editText_note_content);
        et_content.setText(note.content);

        Button btn_save=title_layout.findViewById(R.id.button_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取写入内容并判断是否做出改动，改动了则插入数据库;
                EditText et_content=findViewById(R.id.editText_note_content);
                String noteContent=et_content.getText().toString();
                Note note=mDBHelper.queryNoteByUserId(DEFAULT_USER_ID);
                if(note.user_id==null){
                    //当未能查询到时说明是第一次写入,则进行插入;
                    mDBHelper.insertNoteByUserId(DEFAULT_USER_ID,noteContent);
                }
                else if(!note.content.equals(noteContent)){
                    mDBHelper.updateNoteContentByUserId(DEFAULT_USER_ID,noteContent);
                }
                Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_LONG).show();
            }
        });
    }


    protected void onResume(){
        super.onResume();
    }

    protected void onDestroy(){
        mDBHelper.closeLink();
        super.onDestroy();
    }
}
