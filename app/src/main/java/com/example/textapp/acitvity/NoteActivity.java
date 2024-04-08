package com.example.textapp.acitvity;

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

import java.util.ArrayList;
import java.util.List;

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
        DEFAULT_USER_ID=intent.getStringExtra("userId");

        loadNoteLayout();

        //实现该组件对应按钮;
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
        if(info.gender.equals("male")){
            RadioButton rb_male=rg_gender.findViewById(R.id.radioButton_user_info_male);
            rb_male.setChecked(true);
        }
        else{
            RadioButton rb_female=rg_gender.findViewById(R.id.radioButton_user_info_female);
            rb_female.setChecked(true);
        }

        //从库中找到该用户最近登录的五次结果;
        //TODO
        //本地数据需要与服务器数据进行合并并选取最新的五次;
        List<Login_Info> listLoginInfo=mDBHelper.queryLoginInfoByUserId(DEFAULT_USER_ID);
        if(listLoginInfo.size()>5){
            listLoginInfo=listLoginInfo.subList(0,5);
        }
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
                    info.gender="male";
                }
                else{
                    info.gender="female";
                }

                User_Info preInfo=mDBHelper.queryUserInfoByUserId(DEFAULT_USER_ID);
                ContentValues values=new ContentValues();
                boolean flag=false;
                if(!info.password.equals(preInfo.password)){
                    values.put("password",info.password);
                    flag=true;
                }
                if(info.age!=preInfo.age){
                    values.put("age",info.age);
                    flag=true;
                }
                if(!info.name.equals(preInfo.name)){
                    values.put("name",info.name);
                    flag=true;
                }
                if(!info.gender.equals(preInfo.gender)){
                    values.put("gender",info.gender);
                    flag=true;
                }
                if(flag){
                    //本地数据库更新;
                    mDBHelper.updateUerInfoByUserId(DEFAULT_USER_ID,values);

                    //TODO;
                    //服务器更新相关数据;

                    Toast.makeText(getApplicationContext(),"信息保存成功",Toast.LENGTH_LONG).show();

                }
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
        mDBHelper.colseLink();
        super.onDestroy();
    }
}
