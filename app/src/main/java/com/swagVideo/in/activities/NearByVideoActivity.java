package com.swagVideo.in.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;

import com.swagVideo.in.R;
import com.swagVideo.in.fragments.NearbyFragment;
import com.swagVideo.in.fragments.NearbyPlayerFragment;
import com.swagVideo.in.fragments.PlayerSliderFragment;
import com.swagVideo.in.fragments.TrendingFragment;

public class NearByVideoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_near_by_video);
        Bundle args = new Bundle();
        //  args.putString("joinAs", "");
        Fragment fr = new NearbyPlayerFragment();
        fr.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fr).addToBackStack(null).commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(NearByVideoActivity.this, MainActivity.class);
       // intent.putExtra("from","nearby");
        startActivity(intent);
        finish();
    }
}