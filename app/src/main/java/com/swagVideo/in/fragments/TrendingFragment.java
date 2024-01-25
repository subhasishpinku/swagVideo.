package com.swagVideo.in.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.smarteist.autoimageslider.SliderViewAdapter;
import com.swagVideo.in.R;
import com.swagVideo.in.activities.MainActivity;
import com.swagVideo.in.activities.TrendingActivity;
import com.swagVideo.in.adapter.RecyclerViewAdapter;
import com.swagVideo.in.adapter.TextGradient;
import com.swagVideo.in.data.api.REST;
import com.swagVideo.in.data.models.Slider;
import com.swagVideo.in.data.models.User;
import com.swagVideo.in.pojo.Itemlist;
import com.swagVideo.in.pojo.NearBylIst;
import com.swagVideo.in.pojo.SliderItem;
import com.swagVideo.in.pojo.TrendingList;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TrendingFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private ProgressDialog progressDialog;
    private RecyclerView rv;
    private RecyclerViewAdapter<TrendingList> viewAdapter;
    private ArrayList<TrendingList> nearBylIsts = new ArrayList<>();
    public static ArrayList<NearBylIst> trendingLists = new ArrayList<>();
    public static ArrayList<NearBylIst> latestLists = new ArrayList<>();
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Geocoder geocoder;
    private String lat = "", longi = "";
    private Dialog myDialog;
    private ProgressBar loading;

    private SliderView sliderView;
    private SliderAdapterExample adapter;
    private ArrayList<SliderItem> sliderItems = new ArrayList<>();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TrendingFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static TrendingFragment newInstance(String param1, String param2) {
        TrendingFragment fragment = new TrendingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_trending, container, false);

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

        initView(view);

//        nearBylIsts.add(new NearBylIst(R.drawable.people,"0.2", "http://goo.gl/gEgYUd","https://mir-s3-cdn-cf.behance.net/project_modules/max_1200/f19c6c63077653.5aa65266cb14d.gif"));
//        nearBylIsts.add(new NearBylIst(R.drawable.people,"1.2", "http://goo.gl/gEgYUd","https://mir-s3-cdn-cf.behance.net/project_modules/max_1200/f19c6c63077653.5aa65266cb14d.gif"));
//        nearBylIsts.add(new NearBylIst(R.drawable.people,"1.5", "http://goo.gl/gEgYUd","https://mir-s3-cdn-cf.behance.net/project_modules/max_1200/f19c6c63077653.5aa65266cb14d.gif"));
//        nearBylIsts.add(new NearBylIst(R.drawable.people,"1.2", "http://goo.gl/gEgYUd","https://mir-s3-cdn-cf.behance.net/project_modules/max_1200/f19c6c63077653.5aa65266cb14d.gif"));

        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        viewAdapter = new RecyclerViewAdapter<>(getActivity(), R.layout.trending_recycler_layout, nearBylIsts);
        viewAdapter.setMapper((viewHolder, source) -> {

            try {

                RecyclerView rvItems = (RecyclerView) viewHolder.getView(R.id.rvItems);
                TextView TvHeading = (TextView) viewHolder.getView(R.id.TvHeading);
                TextView tvViews = (TextView) viewHolder.getView(R.id.tvViews);
                LinearLayout llHeading = (LinearLayout) viewHolder.getView(R.id.heading);
                RelativeLayout rl = (RelativeLayout) viewHolder.getView(R.id.rl);
                RecyclerViewAdapter<Itemlist> viewAdapterItems;
                ArrayList<Itemlist> itemList = new ArrayList<>();

    /*rl.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            replaceFragment(new TrendingTabsFragment());
        }
    });*/

                for (int i = 0; i < source.getItems().size(); i++) {
                    itemList.add(new Itemlist(source.getItems().get(i).getGif(),
                            String.valueOf(source.getItems().get(i).getViewsCount()),
                            source.getItems().get(i).getDescription(),
                            source.getItems().get(i).getUser().name, source.getItems().get(i).getUser().photo));
                }
                if (itemList.size() > 0) {
       /* TvHeading.setVisibility(View.VISIBLE);
        tvViews.setVisibility(View.VISIBLE);*/
                    rvItems.setVisibility(View.VISIBLE);
                    llHeading.setVisibility(View.VISIBLE);

                    SpannableString gradientText = new SpannableString("#" + source.getHeading());
                    gradientText.setSpan(new TextGradient(Color.RED, Color.YELLOW, source.getHeading().length()),
                            0, gradientText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    /*
        gradientText.setSpan(new TextGradient(Color.RED, Color.YELLOW, TvHeading.getLineHeight()),
                0, gradientText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);*/
                    SpannableStringBuilder sb = new SpannableStringBuilder();
                    sb.append(gradientText);
                    TvHeading.setText(sb);
                    tvViews.setText(source.getTotalViewCount());

                    llHeading.setOnClickListener(v -> {
                        trendingLists = source.getItems();
                        latestLists = source.getItemsLatest();
                        Bundle args = new Bundle();
                        args.putString("heading", source.getHeading());
                        args.putString("description", source.getDesc());
                        args.putString("image", source.getImage());
                        args.putString("viewCount", source.getTotalViewCount());
                        TrendingTabsFragment fragment = new TrendingTabsFragment();
                        fragment.setArguments(args);
                        //replaceFragment(fragment);

                        ((MainActivity) requireActivity()).showTrendingDetails(args);
                    });

                    //rvItems.setLayoutManager(new GridLayoutManager(getActivity(), 3));
                    rvItems.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
                    viewAdapterItems = new RecyclerViewAdapter<>(getActivity(), R.layout.trending_items_layout, itemList);
                    viewAdapterItems.setMapper((viewHolderitem, sourceitem) -> {
                        try {
                            ImageView iv = (ImageView) viewHolderitem.getView(R.id.iv);
                            TextView tvKm = (TextView) viewHolderitem.getView(R.id.tvKm);
                            ImageView ivUser = (ImageView) viewHolderitem.getView(R.id.iv_user);
                            TextView tvUserName = (TextView) viewHolderitem.getView(R.id.tv_user_name);
                            TextView tvTitle = (TextView) viewHolderitem.getView(R.id.tv_title);

                            iv.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    trendingLists = source.getItems();
                                    latestLists = source.getItemsLatest();
                                    Bundle args = new Bundle();
                                    args.putString("heading", source.getHeading());
                                    args.putString("description", source.getDesc());
                                    args.putString("image", source.getImage());
                                    args.putString("viewCount", source.getTotalViewCount());
                                    TrendingTabsFragment fragment = new TrendingTabsFragment();
                                    fragment.setArguments(args);
                                    //replaceFragment(fragment);

                                    ((MainActivity) requireActivity()).showTrendingDetails(args);
                                }
                            });

                            tvKm.setText(sourceitem.getCount());
                            tvUserName.setText(sourceitem.getUserName());
                            tvTitle.setText(sourceitem.getTitle());
                            Glide.with(this).load(sourceitem.getImg()).fitCenter().error(R.mipmap.ic_app_icon).into(iv);
                            String imgUrl = getResources().getString(R.string.image_base_url) + sourceitem.getUserImage();
                            Glide.with(this).load(imgUrl).apply(new RequestOptions().circleCrop()).error(R.drawable.user).into(ivUser);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });
                    rvItems.setAdapter(viewAdapterItems);
                    viewAdapterItems.notifyDataSetChanged();
                } else {
        /*TvHeading.setVisibility(View.GONE);
        tvViews.setVisibility(View.GONE);*/
                    rvItems.setVisibility(View.GONE);
                    llHeading.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
        rv.setAdapter(viewAdapter);
        viewAdapter.notifyDataSetChanged();

        fetchTrending();
        getSliderImage();

        return view;
    }

    private void initView(View view) {
        sliderView = view.findViewById(R.id.sv_slider);
        rv = view.findViewById(R.id.rv);
        loading = view.findViewById(R.id.loading);
    }

    private void setSlider(ArrayList<Slider> sliderList) {
        sliderItems.clear();

        for (int i = 0; i < sliderList.size(); i++) {
            sliderItems.add(new SliderItem(sliderList.get(i).getName(), sliderList.get(i).getImage()));
       /* sliderItems.add(new SliderItem("Text Here", R.drawable.slideone));
        sliderItems.add(new SliderItem("Text Here", R.drawable.slidetwo));
        sliderItems.add(new SliderItem("Text Here", R.drawable.slidethree));*/
        }

        adapter = new SliderAdapterExample(getActivity(), sliderItems);
        sliderView.setSliderAdapter(adapter);
        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM); //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH);
        //sliderView.setIndicatorSelectedColor(Color.WHITE);
        //sliderView.setIndicatorUnselectedColor(Color.GRAY);
        sliderView.setScrollTimeInSec(3);
        sliderView.setAutoCycle(true);
        sliderView.startAutoCycle();
    }

    public void replaceFragment(Fragment fragment) {
        ((TrendingActivity) getContext()).replaceFragment(fragment);
    }

    public void getSliderImage() {
        loading.setVisibility(View.VISIBLE);

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
                    } finally {
                        loading.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    progressDialog.dismiss();
                }
            });
        } catch (Exception e) {
            // progressDialog.dismiss();
            loading.setVisibility(View.GONE);
            e.printStackTrace();
        }
    }

    public void fetchTrending() {
        /*progressDialog = new ProgressDialog(getActivity());
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);*/
        loading.setVisibility(View.VISIBLE);
        nearBylIsts.clear();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getResources().getString(R.string.server_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        REST api = retrofit.create(REST.class);
        try {
            Call<ResponseBody> call = api.getTrending();
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.i("fetchNearbyData", response.body().toString());
                    /*if (response.body().getAck()==1) {
                        NearBylIst nearBylIst = response.body();
                        nearBylIsts.addAll(nearBylIst.getOrderData());
                        viewAdapter.notifyDataSetChanged();

                    }else {
                        Toast.makeText(getActivity(), response.body().getMsg(), Toast.LENGTH_LONG).show();
                    }*/
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONObject jsonObjectData = jsonObject.getJSONObject("data");
                        JSONArray jsonArray = jsonObjectData.getJSONArray("tag");
                        Log.i("fetchNearbyData", String.valueOf(jsonArray.length()));
                        Gson gson = new Gson();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject1 = new JSONObject(jsonArray.getString(i));
                            String hashtag = jsonObject1.getString("tag_title");
                            String tagImage = jsonObject1.getString("tag_image");
                            String tagDesc = jsonObject1.getString("tag_desc");
                            String totalViewCount = jsonObject1.getString("total_view_count");
                            ArrayList<NearBylIst> myModelList = new ArrayList();
                            ArrayList<NearBylIst> myModelLatestList = new ArrayList();
                            JSONArray jsonArrayOther = jsonObject1.getJSONArray("otherclip");
                            for (int j = 0; j < jsonArrayOther.length(); j++) {
                                //myModelList = gson.fromJson(String.valueOf(jsonArrayLatest),ArrayList.class);
                                JSONObject jsonObject2 = new JSONObject(jsonArrayOther.getString(j));
                                User user = new User();
                                user = (User) gson.fromJson(jsonObject2.getJSONObject("user").toString(), User.class);
                                myModelList.add(new NearBylIst("", jsonObject2.getString("video"), jsonObject2.getString("preview"), jsonObject2.getString("video"), user, jsonObject2.getInt("views_count"), jsonObject2.getInt("likes_count"), jsonObject2.getInt("comments_count"), jsonObject2.getBoolean("comments"), jsonObject2.getBoolean("liked"), jsonObject2.getBoolean("saved"), jsonObject2.getInt("id"), jsonObject2.getString("location"), jsonObject2.getString("screenshot"), jsonObject2.getString("description")));
                            }
                            JSONArray jsonArrayLatest = jsonObject1.getJSONArray("letestclip");
                            for (int j = 0; j < jsonArrayLatest.length(); j++) {
                                //myModelList = gson.fromJson(String.valueOf(jsonArrayLatest),ArrayList.class);
                                JSONObject jsonObject2 = new JSONObject(jsonArrayLatest.getString(j));
                                User user = new User();
                                user = (User) gson.fromJson(jsonObject2.getJSONObject("user").toString(), User.class);
                                myModelLatestList.add(new NearBylIst("", jsonObject2.getString("video"), jsonObject2.getString("preview"), jsonObject2.getString("video"), user, jsonObject2.getInt("views_count"), jsonObject2.getInt("likes_count"), jsonObject2.getInt("comments_count"), jsonObject2.getBoolean("comments"), jsonObject2.getBoolean("liked"), jsonObject2.getBoolean("saved"), jsonObject2.getInt("id"), jsonObject2.getString("location"), jsonObject2.getString("screenshot"), jsonObject2.getString("description")));
                            }
                            //nearBylIsts.add(new TrendingList(hashtag, myModelList));
                            nearBylIsts.add(new TrendingList(hashtag, tagImage, tagDesc, totalViewCount, myModelList, myModelLatestList));
                            Log.i("fetchNearbyData", "Listsize:" + String.valueOf(nearBylIsts.size()));
                        }

                        viewAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        // progressDialog.dismiss();
                        loading.setVisibility(View.GONE);
                    }

                    viewAdapter.notifyDataSetChanged();

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    progressDialog.dismiss();
                }
            });
        } catch (Exception e) {
            // progressDialog.dismiss();
            loading.setVisibility(View.GONE);
            e.printStackTrace();
        }
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
                    .load(sliderItem.getImage())
                    .error(R.mipmap.ic_app_icon)
                    .fitCenter()
                    .into(viewHolder.imageViewBackground);

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Toast.makeText(getContext(), "This is item in position " + position, Toast.LENGTH_SHORT).show();
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