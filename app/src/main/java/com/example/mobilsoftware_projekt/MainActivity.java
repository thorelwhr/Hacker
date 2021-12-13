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
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    public static final int TEXT_REQUEST = 1; // Für Verkehrsmittelauswahl, Funktion wie bei Permission
    private static final int DEFAULT_UPDATE_INTERVALL = 10; //best practice; not necessary
    private static final int FASTEST_UPDATE_INTERVALL = 5;
    private boolean permissionDenied = false;
    private boolean isTracking = false;

    private GoogleMap map;
    private FloatingActionButton mTracking;
    private FloatingActionButton mVerkehrsmittel;

    /*shared preferences
    private String lastMapStyle;
    private String restoredMapStyle;
    private boolean trafficEnabled = false;
    private boolean indoorEnabled = false;
    private boolean buildingEnabled = false;*/

    //Google API for location services
    FusedLocationProviderClient fusedLocationProviderClient;

    //Location request config file for all settings related to FusedLocationProvider
    private LocationRequest locationRequest;

    private LocationCallback locationCallback;

    private Location mCurrentLocation;
    private Location mLastLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        /* mit PermissionUtils nicht mehr nötig

        if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(MainActivity.this, "Erlaubnis bereits erteilt!", Toast.LENGTH_SHORT).show();
        } else {
            requestFinePermission();
        }*/

        //retrieve settings

        /*SharedPreferences settings;
        settings = getSharedPreferences("SAVE_MAP_SETTINGS", Context.MODE_PRIVATE);
        restoredMapStyle = settings.getString("STYLE_OF_MAP", "MAP_TYPE_NORMAL)");
        trafficEnabled = settings.getBoolean("TRAFFIC_SHOWING_ON_MAP", false);
        buildingEnabled = settings.getBoolean("BUILDINGS_SHOWING_ON_MAP", false);
        indoorEnabled =settings.getBoolean("INDOOR_SHOWING_ON_MAP", false);
        lastMapStyle = restoredMapStyle;*/

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mTracking = (FloatingActionButton) findViewById(R.id.fab_tracking);
        mVerkehrsmittel = (FloatingActionButton)  findViewById(R.id.fab_verkehrsmittel);

        //set all properties of LocationRequest
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVALL);
        locationRequest.setFastestInterval(1000 * FASTEST_UPDATE_INTERVALL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //For continuous location Updates:
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                updateLocationValues(locationResult.getLastLocation());
            }
        };

        mTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isTracking) {
                    isTracking = true;
                    Toast.makeText(MainActivity.this, "Start tracking", Toast.LENGTH_SHORT).show();
                    mTracking.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_stop));
                    mTracking.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.red)));
                    mVerkehrsmittel.setClickable(false);
                    // Daten an Polyline-Funktion übergeben
                }
                else {
                    isTracking = false;
                    Toast.makeText(MainActivity.this, "Stop tracking", Toast.LENGTH_SHORT).show();
                    mTracking.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_start));
                    mTracking.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.green)));
                    mVerkehrsmittel.setClickable(true);
                    //Polyline -Funktion beenden
                }
            }
        });

        mVerkehrsmittel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VerkehrsmittelActivity.class);
                startActivityForResult(intent, TEXT_REQUEST);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        // davor gespeicherte Karteneinstellungen werden hier wieder aufgerufen:

        /*if(restoredMapStyle == null) {
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
        }*/

        map.setOnMyLocationButtonClickListener(this);
        map.setOnMyLocationClickListener(this);
        enableMyLocation();
    }

    private void enableMyLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)){

            // Für Null-Pointer-Exception:
            if (map != null) {
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(@NonNull Location location) {
                        //Got last known location apparently can be null in rare instances
                        //Put Values of location into UI
                        if (location != null) {
                            updateLocationValues(location);
                        }
                    }
                });
                map.setMyLocationEnabled(true);
            }
        }
        else {
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
    }

    private void updateLocationValues(Location location) {
        //update with new location
        if(mCurrentLocation != null){
            mLastLocation = mCurrentLocation;
        }
        if(location != null){
            mCurrentLocation = location;
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), 4));
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    /* mit PermissionUtils nicht mehr nötig

    private void requestFinePermission()
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

        // Zugriff bereits gewährt
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        // Zugriff wird gewährt
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
        // Wenn Zugriff verweigert wird: Error Message
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
    public void  onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Wenn request angenommen wurde:
        if (requestCode == TEXT_REQUEST) {

            // Wenn ich ein Ergebnis habe:
            if (resultCode == RESULT_OK) {
                String verkehrsmittel = data.getStringExtra(VerkehrsmittelActivity.EXTRA_VM);
                Toast.makeText(this, verkehrsmittel, Toast.LENGTH_SHORT).show();
                mVerkehrsmittel = findViewById(R.id.fab_verkehrsmittel);

                if (verkehrsmittel.equals(getString(R.string.vmFuß))) {
                    mVerkehrsmittel.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_fussgaenger));
                }
                if (verkehrsmittel.equals(getString(R.string.vmFahrrad))) {
                    mVerkehrsmittel.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_fahrrad));
                }
                if (verkehrsmittel.equals(getString(R.string.vmMIVFahrer))) {
                    mVerkehrsmittel.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_auto));
                }
                if (verkehrsmittel.equals(getString(R.string.vmMIVMitfahrer))) {
                    mVerkehrsmittel.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_mitfahrer));
                }
                if (verkehrsmittel.equals(getString(R.string.vmOPNV))) {
                    mVerkehrsmittel.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_opnv));
                }
                if (verkehrsmittel.equals(getString(R.string.vmSonstiges))) {
                    mVerkehrsmittel.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_sonstiges));
                }
            }
        }
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
        /*SharedPreferences settings;
        settings = getApplicationContext().getSharedPreferences("SAVE_MAP_SETTINGS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("STYLE_OF_MAP", lastMapStyle);
        editor.putBoolean("TRAFFIC_SHOWING_ON_MAP", trafficEnabled);
        editor.putBoolean("BUILDINGS_SHOWING_ON_MAP", buildingEnabled);
        editor.putBoolean("INDOOR_SHOWING_ON_MAP", indoorEnabled);
        editor.apply();*/
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

        /*SharedPreferences settings;
        settings = getApplicationContext().getSharedPreferences("SAVE_MAP_SETTINGS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("STYLE_OF_MAP", lastMapStyle);
        editor.putBoolean("TRAFFIC_SHOWING_ON_MAP", trafficEnabled);
        editor.putBoolean("BUILDINGS_SHOWING_ON_MAP", buildingEnabled);
        editor.putBoolean("INDOOR_SHOWING_ON_MAP", indoorEnabled);
        editor.apply();*/
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_menu, menu);
        return true;
    }*/

    /*@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (trafficEnabled == true) {
            MenuItem item = menu.findItem(R.id.traffic_switch);
            item.setIcon(R.drawable.ic_checked_mark);
        }
        if (buildingEnabled == true) {
            MenuItem item = menu.findItem(R.id.building_switch);
            item.setIcon(R.drawable.ic_checked_mark);
        }
        if (indoorEnabled == true) {
            MenuItem item = menu.findItem(R.id.indoor_switch);
            item.setIcon(R.drawable.ic_checked_mark);
        }
        return super.onPrepareOptionsMenu(menu);
    }*/

    /*@Override
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
                if(trafficEnabled) {
                    map.setTrafficEnabled(false);
                    item.setIcon(R.drawable.ic_unchecked_mark);
                    trafficEnabled = false;
                    return true;
                }
                else {
                    map.setTrafficEnabled(true);
                    item.setIcon(R.drawable.ic_checked_mark);
                    trafficEnabled = true;
                    return true;
                }
            case R.id.building_switch:
                if(buildingEnabled) {
                    map.setBuildingsEnabled(false);
                    item.setIcon(R.drawable.ic_unchecked_mark);
                    buildingEnabled = false;
                    return true;
                }
                else {
                    map.setBuildingsEnabled(true);
                    item.setIcon(R.drawable.ic_checked_mark);
                    buildingEnabled = true;
                    return true;
                }
            case R.id.indoor_switch:
                if(indoorEnabled) {
                    map.setIndoorEnabled(false);
                    item.setIcon(R.drawable.ic_unchecked_mark);
                    indoorEnabled = false;
                    return true;
                }
                else {
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
    }*/
}
