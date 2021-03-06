package com.example.mobilsoftware_projekt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 99;
    public static final int TEXT_REQUEST = 1; // F??r Verkehrsmittelauswahl, Funktion wie bei Permission
    private static final int DEFAULT_UPDATE_INTERVALL = 10; //best practice; not necessary
    private static final int FASTEST_UPDATE_INTERVALL = 1;
    private static final float MAP_STANDARD_ZOOM = 10f;
    private boolean permissionDenied = false;
    private boolean isTracking = false;
    private boolean mDidCheckPermission;

    private GoogleMap map;
    private FloatingActionButton mTracking;
    private FloatingActionButton mVerkehrsmittel;
    private ImageButton mImageButton;

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
    private boolean locationCallbackCalled = false;

    private Location mCurrentLocation;
    private Location mLastLocation;

    private Address mCurrentAddress;

    private ArrayList<Location> mTrackedPath;
    private ArrayList<LatLng> mPolylinePoints;

    private String mCameraSettings = "Standard";
    private String vm = "Fu??";
    private String mStartort;
    private String mEndort;

    DBHelper mDBHelper = new DBHelper(this);

    //Timer Stuff:
    private long startTime = 0;
    private String mTrackingDuration;

    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            //Log.d("TAG", String.valueOf(System.currentTimeMillis()));
            long millis = System.currentTimeMillis() - startTime;
            //Log.d("TAG", String.valueOf(millis));
            int seconds = (int) (millis / 1000);
            //Log.d("TAG", String.valueOf(seconds));
            int minutes = seconds / 60;
            //Log.d("TAG", String.valueOf(minutes));
            int hours = minutes/60;
            seconds = seconds % 60;
            minutes = minutes % 60;
            //Log.d("TAG", String.valueOf(seconds));

            mTrackingDuration = String.format("%d:%02d:%02d", hours, minutes, seconds);

            timerHandler.postDelayed(this, 500);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        Log.d("TAG", "LocationProvider l??uft --------");

        //retrieve settings

        SharedPreferences settings;
        settings = getSharedPreferences("SAVE_MAP_SETTINGS", Context.MODE_PRIVATE);
        mDidCheckPermission = settings.getBoolean("checkedPermission", false);
        /*restoredMapStyle = settings.getString("STYLE_OF_MAP", "MAP_TYPE_NORMAL)");
        trafficEnabled = settings.getBoolean("TRAFFIC_SHOWING_ON_MAP", false);
        buildingEnabled = settings.getBoolean("BUILDINGS_SHOWING_ON_MAP", false);
        indoorEnabled =settings.getBoolean("INDOOR_SHOWING_ON_MAP", false);
        lastMapStyle = restoredMapStyle;*/

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mTracking = (FloatingActionButton) findViewById(R.id.fab_tracking);
        mVerkehrsmittel = (FloatingActionButton) findViewById(R.id.fab_verkehrsmittel);
        mImageButton = (ImageButton) findViewById(R.id.Imagebutton);

        mLocationCallback();
        Toolbar toolbar = findViewById(R.id.neue_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("MyJournal");


        mTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mGeocodeLocations(mCurrentLocation);

                if (!isTracking) {
                    isTracking = true;
                    startTime = System.currentTimeMillis();
                    timerHandler.postDelayed(timerRunnable, 0);
                    if (mCurrentAddress != null) {
                        Toast.makeText(MainActivity.this, "Start tracking at: " +
                                mCurrentAddress.getAddressLine(0), Toast.LENGTH_SHORT).show();
                        mStartort = mCurrentAddress.getAddressLine(0);
                    } else {
                        Toast.makeText(MainActivity.this, "Start tracking", Toast.LENGTH_SHORT).show();
                        int steal = mPolylinePoints.size() - 1;
                        mStartort = String.valueOf(mPolylinePoints.get(steal));
                    }
                    mTracking.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_stop));
                    mTracking.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.red)));
                    mVerkehrsmittel.setClickable(false);
                    // Daten an Polyline-Funktion ??bergeben
                    mPolylinePoints = new ArrayList<LatLng>();
                    drawPolyline();

                } else {
                    isTracking = false;
                    timerHandler.removeCallbacks(timerRunnable);
                    if (mCurrentAddress != null) {
                        Toast.makeText(MainActivity.this, "Stop tracking at: " +
                                mCurrentAddress.getAddressLine(0), Toast.LENGTH_SHORT).show();
                        mEndort = mCurrentAddress.getAddressLine(0);
                    } else {
                        Toast.makeText(MainActivity.this, "Stop tracking", Toast.LENGTH_SHORT).show();
                        int steal = mPolylinePoints.size() - 1;
                        mEndort = String.valueOf(mPolylinePoints.get(steal));
                    }
                    mTracking.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_start));
                    mTracking.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.green)));
                    mVerkehrsmittel.setClickable(true);

                    //Daten an Datenbank ??bergeben
                    mAddToDatatabase();

                    //Polyline -Funktion beenden
                    deletePolyline();
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

        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ImageButtonActivity.class);
                startActivity(intent);
            }
        });
    }

    //-------------------- Karte ----------------------------------------
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("TAG", "onMapReady() gestartet");
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
            GoogleMap.getUiSettings().setIndoorLevelPickerEnabled(true);
        }*/

        map.setOnMyLocationButtonClickListener(this);
        Log.d("TAG", "onMyLocationButton sollte angezeigt werden");
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setOnMyLocationClickListener(this);
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                mCameraSettings = getString(R.string.camera_adjZ);
            }
        });

        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng latLng) {
                mCameraSettings = getString(R.string.camera_free);
            }
        });

        enableMyLocation();
        startLocationUpdates();
    }

    //-------------------Location------------------------------------------
    private void enableMyLocation() {
        Log.d("TAG", "enableLocation() gestartet --------");


        if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {

            // F??r Null-Pointer-Exception:
            if (map != null) {
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Log.d("TAG", "looking for last location...");
                        //Got last known location apparently can be null in rare instances
                        //Put Values of location into UI
                        if (location != null) {
                            Log.d("TAG", location.toString());
                            Log.d("TAG", "found last location");
                            updateLocationValues(location);
                        } else {
                            Log.d("TAG", "could not find location");
                            Toast.makeText(MainActivity.this, "Keine Standortdaten gefunden, bitte " +
                                    "??berpr??fe deine Einstellungen", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                map.setMyLocationEnabled(true);
            }
        } else {
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
            Log.d("TAG", "doooooooooone ---------------------------");
        }
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        //turn on continuous location Tracking
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        Log.d("TAG", "Standort Update gestartet");
        enableMyLocation();
    }

    private void stopLocationUpdates() {
        //turn off location tracking
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void mLocationCallback() {
        Log.d("TAG", "mLocationUpdate() gestartet");
        //set all properties of LocationRequest
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVALL);
        locationRequest.setFastestInterval(1000 * FASTEST_UPDATE_INTERVALL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //For continuous location Updates, is triggered whenever the update interval is met:
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Log.d("TAG", "LocationCallback() got a result");
                //save location
                updateLocationValues(locationResult.getLastLocation());
                //locationCallbackCalled = true;
            }
        };
        Log.d("TAG", "mLocationCallback() am Ende");
    }

    private void updateLocationValues(Location location) {
        //update with new location
        if(mCurrentLocation != null){
            mLastLocation = mCurrentLocation;
            Log.d("TAG", "letzter Standort:" + mLastLocation);
        }
        if(location != null){
            mCurrentLocation = location;
            Log.d("TAG", "Standort aktualisiert :" + mCurrentLocation);
        }
        else {
            Log.d("TAG", "aktueller Standort konnte nicht gefunden werden");
        }

        //Kamera fokussiert sich je nach Einstellung wieder auf aktuellen Standort oder nicht
        if(mCameraSettings.equals(getString(R.string.camera_standard))) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(),
                    mCurrentLocation.getLongitude()), MAP_STANDARD_ZOOM));
            Log.d("TAG", "Kamera folgt Standort mit Standardzoom");
        } else if (mCameraSettings.equals(getString(R.string.camera_adjZ))){
            float zoom = map.getCameraPosition().zoom;
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(),
                    mCurrentLocation.getLongitude()), zoom));
            Log.d("TAG", "Kamera folgt Standort mit angepasstem Zoom");
        } else if (mCameraSettings.equals(getString(R.string.camera_free))){
            Log.d("TAG", "Kamera folgt Standort nicht - ist frei");
        } else {
            Toast.makeText(this, "Da ist etwas schiefgelaufen - die " +
                    "Kamera stellt sich nicht mehr auf den aktuellen Standort ein", Toast.LENGTH_SHORT).show();
            Log.d("TAG", "Toll jetzt ist etwas mit der Kamera schiefgelaufen");
        }
        /*if(!locationCallbackCalled){
            startLocationUpdates();
        }*/
        if(isTracking){
            drawPolyline();
        }
    }

    private void mGeocodeLocations(Location location) {
        //separated cause this slows the app down significantly
        //use runOnUiThread bc it's a heavy task - no clue if it actually makes a difference
        //call this as few times as possible, if no address is found app will slow down significantly
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("TAG", "Geocoder gestartet");
                Geocoder geocoder = new Geocoder(MainActivity.this);
                try{
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(),1);
                    mCurrentAddress = addresses.get(0);
                    Log.d("TAG", "Geocoder sucht...");
                }
                catch (Exception e){
                    Log.d("TAG", "Geocoder hat verkackt");
                    //do nothing or bad stuff will happen; unless you know what you're doing - but I most certainly have no clue
                }
            }
        });

    }

    @Override
    public boolean onMyLocationButtonClick() {
        //Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        mCameraSettings = getString(R.string.camera_standard);
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }



    // --------- Permissions------------------------------------------------------
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Zugriff bereits gew??hrt
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            Log.d("TAG", "Zugriff bereits gew??hrt--------------------------");
            mDidCheckPermission = true;
            if(mDidCheckPermission){
                Log.d("TAG", "mDidPermissionCheck = true");
            }
            return;
        }

        // Zugriff wird gew??hrt
        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionDenied = false; //wird hier warum auch immer sonst als true gesetzt --> Absturz bei Erstinstallation
            Log.d("TAG", "Zugriff wird gew??hrt--------------------------");
            mDidCheckPermission = true;
            if(mDidCheckPermission){
                Log.d("TAG", "mDidPermissionCheck = true");
            }
            enableMyLocation();
            //mLocationCallback();
        } else {
            Log.d("TAG", "Zugriff nicht gew??hrt--------------------------");
            mDidCheckPermission = true;
            if(mDidCheckPermission){
                Log.d("TAG", "mDidPermissionCheck = true");
            }
            permissionDenied = true;
        }
        mDidCheckPermission = true;
        /*if(mDidCheckPermission){
            Log.d("TAG", "mDidPermissionCheck = true");
        }*/
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
        Log.d("TAG", "MissingPermissionError()-------------");
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }


    //------------------- Requests & Sonstiges-----------------------------------
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // Wenn request angenommen wurde:
        if (requestCode == TEXT_REQUEST) {

            // Wenn ich ein Ergebnis habe:
            if (resultCode == RESULT_OK) {
                String verkehrsmittel = data.getStringExtra(VerkehrsmittelActivity.EXTRA_VM);
                Toast.makeText(this, verkehrsmittel, Toast.LENGTH_SHORT).show();
                mVerkehrsmittel = findViewById(R.id.fab_verkehrsmittel);

                if (verkehrsmittel.equals(getString(R.string.vmFu??))) {
                    mVerkehrsmittel.setImageDrawable(ContextCompat.getDrawable
                            (getApplicationContext(), R.drawable.ic_fussgaenger));
                    vm = getString(R.string.vmFu??);
                }
                if (verkehrsmittel.equals(getString(R.string.vmFahrrad))) {
                    mVerkehrsmittel.setImageDrawable(ContextCompat.getDrawable
                            (getApplicationContext(), R.drawable.ic_fahrrad));
                    vm = getString(R.string.vmFahrrad);
                }
                if (verkehrsmittel.equals(getString(R.string.vmMIVFahrer))) {
                    mVerkehrsmittel.setImageDrawable(ContextCompat.getDrawable
                            (getApplicationContext(), R.drawable.ic_auto));
                    vm = getString(R.string.vmMIVFahrer);
                }
                if (verkehrsmittel.equals(getString(R.string.vmMIVMitfahrer))) {
                    mVerkehrsmittel.setImageDrawable(ContextCompat.getDrawable
                            (getApplicationContext(), R.drawable.ic_mitfahrer));
                    vm = getString(R.string.vmMIVMitfahrer);
                }
                if (verkehrsmittel.equals(getString(R.string.vmOPNV))) {
                    mVerkehrsmittel.setImageDrawable(ContextCompat.getDrawable
                            (getApplicationContext(), R.drawable.ic_opnv));
                    vm = getString(R.string.vmOPNV);
                }
                if (verkehrsmittel.equals(getString(R.string.vmSonstiges))) {
                    mVerkehrsmittel.setImageDrawable(ContextCompat.getDrawable
                            (getApplicationContext(), R.drawable.ic_sonstiges));
                    vm = getString(R.string.vmSonstiges);
                }
            }
        }
    }

    private void drawPolyline() {
            double mCurrentLat = mCurrentLocation.getLatitude();
            double mCurrentLong = mCurrentLocation.getLongitude();
            LatLng currentLatLng = new LatLng(mCurrentLat, mCurrentLong);
            Polyline polyline = null;

            //Populate ArrayLists - constantly
            /*mTrackedPath = new ArrayList<Location>();
            mTrackedPath.add(mCurrentLocation);
            Log.d("TAG", "Array: " + String.valueOf(mTrackedPath));*/
            if(mPolylinePoints.isEmpty()) {
                mPolylinePoints.add(currentLatLng);
            }
            else{
                int j = mPolylinePoints.size() -1;
                if(!mPolylinePoints.get(j).equals(currentLatLng)){
                    mPolylinePoints.add(currentLatLng);
                }
                Log.d("TAG", "aktuelle Location: " + mPolylinePoints.toString());
                Log.d("TAG", "-------------- " + mPolylinePoints.toString());
            }
            //Polyline zeichnen:
            for (int i = 0; i < mPolylinePoints.size(); i++) {
                polyline = map.addPolyline(new PolylineOptions()
                        .add(mPolylinePoints.toArray(new LatLng[i]))
                        .color(R.color.blue));
                Log.d("TAG", "Neuer Punkt an " + mPolylinePoints.get(i) + " hinzugef??gt");
            }
    }

    private void  deletePolyline(){
        //mTrackedPath.clear();
        //Log.d("TAG", "Array: " + String.valueOf(mTrackedPath));
        mPolylinePoints.clear();
        map.clear();
        Log.d("TAG", "Verbleibende Werte in mPolylinePoints: " + String.valueOf(mPolylinePoints.size()));
    }

    //------------- Lifecycle --------------------------------------

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("TAG", "onStart() gestartet");
        if(mDidCheckPermission) {
            Log.d("TAG", "onStart() if-Schleife l??uft");
            mLocationCallback();
            enableMyLocation();
            startLocationUpdates();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("TAG", "onResume() gestartet");
        if(mDidCheckPermission) {
            Log.d("TAG", "onResume() if-Schleife l??uft");
            mLocationCallback();
            enableMyLocation();
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("TAG", "onResume() gestartet");
        stopLocationUpdates();
    }

    @Override
    protected void onStop() {
        Log.d("TAG", "onStop() gestartet");
        super.onStop();
        stopLocationUpdates();
        SharedPreferences settings;
        settings = getApplicationContext().getSharedPreferences("SAVE_MAP_SETTINGS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        /*editor.putString("STYLE_OF_MAP", lastMapStyle);
        editor.putBoolean("TRAFFIC_SHOWING_ON_MAP", trafficEnabled);
        editor.putBoolean("BUILDINGS_SHOWING_ON_MAP", buildingEnabled);
        editor.putBoolean("INDOOR_SHOWING_ON_MAP", indoorEnabled);*/
        editor.putBoolean("checkedPermission", mDidCheckPermission);
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
        stopLocationUpdates();

        SharedPreferences settings;
        settings = getApplicationContext().getSharedPreferences("SAVE_MAP_SETTINGS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        /*editor.putString("STYLE_OF_MAP", lastMapStyle);
        editor.putBoolean("TRAFFIC_SHOWING_ON_MAP", trafficEnabled);
        editor.putBoolean("BUILDINGS_SHOWING_ON_MAP", buildingEnabled);
        editor.putBoolean("INDOOR_SHOWING_ON_MAP", indoorEnabled);*/
        editor.putBoolean("checkedPermission", mDidCheckPermission);
        editor.apply();
    }

    //---------------- Database stuff ------------------------------------
    public void mAddToDatatabase(){
        Log.d("TAG", "mAddToDatabase() gestartet");
        String mDistance = calcLengthOfTrack();
        Log.d("TAG", mDistance);
        String mDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        Log.d("TAG", vm + "; " + mTrackingDuration + "; " + mDistance + "; " + mDate + "; " + mPolylinePoints);
        mDBHelper.addData(vm, mTrackingDuration, mDistance, mDate, String.valueOf(mPolylinePoints), mStartort, mEndort);
    }

    public String calcLengthOfTrack(){
        float calcLengthHelper = 0;
        String mLengthOfTrack;

        LatLng latlng1 = null;
        LatLng latlng2 = null;

        for(int i = 0; i < mPolylinePoints.size(); i++){
            latlng2 = mPolylinePoints.get(i);
            if(latlng1 != null){
                Location loc1 = new Location("");
                loc1.setLatitude(latlng1.latitude);
                loc1.setLongitude(latlng1.longitude);

                Location loc2 = new Location("");
                loc2.setLatitude(latlng2.latitude);
                loc2.setLongitude(latlng2.longitude);

                calcLengthHelper = loc1.distanceTo(loc2) + calcLengthHelper;
                Log.d("TAG", String.valueOf(calcLengthHelper));
            }
            latlng1 = latlng2;
        }

        if(calcLengthHelper < 1000){
            NumberFormat numberFormat = new DecimalFormat("0.00");
            numberFormat.setRoundingMode(RoundingMode.HALF_EVEN);
            mLengthOfTrack = String.valueOf(numberFormat.format(calcLengthHelper)) + " m";
        }
        else {
            float l = calcLengthHelper/1000;
            NumberFormat numberFormat = new DecimalFormat("0.00");
            numberFormat.setRoundingMode(RoundingMode.HALF_EVEN);
            mLengthOfTrack = String.valueOf(numberFormat.format(l)) + " km";
        }
        return mLengthOfTrack;
        /*sollte aus irgendeinem Grund der wahre float Wert von der Distanz n??tig werden einfach
          obiges return auskommentieren und das hier unten verwenden: */
        //return String.valueOf(calcLengthHelper);
    }

    //--------------- Menu-settings -----------------------------------------

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
                    GoogleMap.getUiSettings().setIndoorLevelPickerEnabled(false);
                    item.setIcon(R.drawable.ic_unchecked_mark);
                    indoorEnabled = false;
                    return true;
                }
                else {
                    map.setIndoorEnabled(true);
                    GoogleMap.getUiSettings().setIndoorLevelPickerEnabled(true);
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
