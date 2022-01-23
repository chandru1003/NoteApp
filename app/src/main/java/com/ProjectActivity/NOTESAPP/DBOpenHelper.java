package com.ProjectActivity.NOTESAPP;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

// Management of the database that will store the notes
public class DBOpenHelper extends SQLiteOpenHelper
{
    public DBOpenHelper(Context context, String name, CursorFactory factory, int version)
    {
        super(context, name, factory, version);
    }

    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(create_table);
    } // Create the database

    public void onUpgrade(SQLiteDatabase db, int version_old, int version_new)
    {
        db.execSQL(drop_table);
        db.execSQL(create_table);
    } // Upgrade the database

    // Fields of the database
    private static final String create_table = "create table test(" +
            "ID integer primary key autoincrement, " +
            "TITLE_NAME string, " + "DATA_NAME string, " +
            "TIME_NAME string," + "REMINDER_NAME string" + ")";
    private static final String drop_table = "drop table test";
}