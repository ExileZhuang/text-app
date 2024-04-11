package com.example.textapp.entity;

//主线程与子线程间通信;
public class MessageType {

    public static final int WHAT_QUERY=1;

    public static final int WHAT_INSERT=2;

    public static final int WHAT_UPDATE=3;

    public static final int WHAT_DELETE=4;

    public static final String BUNDLE_KEY_TABLENAME="TableName";

    public static final String BUNDLE_KEY_QUERYCOLUMNS="QueryColumns";

    public static final String BUNDLE_KEY_SELECTIONKEYS="SelectionsKeys";

    public static final String BUNDLE_KEY_SELECTIONSVALUES="SelectionsValues";

    public static final String BUNDLE_KEY_RESULTSCOUNT="ResultsCount";

    public static final String BUNDLE_KEY_RESULTINDEX="Result";

    public static final String BUNDLE_KEY_VALUES="Values";

    public static final String BUNDLE_KEY_KEYS="Keys";
}
