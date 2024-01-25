package com.swagVideo.in.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.swagVideo.in.R;

import java.util.Arrays;
import java.util.List;

import static com.swagVideo.in.data.StaticData.latitude;
import static com.swagVideo.in.data.StaticData.longitude;
import static com.swagVideo.in.data.StaticData.placeName;

public class PlacesActivity extends AppCompatActivity {

    private  EditText etPlace;
    private TextView tvview1,tvview2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);


        etPlace = findViewById(R.id.etPlace);
        tvview1 = findViewById(R.id.tvView1);
        tvview2 = findViewById(R.id.tvView2);


//        Places.initialize(getApplicationContext(),"");

                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);

                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,fieldList).build(PlacesActivity.this);
                startActivityForResult(intent, 100);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==100 && resultCode == RESULT_OK){
            Place place = Autocomplete.getPlaceFromIntent(data);
            String[] spiteplaceName = place.getAddress().split(",");
            placeName =  spiteplaceName[0]+", "+spiteplaceName[1];
            String[] spiteS = String.valueOf(place.getLatLng()).split("\\(");
            String[] spitelat = spiteS[1].split(",");
            String[] spiteLong = spitelat[1].split("\\)");
            latitude = spitelat[0];
            longitude = spiteLong[0];
            onBackPressed();
            finish();
        }else{
            onBackPressed();
            finish();
        }
    }
}