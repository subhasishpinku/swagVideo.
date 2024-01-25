package com.swagVideo.in.fragments;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.gson.Gson;
import com.pixplicity.easyprefs.library.Prefs;
import com.swagVideo.in.MainNavigationDirections;
import com.swagVideo.in.R;
import com.swagVideo.in.SharedConstants;
import com.swagVideo.in.activities.MainActivity;
import com.swagVideo.in.activities.TrendingActivity;
import com.swagVideo.in.adapter.RecyclerViewAdapter;
import com.swagVideo.in.ads.BannerAdProvider;
import com.swagVideo.in.data.ClipDataSource;
import com.swagVideo.in.data.api.REST;
import com.swagVideo.in.data.models.Advertisement;
import com.swagVideo.in.data.models.Clip;
import com.swagVideo.in.data.models.User;
import com.swagVideo.in.pojo.NearBylIst;
import com.swagVideo.in.pojo.TrendingList;
import com.swagVideo.in.utils.AdsUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.swagVideo.in.activities.TrendingActivity.trendingTabsFragment;
import static com.swagVideo.in.data.StaticData.placeName;
import static com.swagVideo.in.fragments.NearbyFragment.clipStat;
import static com.swagVideo.in.fragments.TrendingFragment.trendingLists;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TrendFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TrendFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private ProgressDialog progressDialog;
    private RecyclerView rv;
    private RecyclerViewAdapter<NearBylIst> viewAdapter;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Geocoder geocoder;
    private String lat = "", longi = "";
    private Dialog myDialog;
    ArrayList<Clip> myModelList = new ArrayList();

    private ImageButton header_more;
    private TextView header_title;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    String ARG_ADS = "ads";
    String ARG_PARAMS = "params";
    String ARG_TITLE = "title";
    String mTitle;
    Bundle mParams;
    BannerAdProvider mAd;
    boolean mAds;

    public TrendFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NearbyFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TrendFragment newInstance(String param1, String param2) {
        TrendFragment fragment = new TrendFragment();
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
        View view =  inflater.inflate(R.layout.fragment_trend, container, false);

        initView(view);

        rv.setLayoutManager(new GridLayoutManager(getActivity(),3));
        //rv.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        viewAdapter = new RecyclerViewAdapter<>(getActivity(), R.layout.trend_item_layout,trendingLists);
        viewAdapter.setMapper((viewHolder, source) -> {

            ImageView iv = (ImageView) viewHolder.getView(R.id.iv);
            TextView tvView = (TextView) viewHolder.getView(R.id.tvView);
            ImageView ivUser = (ImageView) viewHolder.getView(R.id.iv_user);
            TextView tvUserName = (TextView) viewHolder.getView(R.id.tv_user_name);

//            tvView.setText(source.getViewsCount());

                            iv.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                   /* clipStat.setVideo(source.video);
                                    clipStat.setUser(source.getUser());
                                    clipStat.setViewsCount(source.viewsCount);
                                    clipStat.setLikesCount(source.likesCount);
                                    clipStat.setDescription(source.getDescription());
                                    clipStat.setCommentsCount(source.commentsCount);
                                    clipStat.setComments(source.comments);
                                    clipStat.setSaved(source.saved);
                                    replaceFragment(new NearbyPlayerFragment());*/
                                    Intent intent = new Intent(getActivity(), MainActivity.class);
                                    intent.putExtra("from","nearby");
                                    // startActivity(intent);

                                    try {

                                        Bundle arguments = getArguments();
                                        if (arguments != null) {
                                            mParams = arguments.getBundle(ARG_PARAMS);
                                            mTitle = arguments.getString(ARG_TITLE);
                                            boolean ads = requireArguments().getBoolean(ARG_ADS, false);
                                            if (mAds = ads) {
                                                Advertisement ad = AdsUtil.findByLocationAndType("grid", "banner");
                                                if (ad != null) {
                                                    mAd = new BannerAdProvider(ad);
                                                }
                                            }
                                        }

                                        if (mParams == null) {
                                            mParams = new Bundle();
                                        }

                                        Set<String> languages = Prefs.getStringSet(SharedConstants.PREF_PREFERRED_LANGUAGES, null);
                                        if (languages != null && !languages.isEmpty()) {
                                            mParams.putStringArrayList(ClipDataSource.PARAM_LANGUAGES, new ArrayList<>(languages));
                                        }

                                        ((MainActivity) requireActivity()).showPlayerSlider(source.getId(), mParams);

                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            });

             tvView.setText(String.valueOf(source.viewsCount));
             tvUserName.setText(String.valueOf(source.getUser().name));
             Glide.with(this).load(source.getGif()).fitCenter().error(R.mipmap.ic_app_icon).into(iv);
             Glide.with(this).load(source.getUser().photo).apply(new RequestOptions().circleCrop()).error(R.drawable.user).into(ivUser);

        });
        rv.setAdapter(viewAdapter);
        viewAdapter.notifyDataSetChanged();

        geocoder = new Geocoder(getActivity(), Locale.getDefault());

        locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try {
                    lat = String.valueOf(location.getLatitude());
                    longi = String.valueOf(location.getLongitude());
                    // Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                    List<Address> list =geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                    if(list != null && list.size() >0){
                        Log.i("place", list.toString());
                        Log.i("lati", lat+longi);
                      //  tvLocation.setText(list.get(0).);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        return view;

    }

    private void initView(View view) {
        rv = view.findViewById(R.id.rv);

    }
    public void replaceFragment (Fragment fragment){
        ((TrendingActivity) getContext()).replaceFragment(fragment);
    }

}