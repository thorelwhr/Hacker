package com.example.mobilsoftware_projekt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VerkehrsmittelActivity extends AppCompatActivity
{

    public static  final String EXTRA_VM = "com.example.mobilsoftware_projekt.extra.VM";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verkehrsmittel);

        final ListView vm_listView = (ListView) findViewById(R.id.vm_listView);
        String[] values = new String[]{getString(R.string.vmFuß), getString(R.string.vmFahrrad), getString(R.string.vmOPNV),
                getString(R.string.vmMIVFahrer), getString(R.string.vmMIVMitfahrer), getString(R.string.vmSonstiges)};

        final ArrayList<String> vmList = new ArrayList<String>();
        for (int i = 0; i < values.length; ++i)
        {
            vmList.add(values[i]);
        }

        final StableArrayAdapter adapter = new StableArrayAdapter(this, android.R.layout.simple_list_item_1, vmList);
        vm_listView.setAdapter(adapter);

        vm_listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id)
            {
                final String item = (String) parent.getItemAtPosition(position);
                view.animate().setDuration(100).alpha(0)
                        .withEndAction(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Toast.makeText(VerkehrsmittelActivity.this, vmList.get(position) + " ausgewählt!", Toast.LENGTH_SHORT).show();;
                                String mChosenVm =vmList.get(position); // Position rauslesen
                                Intent mChosenVmIntent = new Intent(); // Neuer Intent
                                mChosenVmIntent.putExtra(EXTRA_VM, mChosenVm); //
                                setResult(RESULT_OK, mChosenVmIntent);
                                //Log.d(LOG_TAG, "End SecondActivity");
                                finish();
                            }
                        });
            }

        });
    }



    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }
}