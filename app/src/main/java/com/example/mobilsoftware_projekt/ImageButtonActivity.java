package com.example.mobilsoftware_projekt;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class ImageButtonActivity extends AppCompatActivity
{
    ArrayList<String> dataList;
    DBHelper mDBHelper = new DBHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_button);

        ListView listview = findViewById(R.id.listview);
        Toolbar toolbar =findViewById(R.id.toolbar_fuer_ImageButtonActivity);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Trackdaten");

        ActionBar zuruck = getSupportActionBar();
        zuruck.setDisplayHomeAsUpEnabled(true);

        getData();
    }

    protected void getData()
    {
        if(mDBHelper != null)
        {
            Cursor data = mDBHelper.getData();
            ListView listview = new ListView(this);

            while(data.moveToNext())
            {
                dataList.add(data.getString(1));
                dataList.add(data.getString(2));
                dataList.add(data.getString(3));
                dataList.add(data.getString(4));
                dataList.add(data.getString(5));
            }
            data.close();
        }



    }
}