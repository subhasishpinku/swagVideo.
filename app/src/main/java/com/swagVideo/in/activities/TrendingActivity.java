package com.swagVideo.in.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.smarteist.autoimageslider.SliderViewAdapter;
import com.swagVideo.in.R;
import com.swagVideo.in.fragments.NearbyPlayerFragment;
import com.swagVideo.in.fragments.PlayerSliderFragment;
import com.swagVideo.in.fragments.TrendingFragment;
import com.swagVideo.in.fragments.TrendingTabsFragment;
import com.swagVideo.in.pojo.SliderItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;

public class TrendingActivity extends AppCompatActivity {

    Fragment currentFragment;
    public static TrendingTabsFragment trendingTabsFragment = new TrendingTabsFragment();
    private TrendingFragment trendingFragment = new TrendingFragment();
    private ImageButton header_more;
    private TextView header_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trending);

        header_more = findViewById(R.id.header_more);
        header_title = findViewById(R.id.header_title);
        Glide.with(this).load(R.drawable.ic_baseline_search_24).into(header_more);
        header_title.setText("Trending");
        View back = findViewById(R.id.header_back);
        //  back.setOnClickListener(v -> ((MainActivity)requireActivity()).popBackStack());
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        replaceFragment(trendingFragment);
    }

    @Override
    public void onBackPressed() {
      //  super.onBackPressed();
        /*Intent intent = new Intent(TrendingActivity.this, MainActivity.class);
       // intent.putExtra("from","nearby");
        startActivity(intent);*/
        //finish();
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        Fragment activeFragment = fragments.get(fragments.size() - 1);
        if(activeFragment instanceof TrendingFragment){
            finish();
        }else {
            //replaceFragment(new TrendingTabsFragment());
            super.onBackPressed();
        }/*else {
            replaceFragment(trendingFragment);
        }*/
    }
    public void replaceFragment (Fragment fragment){
        String backStateName =  fragment.getClass().getName();
        String fragmentTag = backStateName;

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();

        ft.replace(R.id.container, fragment, fragmentTag);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(backStateName);
        ft.commit();
        currentFragment=fragment;
    }
    public void replaceFragmentWithoutBackStack (Fragment fragment){
        String backStateName =  fragment.getClass().getName();
        String fragmentTag = backStateName;

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();

        ft.replace(R.id.container, fragment, fragmentTag);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        // ft.addToBackStack(backStateName);
        ft.commit();
        currentFragment=fragment;
    }

    public class SliderAdapterExample extends
            SliderViewAdapter<SliderAdapterExample.SliderAdapterVH> {

        private Context context;
        private List<SliderItem> mSliderItems = new ArrayList<>();

        public SliderAdapterExample(Context context, List<SliderItem> mSliderItems) {
            this.context = context;
            this.mSliderItems = mSliderItems;
        }

        public void renewItems(List<SliderItem> sliderItems) {
            this.mSliderItems = sliderItems;
            notifyDataSetChanged();
        }

        public void deleteItem(int position) {
            this.mSliderItems.remove(position);
            notifyDataSetChanged();
        }

        public void addItem(SliderItem sliderItem) {
            this.mSliderItems.add(sliderItem);
            notifyDataSetChanged();
        }

        @Override
        public SliderAdapterExample.SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
            View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_slider_layout_item, null);
            return new SliderAdapterExample.SliderAdapterVH(inflate);
        }

        @Override
        public void onBindViewHolder(SliderAdapterExample.SliderAdapterVH viewHolder, final int position) {

            SliderItem sliderItem = mSliderItems.get(position);

            // viewHolder.textViewDescription.setText(sliderItem.getDescription());
            viewHolder.textViewDescription.setTextSize(16);
            viewHolder.textViewDescription.setTextColor(Color.WHITE);
            Glide.with(viewHolder.itemView)
                    .load(sliderItem.getImg())
                    .fitCenter()
                    .into(viewHolder.imageViewBackground);

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplicationContext(), "This is item in position " + position, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getCount() {
            //slider view count could be dynamic size
            return mSliderItems.size();
        }

        class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

            View itemView;
            ImageView imageViewBackground;
            ImageView imageGifContainer;
            TextView textViewDescription;

            public SliderAdapterVH(View itemView) {
                super(itemView);
                imageViewBackground = itemView.findViewById(R.id.iv_auto_image_slider);
                imageGifContainer = itemView.findViewById(R.id.iv_gif_container);
                textViewDescription = itemView.findViewById(R.id.tv_auto_image_slider);
                this.itemView = itemView;
            }
        }
    }
}