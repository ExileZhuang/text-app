import java.sql.*;
import java.util.ArrayList;
import java.util.*;

class SQLHelper{

    private static final String JDBC_DRIVER="com.mysql.jdbc.Driver";

    private static final String DATABASE_URL="jdbc:mysql://localhost:3306/database-textapp?characterEncoding=utf8&useSSL=false";

    private static final String DATABASE_USER="root";
        
    private static final String DATABASE_PASSWORD="20020911z";

    public static final String TABLE_USER_INFO="user_info";

    public static final String TABLE_LOGIN_INFO="login_info";

    public static final String USER_ID="user_id";

    public static final String USER_NAME="name";

    public static final String USER_PASSWORD="password";

    public static final String USER_AGE="age";

    public static final String USER_GENDER="gender";

    public static final String LOGIN_INFO_ID="info_id";

    public static final String LOGIN_TIME="time";

    public static final String Login_DEVICE="device";

    private Connection connect;

    private Statement statement;

    public SQLHelper(){
        //加载java与mysql连接的驱动程序,与数据库建立连接;
        try{
            Class.forName(JDBC_DRIVER);
            connect=DriverManager.getConnection(DATABASE_URL,DATABASE_USER,DATABASE_PASSWORD);
            statement=connect.createStatement();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void close(){
        try{
            statement.close();
            connect.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public List<Map<String,String>> query(String tableName, List<String> queryColumns, Map<String, String> selections) {
      String sql="SELECT ";
      for(int i=0;i<queryColumns.size();++i){
        sql=sql+queryColumns.get(i)+",";
      }
      sql=sql.substring(0,sql.length()-1);
      sql=sql+" FROM "+tableName+" WHERE ";
      for(String key:selections.keySet()){
        String value=selections.get(key);
        if(key.equals(USER_AGE)){
            sql=sql+key+"="+value+" AND ";
        }
        else{
            sql=sql+key+"='"+value+"' AND ";
        }
      }
      sql=sql.substring(0,sql.length()-5);
      List<Map<String,String>> list=new ArrayList<Map<String,String>>();
      ResultSet result;
      try{
        result=statement.executeQuery(sql);
        while(result.next()){
            Map<String,String> data= new HashMap<String,String>();
            for(int i=0;i<queryColumns.size();++i){
                String key=queryColumns.get(i);
                String value=result.getObject(key).toString();
                data.put(key,value);
            }
            list.add(data);
        }
      }catch(SQLException e){
        e.printStackTrace();
      }
      return list;
    }

    public boolean insert(String TableName,Map<String,String> map){
        String sql="INSERT INTO "+TableName+"(";
        for(String key:map.keySet()){
            sql=sql+key+",";
        }
        sql=sql.substring(0,sql.length()-1)+") VALUES (";
        for(String key:map.keySet()){
            String value=map.get(key);
            if(key.equals(USER_AGE)){
                sql=sql+value+",";
            }
            else{
                sql=sql+"'"+value+"',";
            }
        }
        sql=sql.substring(0,sql.length()-1)+")";
        int result=0;
        try{
            result=statement.executeUpdate(sql);
        }catch(SQLException e){
            e.printStackTrace();
        }
        return result>0?true:false;
    }

    public boolean update(String TableName,Map<String,String> data,Map<String,String> selections){
        String sql="UPDATE "+TableName+" SET ";
        for(String key:data.keySet()){
            if(key.equals(USER_AGE)){
                sql=sql+key+"="+data.get(key)+",";
            }
            else{
                sql=sql+key+"='"+data.get(key)+"',";
            }
        }
        sql=sql.substring(0,sql.length()-1)+" WHERE ";
        for(String key:selections.keySet()){
            if(key.equals(USER_AGE)){
                sql=sql+key+"="+selections.get(key)+" AND ";
            }
            else{
                sql=sql+key+"='"+selections.get(key)+"' AND ";
            }
        }
        sql=sql.substring(0, sql.length()-5);
        int result=0;
        try{
            result=statement.executeUpdate(sql);
        }catch(SQLException e){
            e.printStackTrace();
        }
        return result>0?true:false;
    }

    public boolean delete(String TableName,Map<String,String> selections){
        String sql="DELETE FROM "+TableName+" WHERE ";
        for(String key:selections.keySet()){
            //sql=sql+key+"="+selections.get(key)+" AND ";
            if(key.equals(USER_AGE)){
                sql=sql+key+"="+selections.get(key)+" AND ";
            }
            else{
                sql=sql+key+"='"+selections.get(key)+"' AND ";
            }
        }
        sql=sql.substring(0,sql.length()-5);
        int result=0;
        try{
            result=statement.executeUpdate(sql);
        }catch(SQLException e){
            e.printStackTrace();
        }
        return result>0?true:false;
    }
}