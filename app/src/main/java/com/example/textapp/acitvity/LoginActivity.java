package com.example.textapp.acitvity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.textapp.MyApplication;
import com.example.textapp.R;
import com.example.textapp.TCPClient.TCPClient;
import com.example.textapp.Util.SharedUtil;
import com.example.textapp.database.NotesDBHelper;
import com.example.textapp.entity.User_Info;

public class LoginActivity extends AppCompatActivity {

    private NotesDBHelper mDBHelper;

    private TCPClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_login);

        //获取客户端;
        mClient= MyApplication.getInstance().getClient();

        //打开数据库链接;
        mDBHelper=NotesDBHelper.getInstance(this);
        mDBHelper.openReadLink();
        mDBHelper.openWriteLink();

        //初始化为账号密码登录;
        showLoginPasswordLayout();


        //对于登录的三个按钮进行对应页面的展示;
        findViewById(R.id.button_login_nfc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoginNFCLayout();
            }
        });

        findViewById(R.id.button_login_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoginPasswordLayout();

            }
        });

        findViewById(R.id.button_login_qrcode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoginQRcodeLayout();
            }
        });
    }

    //展示二维码页面;
    private void showLoginQRcodeLayout() {
        View qrcodeView= LayoutInflater.from(this).inflate(R.layout.layout_login_qrcode,null);
        LinearLayout layout=findViewById(R.id.LinearLayout_login_show);
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        layout.removeAllViews();
        layout.addView(qrcodeView,params);
    }

    //展示账号密码登录页面;
    private void showLoginPasswordLayout() {
        View passwordView= LayoutInflater.from(this).inflate(R.layout.layout_login_password,null);
        LinearLayout layout=findViewById(R.id.LinearLayout_login_show);
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        layout.removeAllViews();
        layout.addView(passwordView,params);

        SharedUtil mUtil=SharedUtil.getInstance(this);
        boolean showPasswordFromPreference=mUtil.readBoolean(SharedUtil.RESTOREDPASSWORD,false);

        //寻找对应组件并对点击事件进行监听;
        CheckBox cb_restorePassword=passwordView.findViewById(R.id.checkbox_restore_password);
        Button btn_login=passwordView.findViewById(R.id.button_login);
        TextView tv_register=passwordView.findViewById(R.id.textview_register);
        EditText et_password=layout.findViewById(R.id.editText_password);
        EditText et_userId=layout.findViewById(R.id.editText_userId);

        
        cb_restorePassword.setChecked(showPasswordFromPreference);

        if(showPasswordFromPreference){
            //从preference中读取账号密码并展示;
            String storedId=mUtil.readString(SharedUtil.USERID,null);
            String storedPassword=mUtil.readString(SharedUtil.PASSWORD,null);
            if(storedId==null||storedPassword==null){
                Log.v("Note","Read Stored String error");
            }
            else{
                et_password.setText(storedPassword);
                et_userId.setText(storedId);
            }
        }


        //保存密码:不需要抓取checkedBox的checked情况改变做出处理;

        //登录实现;
        btn_login.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //处理登录情况;
                EditText et_userId=findViewById(R.id.editText_userId);
                EditText et_password=findViewById(R.id.editText_password);
                String userId=et_userId.getText().toString();
                String password=et_password.getText().toString();

                //读取账号密码并判断是否合法;
                boolean correct=false;

                //本地数据库查找有无用户信息;
                User_Info info=mDBHelper.queryUserInfoByUserId(userId);
                if(info.user_id!=null&&info.password.equals(password)){
                    correct=true;
                }
                
                if(!correct){
                    //服务器端查询;
                    mClient.sendQueryPasswordByUserId(userId);
                }
                
                
                if(correct){
                    //查看checked_box情况，确定用户是否需要保存账号密码;
                    CheckBox cb_restorePassword=findViewById(R.id.checkbox_restore_password);
                    boolean isChecked=cb_restorePassword.isChecked();
                    SharedUtil mUtil=SharedUtil.getInstance(LoginActivity.this);
                    if(isChecked){
                        //preference本地保存账号密码;
                        mUtil.writeBoolean(SharedUtil.RESTOREDPASSWORD,true);
                        mUtil.updateString(SharedUtil.USERID,et_userId.getText().toString());
                        mUtil.updateString(SharedUtil.PASSWORD,et_password.getText().toString());
                    }
                    else{
                        mUtil.writeBoolean(SharedUtil.RESTOREDPASSWORD, false);
                    }

                    //登录成功后对login_info库进行插入本次登录信息;
                    mDBHelper.insertLoginInfoByUserId(userId);
                    //Log.v("Note","Next Activity");
                    Intent intent=new Intent(LoginActivity.this,NoteActivity.class);
                    intent.putExtra(User_Info.USER_ID,info.user_id);
                    startActivity(intent);
                    finish();
                }
                else{
                    Toast.makeText(getApplicationContext(),"账号或者密码错误",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        //注册账号;
        tv_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadRegisterLayout();
            }
        });

    }

    private void LoadRegisterLayout() {
        View registerView= LayoutInflater.from(this).inflate(R.layout.layout_register_user,null);
        LinearLayout layout=findViewById(R.id.LinearLayout_login_show);
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        layout.removeAllViews();
        layout.addView(registerView,params);

        //获取各个注册按钮;
        Button bnt_register=layout.findViewById(R.id.button_register);

        bnt_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText et_register_userId=findViewById(R.id.editText_register_userId);
                EditText et_register_password=findViewById(R.id.editText_register_password);
                EditText et_confirm_password=findViewById(R.id.editText_confirm_password);
                EditText et_name=findViewById(R.id.editText_register_name);
                EditText et_age=findViewById(R.id.editText_register_age);
                RadioGroup rg_gender=findViewById(R.id.radioGroup_register_gender);

                String userId=et_register_userId.getText().toString();
                String password=et_register_password.getText().toString();
                String confirm_password=et_confirm_password.getText().toString();
                if(userId.isEmpty()||password.isEmpty()){
                    Toast.makeText(getApplicationContext(),"账号与密码不能为空",Toast.LENGTH_LONG).show();
                    return;
                }
                if(!password.equals(confirm_password)){
                    Toast.makeText(getApplicationContext(),"请保持密码一致",
                            Toast.LENGTH_LONG).show();
                }
                else{
                    int rb_gender=rg_gender.getCheckedRadioButtonId();
                    String gender=User_Info.GENDER_MALE;
                    if(rb_gender==R.id.radioButton_gender_female){
                        gender=User_Info.GENDER_FEMALE;
                    }
                    User_Info info=new User_Info(et_register_userId.getText().toString(),password,
                            Integer.parseInt(et_age.getText().toString()),gender,et_name.getText().toString());

                    //判断用户注册的userId是否为新id,先从本地数据库读取,再从服务端读取用户数据库判断是否有重复;
                    User_Info queryInfo=mDBHelper.queryUserInfoByUserId(info.user_id);
                    if(queryInfo.user_id!=null){
                        //本地中已存在该账号;
                        Toast.makeText(getApplicationContext(),"账号已存在",Toast.LENGTH_LONG).show();
                        return;
                    }

                    //对服务端数据库进行查询查询是否存在该账号用户;
                    //TODO

                    //确定该账号不重复后:加入本地数据库,加入服务器数据库;
                    mDBHelper.insertUserInfoByUserInfo(info);
                    
                    //TODO
                    //加入服务器数据库;

                    //本次登录信息记录到数据库并跳转到NoteActivity;
                    mDBHelper.insertLoginInfoByUserId(info.user_id);
                    Intent intent=new Intent(LoginActivity.this,NoteActivity.class);
                    intent.putExtra(User_Info.USER_ID,info.user_id);
                    startActivity(intent);
                    finish();
                }

            }
        });





    }
    



    //展示NFC卡登录界面;
    private void showLoginNFCLayout() {
        View nfcView= LayoutInflater.from(this).inflate(R.layout.layout_login_nfc,null);
        LinearLayout layout=findViewById(R.id.LinearLayout_login_show);
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        layout.removeAllViews();
        layout.addView(nfcView,params);
    }

    protected void onDestroy() {
        mDBHelper.closeLink();
        super.onDestroy();
    }

}