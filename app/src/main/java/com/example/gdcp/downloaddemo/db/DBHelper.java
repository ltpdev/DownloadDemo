package com.example.gdcp.downloaddemo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by asus- on 2017/11/13.
 */

public class DBHelper extends SQLiteOpenHelper{
     private static final String DB_NAME="downloads.db";
    private static final int VERSION=1;
    private static final String SQL_CREATE="create table if not exists thread_info(_id integer primary key autoincrement,thread_id integer" +
            ",url text,start long,end long,finished long)";
    private static final String SQL_DROP="drop table if exists thread_info";
    private static DBHelper dbHelper;

    public static DBHelper getInstance(Context context){
        if (dbHelper==null){
            dbHelper=new DBHelper(context);
        }
        return dbHelper;
    }
    private DBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
           db.execSQL(SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP);
        db.execSQL(SQL_CREATE);
    }
}
