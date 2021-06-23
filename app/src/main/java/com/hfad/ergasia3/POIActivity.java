package com.hfad.ergasia3;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class POIActivity extends AppCompatActivity {

    private TextView txtId;
    private TextView txtTitle;
    private TextView txtDescription;
    private TextView txtCategory;
    private TextView txtLatitude;
    private TextView txtLongitude;
    private ImageView imgViewPOI;

    private TtS POITtS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi);

        POITtS = new TtS(this);

        txtId = findViewById(R.id.textViewID) ;
        txtTitle = findViewById(R.id.textViewTitle) ;
        txtDescription = findViewById(R.id.textViewDescr);
        txtCategory = findViewById(R.id.textViewCategory);
        txtLatitude = findViewById(R.id.textViewLat);
        txtLongitude = findViewById(R.id.textViewLon);
        imgViewPOI = findViewById(R.id.imageViewPOI);

        Intent intent = getIntent();
        String id = intent.getStringExtra(MainActivity.EXTRA_ID);
        String title = intent.getStringExtra(MainActivity.EXTRA_TITLE);
        String description = intent.getStringExtra(MainActivity.EXTRA_DESCRIPTION);
        String category = intent.getStringExtra(MainActivity.EXTRA_CATEGORY);
        Double latitude = intent.getDoubleExtra(MainActivity.EXTRA_LATITUDE,0.0);
        Double longitude = intent.getDoubleExtra(MainActivity.EXTRA_LONGITUDE,0.0);

        txtId.setText(id);
        txtTitle.setText(title);
        txtDescription.setText(description);
        txtCategory.setText(category);
        txtLatitude.setText(String.valueOf(latitude));
        txtLongitude.setText(String.valueOf(longitude));
        try {   //Find a photo from drawable directory
            int imageResource = getResources().getIdentifier("@drawable/" + title.toLowerCase(), null, this.getPackageName());
            if(imageResource == 0){ //If no photo is found in the drawable folder display a message.
                Toast.makeText(this, "No photo of this site was found.",Toast.LENGTH_LONG).show();
            }
            else { //If photo is found, display it on the imageView
                imgViewPOI.setImageResource(imageResource);
            }
        }
        catch(Exception e){
            Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show();
        }
    }

    public void TtS(View view){
        try {
            POITtS.speak("The nearest point of interest is " + txtTitle.getText().toString());
        }
        catch(Exception e){
            Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show();
        }

    }

}
