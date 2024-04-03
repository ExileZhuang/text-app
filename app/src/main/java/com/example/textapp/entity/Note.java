package com.example.textapp.entity;

public class Note {
    public int note_id;
    public String user_id;
    public String content;

    public Note(int _note_id,String _user_id,String _content){
        note_id=_note_id;
        user_id=_user_id;
        content=_content;
    }

    public Note(String _user_id){
        user_id=_user_id;
    }

    public Note(){
        user_id=null;
        content=null;
    }
}
