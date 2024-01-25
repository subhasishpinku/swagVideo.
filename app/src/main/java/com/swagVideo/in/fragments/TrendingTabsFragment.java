package com.swagVideo.in.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.smarteist.autoimageslider.SliderViewAdapter;
import com.swagVideo.in.R;
import com.swagVideo.in.activities.MainActivity;
import com.swagVideo.in.data.ClipDataSource;
import com.swagVideo.in.data.api.REST;
import com.swagVideo.in.data.models.Clip;
import com.swagVideo.in.data.models.Slider;
import com.swagVideo.in.pojo.SliderItem;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import static com.swagVideo.in.fragments.NearbyFragment.clipStat;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TrendingTabsFragment extends Fragment {

    private static final String ARG_PARAMS = "params";
    private MainActivity.MainActivityViewModel mModel;
    private SliderView sliderView;
    private ImageView ivUser;
    private TextView tvUserName;
    private TextView tvDesc;
    private TextView tvView;
    private TrendingTabsFragment.SliderAdapterExample adapter;
    private ArrayList<SliderItem> sliderItems = new ArrayList<>();
    private String image,description,heading,viewCount;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mModel = new ViewModelProvider(requireActivity())
                .get(MainActivity.MainActivityViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trending_tabs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sliderView = view.findViewById(R.id.imageSlider);
        ivUser = view.findViewById(R.id.ivUser);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvDesc = view.findViewById(R.id.tvDesc);
        tvView = view.findViewById(R.id.tvView);
        MaterialCardView mcvHas = view.findViewById(R.id.mcvHas);
        mcvHas.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.bg_round_red_yellow_sheet3));

        View back = view.findViewById(R.id.header_back);
        back.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });
        TextView title = view.findViewById(R.id.header_title);
        title.setText("Trending");
        ImageButton extra = view.findViewById(R.id.header_extra);
        extra.setVisibility(View.GONE);
        View more = view.findViewById(R.id.header_more);
        more.setVisibility(View.GONE);

        Bundle params = requireArguments().getBundle(ARG_PARAMS);
        if (params == null) {
            params = new Bundle();
        }

        heading = params.getString("heading");
        description = params.getString("description");
        image = params.getString("image");
        viewCount = params.getString("viewCount");
        Glide.with(this).load(image).fitCenter().error(R.mipmap.ic_app_icon).into(ivUser);
        tvUserName.setText(heading);
        tvDesc.setText(description);
        tvView.setText(viewCount);

        getSliderImage();

        ViewPager2 pager = view.findViewById(R.id.pager);
        pager.setAdapter(new PlayerTabPagerAdapter(this));
        pager.setCurrentItem(0, true);
       /* TabLayout tabs = view.findViewById(R.id.tabs);
      //  tabs.setBackgroundResource(android.R.color.transparent);
        new TabLayoutMediator(tabs, pager, (tab, position) -> {
            int text = position == 0
                    ? R.string.trending
                    : R.string.recent;
                tab.setText(text);
        }).attach();*/
    }

    public static TrendingTabsFragment newInstance() {
        return new TrendingTabsFragment();
    }

    private class PlayerTabPagerAdapter extends FragmentStateAdapter {

        public PlayerTabPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
          //  if (position == 0) {

                return TrendFragment.newInstance(null, null);
            /*}else {
                return RecentTrendFragment.newInstance(null,null);
            }*/

        }

        @Override
        public int getItemCount() {
            return 1;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        clipStat = new Clip();
    }


    public void getSliderImage() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getResources().getString(R.string.server_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        REST api = retrofit.create(REST.class);
        try {
            Call<ResponseBody> call = api.getBannerList();
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.i("fetchNearbyData", response.body().toString());
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONObject jsonObjectData = jsonObject.getJSONObject("data");

                        ArrayList<Slider>
                                sliderList = new Gson().fromJson(jsonObject.getJSONObject("data").getString("list"), new TypeToken<List<Slider>>() {
                        }.getType());

                        setSlider(sliderList);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                   t.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setSlider(ArrayList<Slider> sliderList) {
        sliderItems.clear();

        for (int i=0; i<sliderList.size(); i++) {
            sliderItems.add(new SliderItem(sliderList.get(i).getName(), sliderList.get(i).getImage()));
       /* sliderItems.add(new SliderItem("Text Here", R.drawable.slideone));
        sliderItems.add(new SliderItem("Text Here", R.drawable.slidetwo));
        sliderItems.add(new SliderItem("Text Here", R.drawable.slidethree));*/
        };

        adapter = new TrendingTabsFragment.SliderAdapterExample(getActivity(),sliderItems);
        sliderView.setSliderAdapter(adapter);
        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM); //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH);
        sliderView.setIndicatorSelectedColor(Color.WHITE);
        sliderView.setIndicatorUnselectedColor(Color.GRAY);
        sliderView.setScrollTimeInSec(3);
        sliderView.setAutoCycle(true);
        sliderView.startAutoCycle();
    }
    public class SliderAdapterExample extends
            SliderViewAdapter<TrendingTabsFragment.SliderAdapterExample.SliderAdapterVH> {

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
        public TrendingTabsFragment.SliderAdapterExample.SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
            View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_slider_layout_item, null);
            return new TrendingTabsFragment.SliderAdapterExample.SliderAdapterVH(inflate);
        }

        @Override
        public void onBindViewHolder(TrendingTabsFragment.SliderAdapterExample.SliderAdapterVH viewHolder, final int position) {

            SliderItem sliderItem = mSliderItems.get(position);

            // viewHolder.textViewDescription.setText(sliderItem.getDescription());
            viewHolder.textViewDescription.setTextSize(16);
            viewHolder.textViewDescription.setTextColor(Color.WHITE);
            Glide.with(viewHolder.itemView)
                    .load(sliderItem.getImage())
                    .error(R.mipmap.ic_app_icon)
                    .fitCenter()
                    .into(viewHolder.imageViewBackground);

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), "This is item in position " + position, Toast.LENGTH_SHORT).show();
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
