package com.bookrental.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "book.db";

    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        //M_BOOKテーブル作成
        StringBuilder sbBook = new StringBuilder();
        sbBook.append("CREATE TABLE M_BOOK(");
        sbBook.append("book_id INTEGER PRIMARY KEY AUTOINCREMENT");
        sbBook.append(",book_title TEXT");
        sbBook.append(",book_kana TEXT");
        sbBook.append(",rental_flg INTEGER DEFAULT 0");
        sbBook.append(",rental_start_date TEXT");
        sbBook.append(",created_at TEXT NOT NULL DEFAULT (DATETIME('now', 'localtime'))");
        sbBook.append(",updated_at TEXT NOT NULL DEFAULT (DATETIME('now', 'localtime'))");
        sbBook.append(");");

        String sql=sbBook.toString();

        db.execSQL(sql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){

    }
}
