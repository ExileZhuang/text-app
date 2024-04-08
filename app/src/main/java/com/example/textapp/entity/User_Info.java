package com.example.textapp.entity;

public class User_Info {

    public static final String TABLE_USER="user_info";

    public static final String USER_ID="user_id";

    public static final String PASSWORD="password";

    public static final String AGE="age";

    public static final String GENDER="gender";

    public static final String GENDER_MALE="male";
    public static final String GENDER_FEMALE="female";

    public static final String NAME="name";

    public String user_id;

    public String password;

    public int age;

    public String gender;

    public String name;

    public User_Info(String _user_id,String _password,
                     int _age,String _gender,String _name){
        user_id=_user_id;
        password=_password;
        age=_age;
        gender=_gender;
        name=_name;
    }

    public User_Info(){
        user_id=null;
        password=null;
        gender=GENDER_MALE;
    }

    public boolean equals(User_Info info){
        if(this.user_id.equals(info.user_id)&&
        this.password.equals(info.password)&&
        this.name.equals(info.name)&&
        this.gender.equals(info.gender)&&
        this.age==info.age){
            return true;
        }
        return false;
    }

}
