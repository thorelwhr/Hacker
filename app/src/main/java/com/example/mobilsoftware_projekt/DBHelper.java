package com.example.mobilsoftware_projekt;

import androidx.annotation.Nullable;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;


public class DBHelper extends SQLiteOpenHelper
{
    private static final String TAG ="DatabaseHelper";

    public static final String TABLE_NAME = "DataList";
    public static final String COLUMN_NAME_ID = "ID";
    public static final String COLUMN_NAME_VERKEHRSMITTEL = "Verkehrsmittel";
    public static final String COLUMN_NAME_ZEIT = "Zeit";
    public static final String COLUMN_NAME_DISTANZ = "Distanz";
    public static final String COLUMN_NAME_DATUM = "Datum";
    public static final String COLUMN_NAME_STANDORT = "Standort";
    public static final String COLUMN_NAME_START = "Start";
    public static final String COLUMN_NAME_ENDE = "Ende";

    public static final String CreateDatabase = "CREATE TABLE "+ TABLE_NAME+ "("+  COLUMN_NAME_ID+ " INTEGER PRIMARY KEY AUTOINCREMENT, "+
            COLUMN_NAME_VERKEHRSMITTEL+ " TEXT NOT NULL, "+ COLUMN_NAME_ZEIT+ " TEXT NOT NULL, "+ COLUMN_NAME_DISTANZ+ " TEXT NOT NULL, "+
            COLUMN_NAME_DATUM+ " TEXT NOT NULL, "+ COLUMN_NAME_STANDORT+ " TEXT NOT NULL, "+ COLUMN_NAME_START+ " TEXT NOT NULL, "+
            COLUMN_NAME_ENDE+ " TEXT NOT NULL);";


    private static final String _DB_FILE_NAME = "locations.db";
    public DBHelper(@Nullable Context context){super(context, _DB_FILE_NAME, null, 2 );}


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CreateDatabase);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addData(/*String ID, */String Verkehrsmittel, String Zeit, String Distanz,
                                          String Datum, String Standort, String Start, String Ende) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        //contentValues.put(COLUMN_NAME_ID, ID);
        contentValues.put(COLUMN_NAME_VERKEHRSMITTEL, Verkehrsmittel);
        contentValues.put(COLUMN_NAME_ZEIT, Zeit);
        contentValues.put(COLUMN_NAME_DISTANZ, Distanz);
        contentValues.put(COLUMN_NAME_DATUM, Datum);
        contentValues.put(COLUMN_NAME_STANDORT, Standort);
        contentValues.put(COLUMN_NAME_START, Start);
        contentValues.put(COLUMN_NAME_ENDE, Ende);

    long result = db.insert(TABLE_NAME, null, contentValues);

        if (result == -1) {
            return false;
        }
        else {
            return true;
        }
    }

    public Cursor getData() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query,null);
        return data;
    }

    public Cursor getDataByID(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME_ID + "= " + id;
        return db.rawQuery(query, null);
    }

    public void deleteDataByID(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        // Define 'where' part of query.
        String selection = COLUMN_NAME_ID + "=" + id;
        // Issue SQL statement.
        int deletedRows = db.delete(TABLE_NAME, selection, null);
    }
}