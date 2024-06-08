package com.example.textapp.entity;

//主线程与子线程间通信;
public class MessageType {

    public static final int WHAT_QUERY=1;

    public static final int WHAT_INSERT=2;

    public static final int WHAT_UPDATE=3;

    public static final int WHAT_DELETE=4;

    public static  final int WHAT_QRCODEID_GET =5;

    public static final int WHAT_QRCODEID_ACKNOWLEDGE =6;

    public static final int WHAT_QRCODEID_AUTHORIZE=7;

    public static final int What_HARDWARECONTROL =8;

    public static final String BUNDLE_KEY_USERID="user_id";

    public static final String BUNDLE_KEY_TABLENAME="TableName";

    public static final String BUNDLE_KEY_QUERYCOLUMNS="QueryColumns";

    public static final String BUNDLE_KEY_SELECTIONS="Selections";

    public static final String BUNDLE_KEY_RESULTSCOUNT="ResultsCount";

    public static final String BUNDLE_KEY_RESULTINDEX="Result";

    public static final String BUNDLE_KEY_VALUES="Values";

    public static final String BUNDLE_KEY_QRCODEID="QRCodeId";

    public static final String BUNDLE_KEY_STATUS="Status";

}
