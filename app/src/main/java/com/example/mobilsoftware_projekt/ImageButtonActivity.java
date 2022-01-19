package com.example.mobilsoftware_projekt;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

public class ImageButtonActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_button);

        Toolbar toolbar = findViewById(R.id.toolbar_fuer_ImageButtonActivity);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Trackdaten");

        ActionBar zuruck = getSupportActionBar();
        zuruck.setDisplayHomeAsUpEnabled(true);
    }
}