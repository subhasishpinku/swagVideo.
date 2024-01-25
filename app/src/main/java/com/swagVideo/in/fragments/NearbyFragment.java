package com.swagVideo.in.fragments;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.lang.UScript;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cunoraz.gifview.library.GifView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.gson.Gson;
import com.pixplicity.easyprefs.library.Prefs;
import com.swagVideo.in.R;
import com.swagVideo.in.SharedConstants;
import com.swagVideo.in.activities.MainActivity;
import com.swagVideo.in.activities.NearByVideoActivity;
import com.swagVideo.in.activities.PlacesActivity;
import com.swagVideo.in.activities.UploadActivity;
import com.swagVideo.in.adapter.RecyclerViewAdapter;
import com.swagVideo.in.ads.BannerAdProvider;
import com.swagVideo.in.data.ClipDataSource;
import com.swagVideo.in.data.api.REST;
import com.swagVideo.in.data.models.Advertisement;
import com.swagVideo.in.data.models.Clip;
import com.swagVideo.in.data.models.Song;
import com.swagVideo.in.data.models.User;
import com.swagVideo.in.pojo.NearBylIst;
import com.swagVideo.in.utils.AdsUtil;
import com.whygraphics.gifview.gif.GIFView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.swagVideo.in.data.StaticData.latitude;
import static com.swagVideo.in.data.StaticData.longitude;
import static com.swagVideo.in.data.StaticData.placeName;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NearbyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NearbyFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private ProgressDialog progressDialog;
    private RecyclerViewAdapter<NearBylIst> viewAdapter;
    private RecyclerView rv;
    private ArrayList<NearBylIst> nearBylIsts = new ArrayList<>();
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Geocoder geocoder;
   // private String lat = "", longi = "";
    private double lat, longi;
    private Dialog myDialog;
    private TextView tvLocation, tvChange,tvNoVideo;
    public static Clip clipStat = new Clip();
    public static ArrayList<Clip> myModelList = new ArrayList();
    private ProgressBar loading;

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

    public NearbyFragment() {
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
    public static NearbyFragment newInstance(String param1, String param2) {
        NearbyFragment fragment = new NearbyFragment();
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
        View view =  inflater.inflate(R.layout.fragment_nearby, container, false);
        placeName = "";

        initView(view);


//        nearBylIsts.add(new NearBylIst(R.drawable.people,"0.2", "http://goo.gl/gEgYUd","https://mir-s3-cdn-cf.behance.net/project_modules/max_1200/f19c6c63077653.5aa65266cb14d.gif"));
//        nearBylIsts.add(new NearBylIst(R.drawable.people,"1.2", "http://goo.gl/gEgYUd","https://mir-s3-cdn-cf.behance.net/project_modules/max_1200/f19c6c63077653.5aa65266cb14d.gif"));
//        nearBylIsts.add(new NearBylIst(R.drawable.people,"1.5", "http://goo.gl/gEgYUd","https://mir-s3-cdn-cf.behance.net/project_modules/max_1200/f19c6c63077653.5aa65266cb14d.gif"));
//        nearBylIsts.add(new NearBylIst(R.drawable.people,"1.2", "http://goo.gl/gEgYUd","https://mir-s3-cdn-cf.behance.net/project_modules/max_1200/f19c6c63077653.5aa65266cb14d.gif"));
        rv.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        viewAdapter = new RecyclerViewAdapter<>(getActivity(), R.layout.nearby_layout,nearBylIsts);
        viewAdapter.setMapper((viewHolder, source) -> {

            CircleImageView ivUser = (CircleImageView) viewHolder.getView(R.id.ivUser);
            TextView tvKm = (TextView) viewHolder.getView(R.id.tvKm);
            TextView tvUserName = (TextView) viewHolder.getView(R.id.tv_user_name);
            GIFView gifv = (GIFView) viewHolder.getView(R.id.gifv);

            gifv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   // Toast.makeText(getActivity(), "Work in progress", Toast.LENGTH_SHORT).show();
                    /*Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.putExtra("from","nearby");
                    startActivity(intent);*/
                    /*clipStat.setVideo(source.video);
                    clipStat.setUser(source.getUser());
                    clipStat.setViewsCount(source.viewsCount);
                    clipStat.setLikesCount(source.likesCount);
                    clipStat.setCommentsCount(source.commentsCount);
                    clipStat.setDescription(source.description);
                    clipStat.setComments(source.comments);
                    clipStat.setSaved(source.saved);*/
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.putExtra("from","nearby");
                   // startActivity(intent);
                    /*Bundle args = new Bundle();
                    //  args.putString("joinAs", "");
                    Fragment fr = new PlayerFragment();
                    fr.setArguments(args);
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.host, fr).addToBackStack("dd").commit();*/


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

                     }
            });

            tvKm.setText(source.getKm()+" Km");
            tvUserName.setText(source.getUser().name);

            Glide.with(this).load(source.getUser().photo).fitCenter().error(R.drawable.photo_placeholder).into(ivUser);
            Glide.with(this).load(source.getGif()).fitCenter().into(gifv);

        });
        rv.setAdapter(viewAdapter);
        viewAdapter.notifyDataSetChanged();

        geocoder = new Geocoder(getActivity(), Locale.getDefault());

        locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try {
                    lat = location.getLatitude();
                    longi = location.getLongitude();
                    // Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                    List<Address> list =geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                    if(list != null && list.size() >0){
                        Log.i("place", list.toString());
                        tvLocation.setText(list.get(0).getSubAdminArea()+", "+list.get(0).getAdminArea());
                       // Log.i("lati", lat+longi);
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

        /*if(Build.VERSION.SDK_INT <23){
            startListening();
        }else {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 100, locationListener);
                Location location1 = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location1 != null) {
                    try {
                        lat = String.valueOf(location1.getLatitude());
                        longi = String.valueOf(location1.getLongitude());
                        Log.i("latiNetwork", lat+longi);
                        //Geocoder geocoder = new Geocoder(HomeFragment.this.getContext(), Locale.getDefault());
                        List<Address> list =geocoder.getFromLocation(location1.getLatitude(),location1.getLongitude(),1);
                        if(list != null && list.size() >0){
                            Log.i("place", list.toString());
                            // tv_location.setText(list.get(0).getLocality()+", "+list.get(0).getAdminArea());
                        }
                       // fetchNearByData(lat,longi);
                       // fetchNearByData("31.31000000","54.57000000");

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }*/
        tvChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PlacesActivity.class);
                startActivity(intent);
            }
        });
// GET: https://project.primacyinfotech.com/SwagVideo/api/get-radius-video?latitude=31.31000000&longitude=54.57000000

        return view;

    }

    private void initView(View view) {
        rv = view.findViewById(R.id.rv);
        tvLocation = view.findViewById(R.id.tvLocation);
        tvChange = view.findViewById(R.id.tvChange);
        tvNoVideo = view.findViewById(R.id.tvNoVideo);
        loading = view.findViewById(R.id.loading);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Toast.makeText(getActivity(), "Here ", Toast.LENGTH_SHORT).show();
        if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            startListening();
        }else {

        }
    }

    public void startListening(){

        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
           // Toast.makeText(getActivity(), "location ", Toast.LENGTH_SHORT).show();

        }
    }
    public void fetchNearByData(String latf, String longif){
    /*    progressDialog = new ProgressDialog(getActivity());
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);*/
        loading.setVisibility(View.VISIBLE);
        nearBylIsts.clear();
        myModelList.clear();
        Log.i("fetchNearbyData",Prefs.getString(SharedConstants.PREF_SERVER_TOKEN,""));
        Retrofit retrofit =  new Retrofit.Builder()
                .baseUrl(getResources().getString(R.string.server_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        REST api = retrofit.create(REST.class);
        try {
            Call<ResponseBody> call = api.getNearbyUsers("Bearer " + Prefs.getString(SharedConstants.PREF_SERVER_TOKEN,""),latf, longif);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.i("fetchNearbyData",response.body().toString());
                    /*if (response.body().getAck()==1) {
                        NearBylIst nearBylIst = response.body();
                        nearBylIsts.addAll(nearBylIst.getOrderData());
                        viewAdapter.notifyDataSetChanged();

                    }else {
                        Toast.makeText(getActivity(), response.body().getMsg(), Toast.LENGTH_LONG).show();
                    }*/
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        Gson gson = new Gson();
                        myModelList = gson.fromJson(String.valueOf(jsonArray),ArrayList.class);
                        for (int i=0; i<jsonArray.length();i++){
                            JSONObject jsonObject1 = new JSONObject(jsonArray.getString(i));
                            double latU = Double.parseDouble(jsonObject1.getString("latitude"));
                            double lonU = Double.parseDouble(jsonObject1.getString("longitude"));
                            User user = new User();
                            user = (User) gson.fromJson(jsonObject1.getJSONObject("user").toString(), User.class);
                            /*Song song = new Song();
                            song = (Song) gson.fromJson(jsonObject1.getJSONObject("song").toString(), Song.class);

                            Log.i("userClass", jsonObject1.getJSONObject("user").toString());*/
                           /* JSONArray jsonArrayhastags =  jsonObject1.getJSONArray("hashtags");
                            ArrayList<String> hashtags = new ArrayList<>();

                            for (int j=0; j<jsonArrayhastags.size(); j++){

                               // hashtags.add("");
                            }
                            hashtags = gson.fromJson(jsonObject1.getJSONArray("hashtags").toString(), hashtags);*/

                           // nearBylIsts.add(new NearBylIst(distance(Double.parseDouble(lat), Double.parseDouble(longi), Double.valueOf(lat2), Double.valueOf(lon2)),jsonObject1.getString("video"),"https://mir-s3-cdn-cf.behance.net/project_modules/max_1200/f19c6c63077653.5aa65266cb14d.gif"));
//                            nearBylIsts.add(new NearBylIst(distance(Double.parseDouble(lat), Double.parseDouble(longi), Double.valueOf(lat2), Double.valueOf(lon2)),jsonObject1.getString("video"),"https://mir-s3-cdn-cf.behance.net/project_modules/max_1200/f19c6c63077653.5aa65266cb14d.gif",jsonObject1.getString("video"), user,jsonObject1.getInt("views_count"), jsonObject1.getInt("likes_count"),jsonObject1.getInt("comments_count"),jsonObject1.getBoolean("comments"),jsonObject1.getBoolean("liked"), jsonObject1.getBoolean("saved")));
                            nearBylIsts.add(new NearBylIst(distance( lat, longi, latU, lonU),jsonObject1.getString("video"),
                                    jsonObject1.getString("preview"),jsonObject1.getString("video"), user,jsonObject1.getInt("views_count"),
                                    jsonObject1.getInt("likes_count"),jsonObject1.getInt("comments_count"),jsonObject1.getBoolean("comments"),
                                    jsonObject1.getBoolean("liked"), jsonObject1.getBoolean("saved"),jsonObject1.getInt("id"),
                                    jsonObject1.getString("location"),jsonObject1.getString("description")));
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                    if (nearBylIsts.size()==0){
                        rv.setVisibility(View.GONE);
                        tvNoVideo.setVisibility(View.VISIBLE);
                    }else {
                        rv.setVisibility(View.VISIBLE);
                        tvNoVideo.setVisibility(View.GONE);
                    }
                    loading.setVisibility(View.GONE);
                    viewAdapter.notifyDataSetChanged();

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    loading.setVisibility(View.GONE);
                }
            });
        }catch (Exception e){
//            progressDialog.dismiss();
            loading.setVisibility(View.GONE);
            e.printStackTrace();
        }
    }
    private String distance(double lat1, double lon1, double lat2, double lon2) {
        //Calculating distance
        double earthRadius = 3958.75;

        double dLat = Math.toRadians(lat1-lat2);
        double dLng = Math.toRadians(lon1-lon2);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(lat1)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = earthRadius * c;

        /*Location startPoint=new Location("locationA");
        startPoint.setLatitude(17.372102);
        startPoint.setLongitude(78.484196);

        Location endPoint=new Location("locationA");
        endPoint.setLatitude(17.375775);
        endPoint.setLongitude(78.469218);

        double distance=startPoint.distanceTo(endPoint);*/
        dist = dist/10000.0;

        return String.valueOf(dist);
    }
    private void showDialogPlaces(){
        myDialog = new Dialog(getActivity());
        myDialog.setContentView(R.layout.layout_places);


        EditText etPlace = myDialog.findViewById(R.id.etPlace);
        EditText tvview1 = myDialog.findViewById(R.id.tvview1);
        EditText tvview2 = myDialog.findViewById(R.id.tvview2);
        //TextView tv_ok = myDialog.findViewById(R.id.tv_ok);

        etPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Place.Field>  fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);

                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,fieldList).build(getActivity());
                startActivityForResult(intent, 100);
            }
        });
        myDialog.show();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (!placeName.equals("")){
            tvLocation.setText(placeName);
            //fetchNearByData("31.31000000","54.57000000");
            placeName="";
           fetchNearByData(latitude, longitude);//31.31000000&longitude=54.57000000

        }else {
           // Toast.makeText(getActivity(), "Near by onResume", Toast.LENGTH_SHORT).show();
            if(Build.VERSION.SDK_INT <23){
                startListening();
            }else {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                } else {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 100, locationListener);
                    Location location1 = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location1 != null) {
                        try {
                            lat = location1.getLatitude();
                            longi = location1.getLongitude();
                            //Log.i("latiNetwork", lat+longi);
                            //Geocoder geocoder = new Geocoder(HomeFragment.this.getContext(), Locale.getDefault());
                            List<Address> list =geocoder.getFromLocation(location1.getLatitude(),location1.getLongitude(),1);
                            if(list != null && list.size() >0){
                                Log.i("place", list.toString());
                                // tv_location.setText(list.get(0).getLocality()+", "+list.get(0).getAdminArea());
                                tvLocation.setText(list.get(0).getLocality()+", "+list.get(0).getAdminArea());
                            }
                             fetchNearByData(String.valueOf(lat),String.valueOf(longi));
                            // fetchNearByData("31.31000000","54.57000000");

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}