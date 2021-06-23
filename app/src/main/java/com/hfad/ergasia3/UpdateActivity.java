package com.hfad.ergasia3;

import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.ButtCap;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UpdateActivity extends AppCompatActivity {

    private EditText editTitle;
    private EditText editID;
    private EditText editDescription;
    private EditText editCategory;
    private EditText editLatitude;
    private EditText editLongitude;
    private Button buttonSearch;
    private Button buttonUpdate;
    private Button buttonCancel;


    static ArrayList<POI> firebasePOIs = new ArrayList<>();

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("POIs");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        editTitle = findViewById(R.id.editTextTitle);
        editID = findViewById(R.id.editTextID);
        editDescription = findViewById(R.id.editTextDescription);
        editCategory = findViewById(R.id.editTextCategory);
        editLatitude = findViewById(R.id.editTextLatitude);
        editLongitude = findViewById(R.id.editTextLongitude);
        buttonSearch = findViewById(R.id.btnSearch);
        buttonUpdate = findViewById(R.id.btnUpdate);
        buttonCancel = findViewById(R.id.btnCancel);

        //Set EditTextViews &  Buttons Invisible before the search, except the editTextTitle.
        editTitle.setVisibility(View.VISIBLE);
        editID.setVisibility(View.INVISIBLE);
        editDescription.setVisibility(View.INVISIBLE);
        editCategory.setVisibility(View.INVISIBLE);
        editLatitude.setVisibility(View.INVISIBLE);
        editLongitude.setVisibility(View.INVISIBLE);
        buttonUpdate.setVisibility(View.INVISIBLE);
        buttonCancel.setVisibility(View.INVISIBLE);

        readPOIs();

    }

    //Read POIs from Firebase.
    public void readPOIs(){

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                //Clear the ArrayList from POIs.
                firebasePOIs.clear();
                try {
                    //Add data from Firebase to the ArrayList.
                    for (DataSnapshot poiSnapshot : dataSnapshot.getChildren()) {
                        POI poi =  poiSnapshot.getValue(POI.class);
                        firebasePOIs.add(poi);
                    }
                }
                catch(Exception e){
                    showMessage("Exception",e.toString());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    //Print the POIs that have been stored in the ArrayList from Firebase.
    public void printPOIs(View view){
        /*Print Array list*/
        StringBuffer buffer = new StringBuffer();
        for(POI poi : firebasePOIs){
            buffer.append(poi.getId()+"\n");
            buffer.append(poi.getTitle()+"\n");
            buffer.append(poi.getDescription()+"\n");
            buffer.append(poi.getCategory()+"\n");
            buffer.append(poi.getLatitude()+"\n");
            buffer.append(poi.getLongitude()+"\n");
            buffer.append("----------------------------------------\n");

        }
        //Print records from buffer to the screen.
        showMessage("----POIs----",buffer.toString());
    }

    //Search the ArrayList for a POI using its title and display its characteristics. User cannot edit the Title of the POI.
    public void search(View view){

        boolean poifound = false;
        String title = editTitle.getText().toString().trim();
        if(title.isEmpty()){
            Toast.makeText(this, "Please give a Title and press Search" ,Toast.LENGTH_LONG).show();
        }
        else{
            //poiList = (ArrayList<POI>) getIntent().getSerializableExtra("POIList");
            if(!firebasePOIs.isEmpty()) {

                for (POI poi : firebasePOIs) {

                    if (title.toLowerCase().trim().equals(poi.getTitle().toLowerCase().trim())) {
                        poifound = true;

                        editTitle.setText(poi.getTitle());
                        editTitle.setInputType(InputType.TYPE_NULL);

                        editID.setVisibility(View.VISIBLE);
                        editID.setText(poi.getId());

                        editDescription.setVisibility(View.VISIBLE);
                        editDescription.setText(poi.getDescription());

                        editCategory.setVisibility(View.VISIBLE);
                        editCategory.setText(poi.getCategory());

                        editLatitude.setVisibility(View.VISIBLE);
                        editLatitude.setText(poi.getLatitude().toString());

                        editLongitude.setVisibility(View.VISIBLE);
                        editLongitude.setText(poi.getLongitude().toString());

                        buttonSearch.setVisibility(View.INVISIBLE);
                        buttonUpdate.setVisibility(View.VISIBLE);
                        buttonCancel.setVisibility(View.VISIBLE);
                        break;
                    }

                }
                if(!poifound){
                    showMessage("POI not found","POI " + title + " was not found in Firebase");
                    editTitle.setText("");
                }
            }
            else{
                //showMessage("POI List is empty","");
                Toast.makeText(this, "POI list is empty" ,Toast.LENGTH_LONG).show();
            }
        }

    }

    //Get the values from the EditTextViews and store them to the Firebase node. After that make the EditTextViews
    // for the POI fields invisible, except the Title TextViewView which is cleared and the Search button .
    public void update(View view){
        try {
        POI newpoi = new POI();
        newpoi.setTitle(editTitle.getText().toString().trim());
        newpoi.setId(editID.getText().toString().trim());
        newpoi.setDescription(editDescription.getText().toString().trim());
        newpoi.setCategory(editCategory.getText().toString().trim());
        newpoi.setLatitude(Double.valueOf(editLatitude.getText().toString().trim()));
        newpoi.setLongitude(Double.valueOf(editLongitude.getText().toString().trim()));

        myRef.child(newpoi.getId()).setValue(newpoi);
        showMessage("POI updated","POI " + newpoi.getTitle() +" was updated");

        editTitle.setInputType(InputType.TYPE_CLASS_TEXT);
        editTitle.setText("");
        editID.setVisibility(View.INVISIBLE);
        editDescription.setVisibility(View.INVISIBLE);
        editCategory.setVisibility(View.INVISIBLE);
        editLatitude.setVisibility(View.INVISIBLE);
        editLongitude.setVisibility(View.INVISIBLE);
        buttonSearch.setVisibility(View.VISIBLE);
        buttonUpdate.setVisibility(View.INVISIBLE);
        buttonCancel.setVisibility(View.INVISIBLE);

        }catch(Exception e){ //NumberFormatException
            showMessage("Exception !",e.toString());
        }

    }

    //Make all the EditTextViews and Buttons Invisible, except the search button and the Title editTextView which is cleared.
    public void cancel(View view){
        editTitle.setInputType(InputType.TYPE_CLASS_TEXT);
        editTitle.setText("");
        editID.setVisibility(View.INVISIBLE);
        editDescription.setVisibility(View.INVISIBLE);
        editCategory.setVisibility(View.INVISIBLE);
        editLatitude.setVisibility(View.INVISIBLE);
        editLongitude.setVisibility(View.INVISIBLE);
        buttonSearch.setVisibility(View.VISIBLE);
        buttonUpdate.setVisibility(View.INVISIBLE);
        buttonCancel.setVisibility(View.INVISIBLE);
    }

    public void showMessage(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.show();
    }

}
