package com.example.textapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import com.example.textapp.entity.Login_Info;
import com.example.textapp.entity.Note;
import com.example.textapp.entity.User_Info;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class NotesDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME="notes.db";

    private static final int DB_VERSION=1;

    private static final String TABLE_USER_INFO="user_info";

    private static final String TABLE_LOGIN_INFO="login_info";

    private static final String TABLE_NOTES_INFO="notes_info";

    private static NotesDBHelper mHelper=null;

    private static SQLiteDatabase mRDB=null;

    private static SQLiteDatabase mWDB=null;




    private NotesDBHelper(Context context){
        super(context,DB_NAME,null,DB_VERSION);
    }

    public static NotesDBHelper getInstance(Context context){
        if(mHelper==null){
            mHelper=new NotesDBHelper(context);
        }
        return mHelper;
    }

    public void openReadLink(){
        if(mRDB==null||!mRDB.isOpen()){
            mRDB=mHelper.getReadableDatabase();
        }
    }

    public void openWriteLink(){
        if(mWDB==null||!mWDB.isOpen()){
            mWDB=mHelper.getWritableDatabase();
        }
    }

    public void colseLink(){
        if(mRDB!=null&&mRDB.isOpen()){
            mRDB.close();
            mRDB=null;
        }
        if(mWDB!=null&&mWDB.isOpen()){
            mWDB.close();
            mWDB=null;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建用户信息表;
        //Table_user_info:
        //user_id VARCHAR(11) PRIMARY KEY NOT NULL;
        //password varchar not null
        //name varchar
        //gender (male or female)
        //age INTEGER
        String sql="CREATE TABLE IF NOT EXISTS "+TABLE_USER_INFO+"("+
                "user_id VARCAHR(11) PRIMARY KEY NOT NULL,"+
                "name VARCHAR,"+
                "password VARCHAR NOT NULL,"+
                "gender VARCHAR check(gender in('male','female')) not null default 'male',"+
                "age INTEGER);";

        db.execSQL(sql);

        //创建用户登录记录表;
        //info_id:INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
        //user_id:VARCHAR(11) NOT NULL
        //time:yyyy-MM-dd HH:mm:ss
        //device:设备名称-设备型号-设备安卓版本;
        sql="CREATE TABLE IF NOT EXISTS "+TABLE_LOGIN_INFO+"("+
                "info_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"+
                "user_id VARCHAR(11) NOT NULL,"+
                "time VARCHAR NOT NULL,"+
                "device VARCHAR NOT NULL);";

        db.execSQL(sql);

        //创建用户笔记表:
        //note_id:INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
        //user_id: VARCHAR(11) NOT NULL
        //note_content:VARCHAR
        sql="CREATE TABLE IF NOT EXISTS "+TABLE_NOTES_INFO+"("+
                "note_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"+
                "user_id VACHAR(11) NOT NULL,"+
                "note_content VARCHAR);";

        db.execSQL(sql);
        Log.v("Note","Create NoteDB");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    //往表login_info中根据userid的登录信息;
    public void insertLoginInfoByUserId(String userId) {
        openWriteLink();
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String nowDate=formatter.format(date).toString();
        String deviceInfo= Build.DEVICE+"-"+Build.MODEL+"-"+
                Build.VERSION.SDK_INT;
        ContentValues values=new ContentValues();
        values.put("user_id",userId);
        values.put("time",nowDate);
        values.put("device",deviceInfo);
        try{
            mWDB.beginTransaction();
            long res=mWDB.insert(TABLE_LOGIN_INFO,null, values);
            if(res==-1){
                //插入失败，日志报错;
                Log.v("Note","insert login_info "+userId+" failed");
            }
            mWDB.setTransactionSuccessful();
        }catch(Exception e) {
            e.printStackTrace();
        }finally{
            mWDB.endTransaction();
        }
        Log.v("Note","Insert Login Infomation By UserId");
    }

    //通过user_id查询用户信息;
    public User_Info queryUserInfoByUserId(String UserId){
        openReadLink();
        Cursor cursor=mRDB.query(TABLE_USER_INFO,new String[]{"user_id","name","password","gender","age"},
                "user_id=?",new String[]{UserId},null,null,null);
        User_Info info=new User_Info();
        if(cursor.moveToFirst()){
            info.user_id=cursor.getString(0);
            info.name=cursor.getString(1);
            info.password=cursor.getString(2);
            info.gender=cursor.getString(3);
            info.age=cursor.getInt(4);
        }
        cursor.close();
        Log.v("Note","Query User Info By UserId");
        return info;
    }

    //在user_info表中插入一条新的user_info;
    public void insertUserInfoByUserInfo(User_Info info) {
        openWriteLink();
        ContentValues values=new ContentValues();
        values.put("user_id",info.user_id);
        values.put("name",info.name);
        values.put("age",info.age);
        values.put("password",info.password);
        values.put("gender",info.gender);
        try{
            mWDB.beginTransaction();
            long res=mWDB.insert(TABLE_USER_INFO,null, values);
            if(res==-1){
                Log.v("Note","Insert New User_Info Failed");
            }
            mWDB.setTransactionSuccessful();
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            mWDB.endTransaction();
        }
        Log.v("Note","Insert User Info By UserInfo");
    }

    //查找指定user_id的note;
    public Note queryNoteByUserId(String UserId) {
        openReadLink();
        Cursor cursor=mRDB.query(TABLE_NOTES_INFO,new String[]{"note_id","user_id","note_content"},
                "user_id=?",new String[]{UserId},null,null,null);
        Note note=new Note();
        if(cursor.moveToFirst()){
            note.note_id=cursor.getInt(0);
            note.user_id=cursor.getString(1);
            note.content=cursor.getString(2);
        }
        cursor.close();
        Log.v("Note","Query Note Content By UserId");
        return note;
    }

    //更新指定user_id的note内容;
    public void updateNoteContentByUserId(String content, String UserId) {
        openWriteLink();
        ContentValues values=new ContentValues();
        values.put("note_content",content);
        try{
            mWDB.beginTransaction();
            long res=mWDB.update(TABLE_NOTES_INFO,values,"user_id=?",
                    new String[]{UserId});
            if(res==-1){
                Log.v("Note","Update NoteContent Failed");
            }
            mWDB.setTransactionSuccessful();
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            mWDB.endTransaction();
        }
        Log.v("Note","Update Note Content By UserId");
    }

    public ArrayList<Login_Info> queryLoginInfoByUserId(String UserId) {
        openReadLink();
        ArrayList<Login_Info> listLoginInfo=new ArrayList<Login_Info>();
        Cursor cursor=mRDB.query(TABLE_LOGIN_INFO,new String[]{"info_id","user_id","time","device"},
                "user_id=?",new String[]{UserId},null,null,"time DESC");
        while(cursor.moveToNext()){
            Login_Info info=new Login_Info();
            info.info_id=cursor.getInt(0);
            info.user_id=cursor.getString(1);
            info.time=cursor.getString(2);
            info.device=cursor.getString(3);
            listLoginInfo.add(info);
        }
        cursor.close();
        Log.v("Note","Query Login Info By UserId");
        return listLoginInfo;
    }

    //根据user_info更新指定user_id的user_info表中内容;
    public void updateUerInfoByUserId(String UserId,ContentValues values) {
        openWriteLink();
        try{
            mWDB.beginTransaction();
            long res=mWDB.update(TABLE_USER_INFO,values,"user_id=?",
                    new String[]{UserId});
            if(res==-1){
                Log.v("Note","Update UserInfo Failed");
            }
            mWDB.setTransactionSuccessful();
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            mWDB.endTransaction();
        }
        Log.v("Note","Update User Info By UserId");
    }

    public void insertNoteByUserId(String UserId, String noteContent) {
        openWriteLink();
        ContentValues values=new ContentValues();
        values.put("user_id",UserId);
        values.put("note_content",noteContent);
        try{
            mWDB.beginTransaction();
            long res=mWDB.insert(TABLE_NOTES_INFO,null,values);
            if(res==-1){
                Log.v("Note","Insert Note Failed");
            }
            mWDB.setTransactionSuccessful();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            mWDB.endTransaction();
        }
        Log.v("Note","Insert Note Content By UserId");
    }
}
