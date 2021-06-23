package com.hfad.ergasia3;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener {


    LocationManager locationManager;
    static Integer RANGE;
    SQLiteDatabase db;
    int REQUESTCODE = 322; //static
    Location currentLocation;
    //Button buttonWrite;
    static ArrayList<POI> poiList = new ArrayList<>(); //static
    ArrayList<POI> poiInRangeList = new ArrayList<>();
    //boolean withinRange = false;


    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("POIs");

    /*Dummy Variables*/
    //boolean printed = false;
    boolean foundPoi = false;
    POI closestPoi = new POI();
    Location locationFoundPoi = null;

    /*POI Activity*/
    public static final String EXTRA_ID = "id" ;
    public static final String EXTRA_TITLE = "title" ;
    public static final String EXTRA_DESCRIPTION = "description" ;
    public static final String EXTRA_CATEGORY = "category" ;
    public static final String EXTRA_LATITUDE = "latitude" ;
    public static final String EXTRA_LONGITUDE = "longitude" ;

    public static final String EXTRA_LIST = "POIList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //Read POIs from Firebase
        read();

        //Create Database and a table for storing minimum range from POIs and POI statistics fro every POI found in range.
        db = openOrCreateDatabase("POILocator", MODE_PRIVATE, null);

        db.execSQL("CREATE TABLE IF NOT EXISTS 'minimumrange' (" +
                "'id' INTEGER PRIMARY KEY AUTOINCREMENT , " +
                "'range' INTEGER  )" );


        db.execSQL("CREATE TABLE IF NOT EXISTS 'POIstatistics' (" +
                "'id' INTEGER PRIMARY KEY AUTOINCREMENT , " +
                "'POIid' TEXT ," +
                "'POItitle' TEXT, " +
                "'POIdescription' TEXT, " +
                "'POIcategory' TEXT, " +
                "'latitude' REAL , " +
                "'longitude' REAL, " +
                "'timestamp' TEXT  )" );

        //Set default minimum range from POI to 1000 meters, if no range is stored in database.
        Cursor cursor = db.rawQuery("SELECT range FROM minimumrange ",null);
        if (cursor.getCount() == 0){

            ContentValues insertValues = new ContentValues();
            insertValues.put("range", "1000" );
            db.insert("minimumrange",null,insertValues);
            RANGE = 1000;

        }
        else {
            while (cursor.moveToNext()) {
                RANGE = cursor.getInt(0);
            }
        }

        //The application requests permissions for Location updates, when it starts.
        try {
            giveperm();
        }
        catch(Exception e){
            showMessage("Exception",e.toString());
        }


    }

    //Request permissions for Location updates.
    public void giveperm(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUESTCODE);
        }
        else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,
                    this);
        }
    }

    //Callback for the result from requesting permissions.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            //Toast.makeText(this,"Yesss I have GPS",Toast.LENGTH_SHORT).show();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                        this);
            } else
                Toast.makeText(this,"Please I really need this permission!...",Toast.LENGTH_SHORT).show();
    }

    //Insert POIs in firebase.
    public void write(View view){

        addPOI("Syntagma","Parliament square","Recreation",37.975518,23.734791);
        addPOI("Omonoia","Omonoia square","Food",37.984221,23.728041);
        addPOI("Kotzia","Kotzia square-town hall","Entertainment",37.981693,23.727812);
        addPOI("National_Museum","National Archeological museum","Recreation",37.989035,23.732500);
        addPOI("Acropolis","Acropolis & Parthenon","Recreation",37.971564,23.725740);
        addPOI("National_Art_Gallery","National Art & Picture Gallery","Recreation",37.975891,23.749441);

    }

    //Method for storing POIs in firebase.
    public void addPOI (String title,String description,String category,Double latit, Double longit){

            String id = myRef.push().getKey();
            POI poi = new POI(id,title,description,category,latit, longit);
            myRef.child(id).setValue(poi); //Store POI in firebase.
            Toast.makeText(this,"POI added.",Toast.LENGTH_LONG).show();

    }

    //Method for retrieving POIs from Firebase and storing them to poiList ArrayList.
    public void read(){

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //StringBuffer buffer = new StringBuffer();
                //Clear the List from POIs.
                poiList.clear();
                try {
                    //Add data from Firebase to the List.
                    for (DataSnapshot poiSnapshot : dataSnapshot.getChildren()) {
                        POI poi =  poiSnapshot.getValue(POI.class); // (POI)
                        poiList.add(poi);
                    }
                }
                catch(Exception e){
                    showMessage("",e.toString());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    //Delete POIs from Firebase.
    public void delete(View view){

        myRef.removeValue();
        Toast.makeText(this,"POIs deleted.",Toast.LENGTH_LONG).show();

    }

    //Open new Activity to show the Map with the location of POIs and our location.
    public void map(View view){
        try {
            Intent mapIntent = new Intent(this, MapActivity.class);
            mapIntent.putExtra(EXTRA_LATITUDE, currentLocation.getLatitude());
            mapIntent.putExtra(EXTRA_LONGITUDE, currentLocation.getLongitude());
            startActivity(mapIntent);
        }
        catch(Exception e){
            showMessage("exception",e.toString());
        }
    }

    //The User can change the Minimum Range for the search for POIs.
    public void range(View view){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Current Minimum Range from POI");
        builder.setMessage("To change minimum range, please write a new range measured in meters and press OK: ");



        // Set up the input
        final EditText input = new EditText(this);
        input.setText(String.valueOf(RANGE));
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER); //InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
        builder.setView(input);

        // Set up the OK button
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String m_Text = input.getText().toString();   //input.getText().toString();

                //showMessage("New Speed Limit",m_Text);
                Integer minrange = Integer.parseInt(m_Text);

                ContentValues cv = new ContentValues();
                cv.put("range",minrange );

                //Update the database with the new minimum range.
                int rowsUpdated;
                rowsUpdated = db.update("minimumrange", cv , ""  , null );

                if(rowsUpdated > 0){
                    showMessage("Row updated." ,"New Minimum Range from POI = "+ m_Text );
                    RANGE = minrange;
                }
                else{
                    showMessage("No Row updated","" );
                }
            }
        });
        //Set up the Cancel button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    //Method to display messages.
    public void showMessage(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.show();
    }


    @Override
    public void onLocationChanged(Location location) {

        //If poiList is not empty and foundPoi variable is false search for nearby POIs within range.
        if(!poiList.isEmpty() && !foundPoi){

            currentLocation = location;
            //Check if any POIs are within range and store them in poiInRangeList .
            Location poiLocation = new Location("");
            for (POI poi : poiList) {

                poiLocation.setLatitude(poi.getLatitude());
                poiLocation.setLongitude(poi.getLongitude());

                if (location.distanceTo(poiLocation) <= (float) RANGE) {
                    foundPoi = true; //POI within range has been found. Mark the flag variable foundPoi=true .
                    locationFoundPoi = location; //Mark the device location in variable locationFoundPoi.
                    poiInRangeList.add(poi); //Add the POI that has been found within range to the poiInRangeList.
                }
            }

            //If POIs have been found within range, find the closest one, display its details and store it in the database.
            if (!poiInRangeList.isEmpty()){

                Location poiInRangeLocation = new Location("");
                poiInRangeLocation.setLatitude(poiInRangeList.get(0).getLatitude());
                poiInRangeLocation.setLongitude(poiInRangeList.get(0).getLongitude());
                float minDistPoi = location.distanceTo(poiInRangeLocation);
                closestPoi = poiInRangeList.get(0);

                for (POI poiInRange : poiInRangeList) {
                    poiInRangeLocation.setLatitude(poiInRange.getLatitude());
                    poiInRangeLocation.setLongitude(poiInRange.getLongitude());

                    if (location.distanceTo(poiInRangeLocation) < minDistPoi) {
                        minDistPoi = location.distanceTo(poiInRangeLocation); //New minimum distance
                        //The closest POI to our location.
                        closestPoi = poiInRange;

                    }

                }
                poiInRangeList.clear(); //Clear the ArrayList with the POIs within range.
                POIInfoDisplay(closestPoi); //Display POI information in a new POIActivity.
                POIStoreDatabase(closestPoi); //Store the closest POI in local Database for statistics use.

            }

        }
        //If location is changed after a POI has been found within range, make the variable foundPoi= false again and begin
        //new search for POIs.
        else if(!poiList.isEmpty() && foundPoi){
            if(locationFoundPoi.getLatitude() != location.getLatitude() || locationFoundPoi.getLongitude() != location.getLongitude()){
                foundPoi = false;

            }
        }


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //Display POI information in POIActivity.
    void POIInfoDisplay(POI poi){
        try {

            Intent POIIntent = new Intent(this, POIActivity.class);
            POIIntent.putExtra(EXTRA_ID, poi.getId());
            POIIntent.putExtra(EXTRA_TITLE, poi.getTitle());
            POIIntent.putExtra(EXTRA_DESCRIPTION, poi.getDescription());
            POIIntent.putExtra(EXTRA_CATEGORY, poi.getCategory());
            POIIntent.putExtra(EXTRA_LATITUDE, poi.getLatitude());
            POIIntent.putExtra(EXTRA_LONGITUDE, poi.getLongitude());

            startActivity(POIIntent);
        }
        catch(Exception e){
            showMessage("exception",e.toString());
        }

    }

    //Method used to store a new row in the database for POI search statistics.
    void POIStoreDatabase(POI poi){

        Date currentTime = Calendar.getInstance().getTime();

        ContentValues insertValues = new ContentValues();
        insertValues.put("POIid", poi.getId());
        insertValues.put("POItitle", poi.getTitle());
        insertValues.put("POIdescription", poi.getDescription());
        insertValues.put("POIcategory", poi.getCategory());
        insertValues.put("latitude", poi.getLatitude());
        insertValues.put("longitude", poi.getLongitude());
        insertValues.put("timestamp", currentTime.toString());


        //Paint screen red.
        //getWindow().getDecorView().setBackgroundColor(Color.RED); //Set Window with colour RED if speed is above speed limit .
        long insertResult = db.insert("POIstatistics",null,insertValues);
        if(insertResult == -1){
            showMessage("Error","Closest POI not saved in Database. ");
        }
        else{
            showMessage("Closest POI Saved in Database","");
        }

    }

    //Show POI search statistics by POI category.
    public void showstats(View view){

        StringBuffer buffer = new StringBuffer();
        Cursor cursor = db.rawQuery("SELECT POIcategory, count(*) AS POI_Num FROM 'POIstatistics' GROUP BY POIcategory ",null);
        if (cursor.getCount()!=0){
            while (cursor.moveToNext()){
                buffer.append("Category: "+cursor.getString(0)+"\n");
                buffer.append("Number of POIs stored: "+cursor.getInt(1)+"\n");

                buffer.append("-----------------------------------\n");
            }
            showMessage("--- POI Statistics ---",buffer.toString());
        }
        else{
            showMessage("--- POI Statistics ---","No Records where found.");
        }

    }

    //Delete all the rows from the POI statistics table.
    public void delstats(View view){

        int deletedRows = db.delete("POIstatistics","" ,null);
        if (deletedRows != 0){
            showMessage("Number of Deleted Rows : " , String.valueOf(deletedRows));
        }
        else{
            showMessage("No Rows where Deleted","");
        }
        //Make the autoincrement id column of speed_excess_locations table start from 1 again.
        db.delete("sqlite_sequence","name=?" ,new String []{"POIstatistics"});

    }

    //Call the UpdateActivity, to update Firebase nodes.
    public void update(View view){
        try {
            Intent UpdateIntent = new Intent(this, UpdateActivity.class);
            //UpdateIntent.putExtra(EXTRA_LIST, poiList);
            startActivity(UpdateIntent);
        }
        catch(Exception e){
            showMessage("Exception!",e.toString());
        }

    }

}
