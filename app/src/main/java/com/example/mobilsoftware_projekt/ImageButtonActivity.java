package com.example.mobilsoftware_projekt;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

public class ImageButtonActivity extends AppCompatActivity
{
    ArrayList<String> dataList;
    ListView listview = findViewById(R.id.ListView);
    DBHelper mDBHelper = new DBHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_button);



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
            listview = new ListView(this);

            while()
            {

            }

        }



    }
}