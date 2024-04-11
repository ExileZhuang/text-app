import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

public class NetMessage {

    //传递的json中各项key的值;
    public static final String MESSAGE_TYPE="MessageType";
    public static final String ANSMESSAGE_TYPE="AnsMessageType";

    public static final String ANSMESSAGE_TYPE_QUERY="1";
    public static final String QUERY_RESULTS="QueryResults";
    //查询结果用AnsMessageType=1;
    //返回形式:{AndMessageType:"1",
    //            QueryResults:[{},{},...]};




    public static final String MESSAGE_TYPE_QUERY="1";
    public static final String TABLE_NAME="TableName";
    public static final String QUERYCOLUMNS="QueryColumns";
    public static final String SELECTIONS="Selections";
    //查询则MessageType设为1;
    //json格式:{MessageType:"1",
                // TableName:table_name,
                // QueryColumns:[column1,column2,...],
                // Selections:{key1:value1,key2:value2,...},}
    //test:{MessageType:"1",TableName:"user_info",QueryColumns:[password],Selections:[{"user_id","18460399833"}]};

    public static final String MESSAGE_TYPE_INSERT="2";
    public static final String VALUES="Values";
    //插入则MessageType设为2;
    // json格式:{MessageType:"2",
    //             TableName:table_name,
    //             Values:{key1:value1,key2:value2,...}}

    public static final String ANSMESSAGE_TYPE_INSERT="2";
    public static final String STATUS="Status";
    public static final String STATUS_SUCCESS="200";
    public static final String STATUS_FAIL="500";
    //插入成功返回状态码200，失败返回状态码500;
    //json格式:{AnsMessageType:2,Status:200/500};


    public static final String ANSMESSAGEE_TYPE_UPDATE="3";

    public static final String MESSAGE_TYPE_UPDATE="3";
    //更新则MessageType为3;
    //json格式:{MessageType:"3",
    //          TableName:table_name,
    //          Values:{key1:value1,key2:value2,...},
    //          Selections:{key1:value1,key2:value2,...}}

    public static final String ANSMESSAGE_TYPE_DELETE="4";
    public static final String MESSAGE_TYPE_DELETE="4";
    //删除则MessageType为4;
    //json格式为:{MessageType:"4",
    //          TableName:table_name,
    //          Selections:{key1:value1,key2:value2,...}}

    private JSONObject json;

    public NetMessage(){
        json=new JSONObject();
    }

    public NetMessage(String jsonStr){
        json=new JSONObject(jsonStr);
    }

    public String getMessageType(){
        return json.getString(MESSAGE_TYPE);
    }

    public String getAnsMessageType(){
        String type=json.getString(ANSMESSAGE_TYPE);
        return type;
    }

    public String getString(String key){
        return json.getString(key);
    }

    public List<String> getListString(String key){
        JSONArray list=json.getJSONArray(key);
        List<String> lString=new ArrayList<String>();
        for(int i=0;i<list.length();++i){
            lString.add(list.getString(i));
        }
        return lString;
    }

    public Map<String, String> getMap(String key) {
        JSONObject object=json.getJSONObject(key);
        Map<String,String> m=new HashMap<String,String>();
        Iterator<String> keys=object.keys();
        while(keys.hasNext()){
            String now=keys.next();
            String value=object.getString(now);
            m.put(now, value);
        }
        return m;
    }

    public void put(String key, Object value) {
        json.put(key,value);
    }

    public String toString(){
        return json.toString();
    }

    public List<Map<String,String>> getQueryResults(String key){
        List<Map<String,String>> result=new ArrayList<Map<String,String>>();
        try{
            JSONArray array=json.getJSONArray(key);
            for(int i=0;i<array.length();i++){
                JSONObject json=array.getJSONObject(i);
                Map<String,String> m=new HashMap<String,String>();
                Iterator<String> keys=json.keys();
                while(keys.hasNext()){
                    String now=keys.next();
                    String value=json.getString(now);
                    m.put(now,value);
                }
                result.add(m);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

}
