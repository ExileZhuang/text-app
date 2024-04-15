package com.example.textapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.textapp.MyApplication;
import com.example.textapp.QRCode.QRCode;
import com.example.textapp.R;
import com.example.textapp.TCPClient.TCPClient;
import com.example.textapp.Util.SharedUtil;
import com.example.textapp.database.NotesDBHelper;
import com.example.textapp.entity.Login_Info;
import com.example.textapp.entity.User_Info;
import com.example.textapp.entity.MessageType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private NotesDBHelper mDBHelper;

    private TCPClient mClient;

    //用于判断是否处于QRCode登录的模式下;
    //在QRCode模式下接收到服务器发送的授权信息则进行登录处理;
    //非QRCode模式下接收到服务器发送的授权信息则忽略处理;
    private boolean InQRCodeLoginProcess=false;

    //使用handler接收来自其他线程发送的消息并直接更新;
    private final Handler ReceiveHandler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg){
            super.handleMessage(msg);
            Log.v("Note","MainThread Get Message:"+msg.what);
            switch (msg.what){
                case MessageType.WHAT_QRCODEID_AUTHORIZE:
                    if(InQRCodeLoginProcess){
                        //接收到授权信息;
                        //进行处理;
                        Bundle bundle=msg.getData();
                        String userId= bundle.getString(MessageType.BUNDLE_KEY_USERID);
                        QRCodeLoginByUserId(userId);
                    }
                    break;
                default:
                    break;
            }
        }
    };

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
                showLoginQRCodeLayout();
            }
        });
    }

    //展示二维码页面;
    private void showLoginQRCodeLayout() {
        //加载二维码登录界面;
        View qrcodeView= LayoutInflater.from(this).inflate(R.layout.layout_login_qrcode,null);
        LinearLayout layout=findViewById(R.id.LinearLayout_login_show);
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        layout.removeAllViews();
        layout.addView(qrcodeView,params);

        //向服务器申请一个QRCodeId;
        String qrcodeId=mClient.getQRCodeIdMessageFromServer();
        try{
            ImageView img_qrcode=qrcodeView.findViewById(R.id.imageView_qrcode_show);
            Bitmap qrCode=QRCode.createQRCode(qrcodeId,500);
            img_qrcode.setImageBitmap(qrCode);
            //后续处理该qrcode的持续性和保存性问题;
            //preference?
        }catch (Exception e){
            e.printStackTrace();
        }

        //申请完后向子线程发送消息等待有无授权消息;
        //String UserId=mClient.waitQRCodeIdAuthorizationLoginMessage();

        //Bitmap qrCodeBitmap = EncodingHandler.createQRCode(qrcodeId, 350);
        //实现二维码生成;
        //TODO;
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
        //登录实现;
        btn_login.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //处理登录情况;
                EditText et_userId=findViewById(R.id.editText_userId);
                EditText et_password=findViewById(R.id.editText_password);
                String userId=et_userId.getText().toString();
                String password=et_password.getText().toString();

                if(userId.isEmpty() || password.isEmpty()){
                    Toast.makeText(getApplicationContext(),"账号或密码不能为空",Toast.LENGTH_LONG).show();
                    return;
                }

                //读取账号密码并判断是否合法;
                boolean correct=false;

                //本地数据库查找有无用户信息;
                User_Info info=mDBHelper.queryUserInfoByUserId(userId);
                if(info.user_id!=null&&info.password.equals(password)){
                    correct=true;
                }
                
                if(!correct){
                    //服务器端查询;
                    Map<String,String> selections=new HashMap<>();
                    selections.put(User_Info.USER_ID,userId);
                    ArrayList<String> queryColumn=new ArrayList<>();
                    queryColumn.add(User_Info.PASSWORD);
                    List<Map<String,String>> results=mClient.sendQueryColumnsBySelectionsToServerTable(User_Info.TABLE_USER,queryColumn, selections);
                    if(!results.isEmpty()){
                        String passwordFromServer=results.get(0).get(User_Info.PASSWORD);
                        if(passwordFromServer.equals(password)){
                            //成功找到对应账号密码;
                            correct=true;

                            //由于本地数据库未能成功匹配该账号密码，因此说明该账号本地存储的账号密码与服务器端不一致或者本地无该账号密码，因此更新或插入该信息;
                            ArrayList<String> queryColumns=new ArrayList<>();
                            queryColumns.add(User_Info.PASSWORD);
                            queryColumns.add(User_Info.AGE);
                            queryColumns.add(User_Info.NAME);
                            queryColumns.add(User_Info.GENDER);
                            Map<String,String> selection=new HashMap<>();
                            selection.put(User_Info.USER_ID,userId);
                            List<Map<String,String>> queryResults=mClient.sendQueryColumnsBySelectionsToServerTable(User_Info.TABLE_USER,queryColumns,selection);
                            Map<String,String> userinfoMap=queryResults.get(0);
                            User_Info newInfo=new User_Info();
                            newInfo.user_id=userId;
                            newInfo.password=userinfoMap.get(User_Info.PASSWORD);
                            newInfo.age=Integer.parseInt(Objects.requireNonNull(userinfoMap.get(User_Info.AGE)));
                            newInfo.gender=userinfoMap.get(User_Info.GENDER);
                            newInfo.name=userinfoMap.get(User_Info.NAME);
                            if(info.user_id==null){
                                //本地未有该账号信息时:从服务器获取全部信息并插入到本地数据库;
                                mDBHelper.insertUserInfoByUserInfo(newInfo);
                            }
                            else{
                                //本地有该账号信息时,从服务器获取所有信息并更新到本地数据库;
                                mDBHelper.updateUserInfoByUserId(newInfo);
                            }

                        }
                    }
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

                    //将本次登录信息插入本地和服务端;
                    insertLoginInfoToLocalAndServer(userId);

                    //Log.v("Note","Next Activity");
                    Intent intent=new Intent(LoginActivity.this,NoteActivity.class);
                    intent.putExtra(User_Info.USER_ID,userId);
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
                    User_Info info=new User_Info();
                    info.user_id=userId;
                    info.password=password;
                    info.gender=gender;
                    if(!et_name.getText().toString().isEmpty()){
                        info.name=et_name.getText().toString();
                    }
                    if(!et_age.getText().toString().isEmpty()){
                        info.age=Integer.parseInt(et_age.getText().toString());
                    }

                    //判断用户注册的userId是否为新id,先从本地数据库读取,再从服务端读取用户数据库判断是否有重复;
                    User_Info queryInfo=mDBHelper.queryUserInfoByUserId(info.user_id);
                    if(queryInfo.user_id!=null){
                        //本地中已存在该账号;
                        Toast.makeText(getApplicationContext(),"账号已存在",Toast.LENGTH_LONG).show();
                        return;
                    }

                    //对服务端数据库进行查询查询是否存在该账号用户;
                    ArrayList<String> queryColumn=new ArrayList<>();
                    queryColumn.add(User_Info.USER_ID);
                    Map<String,String> selection=new HashMap<>();
                    selection.put(User_Info.USER_ID,info.user_id);
                    List<Map<String,String>> result=mClient.sendQueryColumnsBySelectionsToServerTable(User_Info.TABLE_USER,queryColumn, selection);
                    if(!result.isEmpty()){
                        //服务器数据库中已经存在对应账号;
                        Toast.makeText(getApplicationContext(),"账号已存在",Toast.LENGTH_LONG).show();
                        return;
                    }

                    //确定该账号不重复后:加入本地数据库;
                    mDBHelper.insertUserInfoByUserInfo(info);
                    //加入服务器数据库;
                    Map<String,String> values=new HashMap<>();
                    values.put(User_Info.USER_ID,info.user_id);
                    values.put(User_Info.PASSWORD,info.password);
                    values.put(User_Info.GENDER,info.gender);
                    if(!et_name.getText().toString().isEmpty()){
                        values.put(User_Info.NAME,info.name);
                    }
                    if(!et_age.getText().toString().isEmpty()){
                        values.put(User_Info.AGE,String.valueOf(info.age));
                    }
                    mClient.insertNewValuesToServerTable(User_Info.TABLE_USER,values);

                    //本次登录信息记录到数据库与远程数据库;
                    insertLoginInfoToLocalAndServer(info.user_id);


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


    //获取时间和设备将本次登录信息记录到本地和数据库;
    public void insertLoginInfoToLocalAndServer(String userId){
        //登录成功后对login_info库进行插入本次登录信息;
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        Date date = new Date(System.currentTimeMillis());
        String time= formatter.format(date);
        //Device格式:设备名称-型号-品牌-安卓版本;
        String Device=Build.DEVICE+"-"+Build.MODEL+"-"+Build.BRAND+"-"+ Build.VERSION.SDK_INT;
        mDBHelper.insertLoginInfoByUserId(userId,time,Device);

        //对服务端数据库插入本次登录信息;
        Map<String,String> values=new HashMap<>();
        values.put(Login_Info.TIME,time);
        values.put(User_Info.USER_ID,userId);
        values.put(Login_Info.DEVICE,Device);
        mClient.insertNewValuesToServerTable(Login_Info.TABLE_LOGIN,values);
    }

    public void QRCodeLoginByUserId(String UserId){

    }
}