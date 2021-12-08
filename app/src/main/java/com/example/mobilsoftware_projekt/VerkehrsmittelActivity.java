package com.example.mobilsoftware_projekt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class VerkehrsmittelActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        ListView listview;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verkehrsmittel);

        listview = (ListView)findViewById(R.id.lv);
        ArrayList<String> arrayList = new ArrayList<>();
        setArrayList(arrayList);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,arrayList);
        listview.setAdapter(arrayAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Intent retrn = new Intent(VerkehrsmittelActivity.this,MainActivity.class);
                Toast.makeText(VerkehrsmittelActivity.this, "clicked item"+ position+ arrayList.get(position).toString(),Toast.LENGTH_SHORT).show();
                startActivity(retrn);
            }
        });
    }

    protected void setArrayList(ArrayList<String> list)
    {
        list.add("Fuß");
        list.add("Fahrrad");
        list.add("ÖPNV");
        list.add("E-Scooter");
        list.add("MIV-Fahrer");
        list.add("MIV-Mitfahrer");
        list.add("Sonstiges");
    }
}