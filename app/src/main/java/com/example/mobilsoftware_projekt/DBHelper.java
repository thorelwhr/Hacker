package com.example.mobilsoftware_projekt;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

public class DBHelper extends SQLiteOpenHelper
{
    public static final String TABLE_NAME = "DataList";
    public static final String COLUMN_NAME_ID = "ID";
    public static final String COLUMN_NAME_VERKEHRSMITTEL = "Verkehrsmittel";
    public static final String COLUMN_NAME_ZEIT = "Zeit";
    public static final String COLUMN_NAME_DISTANZ = "Distanz";
    public static final String COLUMN_NAME_DATUM = "Datum";
    public static final String COLUMN_NAME_STANDORT = "Standorte";

    public static final String CreateDatabase = "CREATE TABLE "+ TABLE_NAME+ "("+  COLUMN_NAME_ID+ "INTEGER PRIMARY KEY AUTOINCREMENT, "+
            COLUMN_NAME_VERKEHRSMITTEL+ "TEXT NOT NULL, "+ COLUMN_NAME_ZEIT+ "TEXT NOT NULL, "+ COLUMN_NAME_DISTANZ+ "TEXT NOT NULL, "+
            COLUMN_NAME_DATUM+ "TEXT NOT NULL, "+ COLUMN_NAME_STANDORT+ "TEXT NOT NULL);";



    public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version)
    {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(CreateDatabase);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }

    public void addData()
    {

    }
}