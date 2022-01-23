package com.example.mobilsoftware_projekt;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class ImageButtonActivity extends AppCompatActivity
{
    ArrayList<String> dataList = new ArrayList<>();
    ArrayList<String> mOrderedList = new ArrayList<>();
    DBHelper mDBHelper = new DBHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_button);

        ListView listview = (ListView) findViewById(R.id.listview);
        Toolbar toolbar =findViewById(R.id.toolbar_fuer_ImageButtonActivity);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Trackdaten");

        ActionBar zuruck = getSupportActionBar();
        zuruck.setDisplayHomeAsUpEnabled(true);

        getData();

        mOrderArray();

        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mOrderedList);
        listview.setAdapter(arrayAdapter);
    }

    protected void getData()
    {
        if(mDBHelper != null)
        {
            Cursor data = mDBHelper.getData();
            ListView listview = new ListView(this);

            while(data.moveToNext())
            {
                dataList.add(data.getString(0));
                dataList.add(data.getString(1));
                dataList.add(data.getString(2));
                dataList.add(data.getString(3));
                dataList.add(data.getString(4));
                dataList.add(data.getString(5));
            }
            data.close();
        }
    }

    public void mOrderArray(){
        boolean mTimeToAddToArray = false;
        String mID = "", mVerkehrsmittel = "", mDuration = "", mLength = "", mDate = "", mTrack = "";
        for(int i = 0; i < dataList.size(); i++){
            Log.d("TAG", "for-SChleife: "+ i);
            String mComplete;
            if (i % 6 == 0){
                mID = dataList.get(i) + ".: ";
            }
            else if (i% 6 == 1){
                mVerkehrsmittel = "Verkehrsmittel: " +dataList.get(i) + "; ";
            }
            else if(i%6==2){
                mDuration = "Dauer: " + dataList.get(i) + "; ";
            }
            else if(i%6==3){
                mLength = "Streckenlänge: " + dataList.get(i) + "; ";
            }
            else if(i%6==4){
                mDate = "Datum: " + dataList.get(i) + "; ";
            }
            else {
                mTrack = "Weg: " + dataList.get(i) + "; ";
                mComplete = mID + mVerkehrsmittel + mDuration + mLength + mDate + mTrack;
                Log.d("TAG", mComplete);
                mOrderedList.add(mComplete);
            }
        }
    }
}