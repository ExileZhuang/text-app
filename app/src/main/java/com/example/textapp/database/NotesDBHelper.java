package com.example.textapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.textapp.entity.Login_Info;
import com.example.textapp.entity.Note;
import com.example.textapp.entity.User_Info;

import java.util.ArrayList;

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

    public void closeLink(){
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
                User_Info.USER_ID+" VARCAHR(11) PRIMARY KEY NOT NULL,"+
                User_Info.NAME+" VARCHAR,"+
                User_Info.PASSWORD+" VARCHAR NOT NULL,"+
                User_Info.GENDER+" VARCHAR check("+User_Info.GENDER+" in('"+User_Info.GENDER_MALE+"','"+User_Info.GENDER_FEMALE+"')) not null default '"+User_Info.GENDER_MALE+"',"+
                User_Info.AGE+" INTEGER);";

        db.execSQL(sql);

        //创建用户登录记录表;
        //info_id:INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
        //user_id:VARCHAR(11) NOT NULL
        //time:yyyy-MM-dd HH:mm:ss
        //device:设备名称-型号-品牌-安卓版本;
        sql="CREATE TABLE IF NOT EXISTS "+TABLE_LOGIN_INFO+"("+
                Login_Info.INFO_ID+" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"+
                User_Info.USER_ID+" VARCHAR(11) NOT NULL,"+
                Login_Info.TIME+" VARCHAR NOT NULL,"+
                Login_Info.DEVICE+" VARCHAR NOT NULL);";

        db.execSQL(sql);

        //创建用户笔记表:
        //note_id:INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
        //user_id: VARCHAR(11) NOT NULL
        //note_content:VARCHAR
        sql="CREATE TABLE IF NOT EXISTS "+TABLE_NOTES_INFO+"("+
                Note.NOTE_ID+" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"+
                User_Info.USER_ID+" VACHAR(11) NOT NULL,"+
                Note.CONTENT+" VARCHAR);";

        db.execSQL(sql);
        Log.v("Note","Create NoteDB");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void insertNewValuesToTable(String Table_name,ContentValues values){
        openWriteLink();
        try{
            mWDB.beginTransaction();
            long res=mWDB.insert(Table_name,null,values);
            if(res==-1){
                Log.v("Note","Insert Values To "+Table_name+" Failed");
            }
            mWDB.setTransactionSuccessful();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            mWDB.endTransaction();
        }
        Log.v("Note","Insert Values To "+Table_name+" Success");
    }

    //往表login_info中插入根据userid的登录信息;
    public void insertLoginInfoByUserId(String userId,String time,String Device) {
        openWriteLink();
        ContentValues values=new ContentValues();
        values.put(User_Info.USER_ID,userId);
        values.put(Login_Info.TIME,time);
        values.put(Login_Info.DEVICE,Device);
        insertNewValuesToTable(TABLE_LOGIN_INFO,values);
    }

    //通过user_id查询用户信息;
    public User_Info queryUserInfoByUserId(String UserId){
        openReadLink();
        Cursor cursor=mRDB.query(TABLE_USER_INFO,new String[]{User_Info.USER_ID,User_Info.NAME,User_Info.PASSWORD,User_Info.GENDER,User_Info.AGE},
                User_Info.USER_ID+"=?",new String[]{UserId},null,null,null);
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
        values.put(User_Info.USER_ID,info.user_id);
        values.put(User_Info.NAME,info.name);
        values.put(User_Info.AGE,info.age);
        values.put(User_Info.PASSWORD,info.password);
        values.put(User_Info.GENDER,info.gender);
        insertNewValuesToTable(TABLE_USER_INFO,values);
    }

    //查找指定user_id的note;
    public Note queryNoteByUserId(String UserId) {
        openReadLink();
        Cursor cursor=mRDB.query(TABLE_NOTES_INFO,new String[]{Note.NOTE_ID,User_Info.USER_ID,Note.CONTENT},
                User_Info.USER_ID+"=?",new String[]{UserId},null,null,null);
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
        values.put(Note.CONTENT,content);
        try{
            mWDB.beginTransaction();
            long res=mWDB.update(TABLE_NOTES_INFO,values,User_Info.USER_ID+"=?",
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
        Cursor cursor=mRDB.query(TABLE_LOGIN_INFO,new String[]{Login_Info.INFO_ID,User_Info.USER_ID,Login_Info.TIME,Login_Info.DEVICE},
                User_Info.USER_ID+"=?",new String[]{UserId},null,null,"time DESC");
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

    //通过User_Info类信息更新user_info库;
    public void updateUserInfoByUserId(User_Info info){
        ContentValues values=new ContentValues();
        values.put(User_Info.USER_ID,info.user_id);
        values.put(User_Info.PASSWORD,info.password);
        values.put(User_Info.NAME,info.name);
        values.put(User_Info.AGE,info.age);
        values.put(User_Info.GENDER,info.gender);
        updateUserInfoByUserId(info.user_id,values);
    }
    //根据user_info更新指定user_id的user_info表中内容;
    public void updateUserInfoByUserId(String UserId, ContentValues values) {
        openWriteLink();
        try{
            mWDB.beginTransaction();
            long res=mWDB.update(TABLE_USER_INFO,values,User_Info.USER_ID+"=?",
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
        values.put(User_Info.USER_ID,UserId);
        values.put(Note.CONTENT,noteContent);
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
