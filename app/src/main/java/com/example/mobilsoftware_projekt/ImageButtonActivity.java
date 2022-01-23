package com.example.mobilsoftware_projekt;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class ImageButtonActivity extends AppCompatActivity
{
    private ArrayList<String> dataList = new ArrayList<>();
    private ArrayList<String> mOrderedList = new ArrayList<>();
    private ArrayList<String> dataByID = new ArrayList<>();
    DBHelper mDBHelper = new DBHelper(this);
    private ImageButton mSaveButton;
    private String mSearchID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_button);

        ListView listview = (ListView) findViewById(R.id.listview);
        mSaveButton = findViewById(R.id.saveButton);
        Toolbar toolbar =findViewById(R.id.toolbar_fuer_ImageButtonActivity);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Trackdaten");

        ActionBar zuruck = getSupportActionBar();
        zuruck.setDisplayHomeAsUpEnabled(true);

        getData();

        mOrderArray();

        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mOrderedList);
        listview.setAdapter(arrayAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Set background of all items to white
                for (int i=0;i<parent.getChildCount();i++){
                    parent.getChildAt(i).setBackgroundColor(Color.WHITE);
                }
                view.setBackgroundColor(Color.DKGRAY);
                final String item = (String) parent.getItemAtPosition(position);
                mSearchID = item.substring(0,1);
                getDataByID();
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
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
                dataList.add(data.getString(6));
                dataList.add(data.getString(7));
            }
            data.close();
        }
    }

    private  void getDataByID(){
        Cursor data = mDBHelper.getDataByID(mSearchID);
        while(data.moveToNext())
        {
            dataByID.add(data.getString(0));
            dataByID.add(data.getString(1));
            dataByID.add(data.getString(2));
            dataByID.add(data.getString(3));
            dataByID.add(data.getString(4));
            dataByID.add(data.getString(5));
            dataByID.add(data.getString(6));
            dataByID.add(data.getString(7));
        }
        data.close();
    }

    public void mOrderArray(){
        String mID = "", mVerkehrsmittel = "", mDuration = "", mLength = "", mDate = "", mTrack = "";
        String mStart = "", mEnde = "";
        for(int i = 0; i < dataList.size(); i++){
            Log.d("TAG", "for-SChleife: "+ i);
            String mComplete, mFullyComplete;
            if (i % 8 == 0){
                mID = dataList.get(i) + ".: ";
            }
            else if (i% 8 == 1){
                mVerkehrsmittel = "Verkehrsmittel: " +dataList.get(i) + "; ";
            }
            else if(i%8==2){
                mDuration = "Dauer: " + dataList.get(i) + "; ";
            }
            else if(i%8==3){
                mLength = "Streckenlänge: " + dataList.get(i) + "; ";
            }
            else if(i%8==4){
                mDate = "Datum: " + dataList.get(i) + "; ";
            }
            else if(i%8==5){
                mTrack = "Weg: " + dataList.get(i) + "; ";
            }
            else if(i%8==6){
                mStart = "Wegstart: " + dataList.get(i) + "; ";
            }
            else {
                mEnde = "Wegende: " + dataList.get(i) + "; ";
                mComplete = mID + mVerkehrsmittel + mDuration + mLength + mDate + mStart + mEnde;
                mFullyComplete = mID + mVerkehrsmittel + mDuration + mLength + mDate + mTrack + mStart + mEnde;
                Log.d("TAG", mFullyComplete);
                mOrderedList.add(mComplete);
            }
        }
    }
}