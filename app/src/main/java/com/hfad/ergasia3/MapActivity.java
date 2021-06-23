package com.hfad.ergasia3;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap map;
    Double latitude;
    Double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        //Get latitude & longitude from MainActivity
        Intent intent = getIntent();
        latitude = intent.getDoubleExtra(MainActivity.EXTRA_LATITUDE,0.0);
        longitude = intent.getDoubleExtra(MainActivity.EXTRA_LONGITUDE,0.0);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;
        LatLng mylatlng = new LatLng(latitude,longitude);
        StringBuffer buffer = new StringBuffer();

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                map.setMyLocationEnabled(true); //Display the device's location on the map.
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(mylatlng,12),1000,null); //Zoom to my location.
            }
        }
        catch(Exception e){
            showMessage("Exception!",e.toString());
        }


        //Create markers for each POI, on the map.
        for(POI poi : MainActivity.poiList){

            //Toast.makeText(this,"POI LatLng: " + poi.getLatitude().toString()+ " , " + poi.getLongitude().toString() ,Toast.LENGTH_LONG).show();
            buffer.append(poi.getTitle() + "\n");
            buffer.append(poi.getLatitude() + "\n");
            buffer.append(poi.getLongitude() + "\n");
            buffer.append("----------------------\n");

            LatLng poilatlng = new LatLng( poi.getLatitude() , poi.getLongitude());
            map.addMarker(new MarkerOptions().position(poilatlng).title( poi.getTitle()));

        }
        //showMessage("----POIs----",buffer.toString());

    }

    public void showMessage(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.show();
    }


}
