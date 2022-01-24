package com.example.mobilsoftware_projekt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ImageButtonActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    final String mForDir = "android-er";
    private ArrayList<String> dataList = new ArrayList<>();
    private ArrayList<String> mOrderedList = new ArrayList<>();
    private ArrayList<String> dataByID = new ArrayList<>();
    DBHelper mDBHelper = new DBHelper(this);
    private ImageButton mSaveButton;
    private String mSearchID = "";
    private String id, verkehrsmittel, duration, length, date, track, start, ende;
    private int mFileNumber = 1;
    private String json = "";
    private boolean mListClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_button);

        ListView listview = (ListView) findViewById(R.id.listview);
        mSaveButton = (ImageButton) findViewById(R.id.saveButton);
        Toolbar toolbar =findViewById(R.id.toolbar_fuer_ImageButtonActivity);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Trackdaten");

        ActionBar zuruck = getSupportActionBar();
        zuruck.setDisplayHomeAsUpEnabled(true);

        //retrieve settings

        SharedPreferences settings;
        settings = getSharedPreferences("SAVE_NUMBER_FILE", Context.MODE_PRIVATE);
        mFileNumber = settings.getInt("FILE_NUMBER", 1);

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
                mListClicked = true;
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListClicked) {
                    if (ContextCompat.checkSelfPermission(ImageButtonActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        // Permission is not granted
                        // Should we show an explanation?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                ImageButtonActivity.this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            // Show an explanation to the user *asynchronously* -- don't block
                            // this thread waiting for the user's response! After the user
                            // sees the explanation, try again to request the permission.

                            //to simplify, call requestPermissions again
                            Toast.makeText(getApplicationContext(),
                                    "shouldShowRequestPermissionRationale",
                                    Toast.LENGTH_LONG).show();
                            ActivityCompat.requestPermissions(ImageButtonActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                        }
                        else {
                            // No explanation needed; request the permission
                            ActivityCompat.requestPermissions(ImageButtonActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                        }
                    }
                    else{
                        // permission granted
                        mAssignValues();
                        Log.d("TAG", id + verkehrsmittel + duration + length + date + track + start + ende);
                        Gson gson = new Gson();
                        GsonClass mDataToGSON = new GsonClass(id, verkehrsmittel, duration, length, date, track, start, ende);
                        json = gson.toJson(mDataToGSON);
                        Log.d("TAG", "JSON String ist: " + json);
                        try {
                            storeFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                else{
                    Toast.makeText(ImageButtonActivity.this, "Bitte wähle eine Datei aus," +
                            " sollten keine Dateien auswählbar seien füge diese via Tracking hinzu.", Toast.LENGTH_SHORT).show();
                }
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
            Log.d("TAG", "for-Schleife: "+ i);
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

    public void  mAssignValues(){
        for(int i = 0; i < dataByID.size(); i++) {
            Log.d("TAG", "for-Schleife: " + i);
            if (i % 8 == 0) {
                id = dataByID.get(i);
            } else if (i % 8 == 1) {
                verkehrsmittel = dataByID.get(i);
            } else if (i % 8 == 2) {
                duration = dataByID.get(i);
            } else if (i % 8 == 3) {
                length = dataByID.get(i);
            } else if (i % 8 == 4) {
                date = dataByID.get(i);
            } else if (i % 8 == 5) {
                track = dataByID.get(i);
            } else if (i % 8 == 6) {
                start = dataByID.get(i);
            } else {
                ende = dataByID.get(i);
            }
        }
    }

    private void storeFile() throws IOException {
        String fileName, filePath, fileContent;
        //generate a unique file name from timestamp
        Date date = new Date();
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("yyMMdd-hhmmss-SSS");
        //Get your FilePath and use it to create your File
        fileName = "MyTrackData" + mFileNumber + "_"+ simpleDateFormat.format(new Date()) + ".txt";
        fileContent = json;
        File dir = getTrackStorageDir(mForDir);
        filePath = String.valueOf(dir);
        File yourFile = new File(filePath, fileName);

        FileOutputStream fileOutputStream = null;
        try {
            //Create your FileOutputStream, yourFile is part of the constructor
            fileOutputStream = new FileOutputStream(yourFile);
            //Convert your JSON String to Bytes and write() it
            fileOutputStream.write(fileContent.getBytes());
            //Finally flush and close your FileOutputStream
            fileOutputStream.flush();
            fileOutputStream.close();
            Toast.makeText(ImageButtonActivity.this, "File saved: \n" +
                    yourFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            mFileNumber ++;
            Log.d("TAG", String.valueOf(mFileNumber));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    "FileNotFoundException: \n" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    "IOException: \n" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    public File getTrackStorageDir(String name) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), name);
        if (file.mkdirs()) {
            //if directory not exist
            Toast.makeText(getApplicationContext(),
                    file.getAbsolutePath() + " created",
                    Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(getApplicationContext(),
                    "Directory not created", Toast.LENGTH_LONG).show();
        }
        return file;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if(requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE){
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted.
                Toast.makeText(getApplicationContext(),
                        "permission was granted, thx:)",
                        Toast.LENGTH_LONG).show();
            } else {
                // permission denied.
                Toast.makeText(getApplicationContext(),
                        "permission denied! Oh:(",
                        Toast.LENGTH_LONG).show();
            }
            return;
        }else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //------------- Lifecycle --------------------------------------

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("TAG", "onStart() gestartet");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("TAG", "onResume() gestartet");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("TAG", "onResume() gestartet");
    }

    @Override
    protected void onStop() {
        Log.d("TAG", "onStop() gestartet");
        super.onStop();

        SharedPreferences settings;
        settings = getApplicationContext().getSharedPreferences("SAVE_FILE_NUMBER", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("FILE_NUMBER", mFileNumber);
        editor.apply();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("TAG", "onSaveInstanceState() gestartet");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d("TAG", "onLowMemory() gestartet");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("TAG", "onDestroy() gestartet");

        SharedPreferences settings;
        settings = getApplicationContext().getSharedPreferences("SAVE_FILE_NUMBER", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("FILE_NUMBER", mFileNumber);
        editor.apply();
    }
}