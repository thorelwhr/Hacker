package com.example.mobilsoftware_projekt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean permissionDenied = false;

    private GoogleMap map;
    private FloatingActionButton mTracking;
    private FloatingActionButton mVerkehrsmittel;

    //shared preferences
    private String lastMapStyle;
    private String restoredMapStyle;
    private boolean trafficEnabled = false;
    private boolean indoorEnabled = false;
    private boolean buildingEnabled = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(MainActivity.this, "Erlaubnis bereits erteilt!", Toast.LENGTH_SHORT).show();
        } else {
            requestFinePermission();
        }*/
        //retrieve settings
        SharedPreferences settings;
        settings = getSharedPreferences("SAVE_MAP_SETTINGS", Context.MODE_PRIVATE);
        restoredMapStyle = settings.getString("STYLE_OF_MAP", "MAP_TYPE_NORMAL)");
        trafficEnabled = settings.getBoolean("TRAFFIC_SHOWING_ON_MAP", false);
        buildingEnabled = settings.getBoolean("BUILDINGS_SHOWING_ON_MAP", false);
        indoorEnabled =settings.getBoolean("INDOOR_SHOWING_ON_MAP", false);
        lastMapStyle = restoredMapStyle;

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mTracking = (FloatingActionButton) findViewById(R.id.fab_tracking);
        mVerkehrsmittel = (FloatingActionButton)  findViewById(R.id.fab_verkehrsmittel);

        mTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mVerkehrsmittel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VerkehrsmittelActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        if(restoredMapStyle == null) {
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else if (restoredMapStyle.equals("MAP_TYPE_NORMAL")) {
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else if (restoredMapStyle.equals("MAP_TYPE_HYBRID")) {
            map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        } else if (restoredMapStyle.equals("MAP_TYPE_SATELLITE")) {
            map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else if (restoredMapStyle.equals("MAP_TYPE_TERRAIN")) {
            map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        } else {
            Log.i("LDA", "Error setting layer with name " + restoredMapStyle);
        }
        if (trafficEnabled == true){
            map.setTrafficEnabled(true);
        }
        if (buildingEnabled == true){
            map.setBuildingsEnabled(true);
        }
        if (indoorEnabled == true){
            map.setBuildingsEnabled(true);
        }

        /*if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED))
        {
            map.setMyLocationEnabled(true);
        }*/
        map.setOnMyLocationButtonClickListener(this);
        map.setOnMyLocationClickListener(this);
        enableMyLocation();
    }

    private void enableMyLocation() {
        if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)){
            if (map != null){
                map.setMyLocationEnabled(true);
            }
        }
        else{
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT)
                .show();
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    /*private void requestFinePermission()
    {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION))
        {
            new AlertDialog.Builder(this)
                    .setTitle("Hinweis")
                    .setMessage("Karte benötigt Zugriff auf Coarse")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            ActivityCompat.requestPermissions(MainActivity.this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                        }
                    })
                    .setNegativeButton("Abbruch", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        }

        else
        {
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }*/


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }
        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            enableMyLocation();
        } else {
            permissionDenied = true;
        }
    }


    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (permissionDenied) {
            showMissingPermissionError();
            permissionDenied = false;
        }
    }


    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences settings;
        settings = getApplicationContext().getSharedPreferences("SAVE_MAP_SETTINGS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("STYLE_OF_MAP", lastMapStyle);
        editor.putBoolean("TRAFFIC_SHOWING_ON_MAP", trafficEnabled);
        editor.putBoolean("BUILDINGS_SHOWING_ON_MAP", buildingEnabled);
        editor.putBoolean("INDOOR_SHOWING_ON_MAP", indoorEnabled);
        editor.apply();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences settings;
        settings = getApplicationContext().getSharedPreferences("SAVE_MAP_SETTINGS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("STYLE_OF_MAP", lastMapStyle);
        editor.putBoolean("TRAFFIC_SHOWING_ON_MAP", trafficEnabled);
        editor.putBoolean("BUILDINGS_SHOWING_ON_MAP", buildingEnabled);
        editor.putBoolean("INDOOR_SHOWING_ON_MAP", indoorEnabled);
        editor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (trafficEnabled == true){
            MenuItem item = menu.findItem(R.id.traffic_switch);
            item.setIcon(R.drawable.ic_checked_mark);
        }
        if (buildingEnabled == true){
            MenuItem item = menu.findItem(R.id.building_switch);
            item.setIcon(R.drawable.ic_checked_mark);
        }
        if (indoorEnabled == true){
            MenuItem item = menu.findItem(R.id.indoor_switch);
            item.setIcon(R.drawable.ic_checked_mark);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
                case R.id.normal_map:
                    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    lastMapStyle = "MAP_TYPE_NORMAL";
                    return true;
                case R.id.hybrid_map:
                    map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    lastMapStyle = "MAP_TYPE_HYBRID";
                    return true;
                case R.id.satellite_map:
                    map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    lastMapStyle = "MAP_TYPE_SATELLITE";
                    return true;
                case R.id.terrain_map:
                    map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    lastMapStyle = "MAP_TYPE_TERRAIN";
                    return true;
            case R.id.traffic_switch:
                if(trafficEnabled){
                    map.setTrafficEnabled(false);
                    item.setIcon(R.drawable.ic_unchecked_mark);
                    trafficEnabled = false;
                    return true;
                }
                else{
                    map.setTrafficEnabled(true);
                    item.setIcon(R.drawable.ic_checked_mark);
                    trafficEnabled = true;
                    return true;
                }
            case R.id.building_switch:
                if(buildingEnabled){
                    map.setBuildingsEnabled(false);
                    item.setIcon(R.drawable.ic_unchecked_mark);
                    buildingEnabled = false;
                    return true;
                }
                else{
                    map.setBuildingsEnabled(true);
                    item.setIcon(R.drawable.ic_checked_mark);
                    buildingEnabled = true;
                    return true;
                }
            case R.id.indoor_switch:
                if(indoorEnabled){
                    map.setIndoorEnabled(false);
                    item.setIcon(R.drawable.ic_unchecked_mark);
                    indoorEnabled = false;
                    return true;
                }
                else{
                    map.setIndoorEnabled(true);
                    item.setIcon(R.drawable.ic_checked_mark);
                    indoorEnabled = true;
                    return true;
                }
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
