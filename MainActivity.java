package com.example.minigps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;


import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_FINE_LOCATION = 99;

    TextView tv_lat, tv_lon, tv_accuracy, tv_alttitude, tv_speed, tv_sensor, tv_updates, tv_address,tv_labelCrumbCounter,tv_countOfCrumb;
    Button btn_newWayPoint,btn_showWayPointList,btn_showMap;
    Switch sw_locationupdates, sw_gps;
    FusedLocationProviderClient fusedLocationProviderClient;
    boolean updateOn = false;
    Location currentLocation;
    List<Location> savedLocations;
    LocationCallback locationCallBack;
    int locationFastestInterval = 5000;
    int locationMaxWaitTime = 45000;
    int locationInterval;


    //location request
    LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_alttitude = findViewById(R.id.tv_alttitude);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_speed = findViewById(R.id.tv_speed);
        tv_address = findViewById(R.id.tv_address);
        sw_gps = findViewById(R.id.sw_gps);
        sw_locationupdates = findViewById(R.id.sw_locationsupdates);
        tv_updates = findViewById(R.id.tv_updates);
        tv_labelCrumbCounter = findViewById(R.id.tv_labelCrumbCounter);
        tv_countOfCrumb=findViewById(R.id.tv_countOfCrumb);
        btn_newWayPoint=findViewById(R.id.btn_newWayPoint);
        btn_showWayPointList=findViewById(R.id.btn_showWayPointList);
        btn_showMap=findViewById(R.id.btn_showMap);
        //location properties
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, locationInterval)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(locationFastestInterval)
                .setMaxUpdateDelayMillis(locationMaxWaitTime)
                .build();
        locationCallBack=new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                updateUIvalues(locationResult.getLastLocation());
            }
        };
        btn_newWayPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApplication myApplication=(MyApplication)getApplicationContext();
                savedLocations=myApplication.getMyLocations();
                savedLocations.add(currentLocation);
            }

        });
        btn_showWayPointList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ShowSavedLocationsList.class);
                startActivity(i);

            }
        });
        btn_showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,MapsActivity.class);
                startActivity(i);

            }
        });
        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sw_gps.isChecked()) {
                    tv_sensor.setText("using Gps sensor");
                } else {
                    tv_sensor.setText("using tower sensor+wifi");
                }
            }
        });
        sw_locationupdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sw_locationupdates.isChecked()){
                    startLocationUpdates();
                }
                else {
                    stopLocationUpdates();
                }
            }
        });
        updateGPS();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateGPS();
                } else {
                    Toast.makeText(this, "this app requires permission to be be granted in order to work properly", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    private void updateGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    updateUIvalues(location);
                    currentLocation=location;

                }
            });
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
        }
    }
    private void startLocationUpdates(){
        tv_updates.setText("Locations is being tracked");
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
        updateGPS();
    }
    private void stopLocationUpdates(){
        tv_updates.setText("location is not being tracked");
        tv_lat.setText("not tracking location");
        tv_lon.setText("not ttracking location");
        tv_speed.setText("not tracking");
        tv_accuracy.setText("not tracking");
        tv_alttitude.setText("not  tracking");
        tv_address.setText("not tracking");
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }
    private void updateUIvalues(Location location) {
        if (location != null) {
            tv_lat.setText(String.valueOf(location.getLatitude()));
            tv_lon.setText(String.valueOf(location.getLongitude()));
            tv_accuracy.setText(String.valueOf(location.getAccuracy()));
            if (location.hasAltitude()) {
                tv_alttitude.setText(String.valueOf(location.getAltitude()));
            } else {
                tv_alttitude.setText("Not available");
            }
            if (location.hasSpeed()) {
                tv_speed.setText(String.valueOf(location.getSpeed()));
            } else {
                tv_speed.setText("Not available");
            }
        } else {
            tv_lat.setText("Not available");
            tv_lon.setText("Not available");
            tv_accuracy.setText("Not available");
            tv_alttitude.setText("Not available");
            tv_speed.setText("Not available");
        }
        Geocoder geocoder=new Geocoder(MainActivity.this);
        try{
            List<Address> addresses= geocoder.getFromLocation(location.getLatitude(), location.getLongitude(),1);
            tv_address.setText(addresses.get(0).getAddressLine(0));
        }
        catch (Exception e){
            tv_address.setText("Unable to get address");
        }
        MyApplication myApplication=(MyApplication)getApplicationContext();
        savedLocations=myApplication.getMyLocations();
        tv_countOfCrumb.setText(Integer.toString(savedLocations.size()));
    }
}