package com.swagVideo.in.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.data.DataHolder;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.jakewharton.rxbinding4.widget.RxTextView;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.rxjava3.disposables.Disposable;
import com.swagVideo.in.MainApplication;
import com.swagVideo.in.R;
import com.swagVideo.in.SharedConstants;
import com.swagVideo.in.activities.MainActivity;
import com.swagVideo.in.common.LoadingState;
import com.swagVideo.in.data.api.REST;
import com.swagVideo.in.data.models.Clip;
import com.swagVideo.in.data.models.Wrappers;
import com.swagVideo.in.utils.AutocompleteUtil;
import com.swagVideo.in.utils.SocialSpanUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditClipFragment extends Fragment {

    private static final String ARG_CLIP = "clip";
    private static final String TAG = "EditClipFragment";

    private int mClip;
    private final List<Disposable> mDisposables = new ArrayList<>();
    private EditClipFragmentViewModel mModel;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Geocoder geocoder;
    private String lat = "", longi = "";

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == SharedConstants.REQUEST_CODE_PICK_LOCATION && resultCode == Activity.RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            setLocation(place);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClip = requireArguments().getInt(ARG_CLIP);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_clip, container, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (Disposable disposable : mDisposables) {
            disposable.dispose();
        }

        mDisposables.clear();
    }

    @Override
    public void onResume() {
        super.onResume();
        LoadingState state = mModel.state.getValue();
        Clip clip = mModel.clip.getValue();
        if (clip == null && state != LoadingState.LOADING) {
            loadClip();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mModel = new ViewModelProvider(this).get(EditClipFragmentViewModel.class);
        ImageButton back = view.findViewById(R.id.header_back);
        back.setOnClickListener(v -> ((MainActivity)requireActivity()).popBackStack());
        TextView title = view.findViewById(R.id.header_title);
        title.setText(R.string.edit_label);
        ImageButton done = view.findViewById(R.id.header_more);
        done.setImageResource(R.drawable.ic_baseline_check_24);
        done.setOnClickListener(v -> updateWithServer());
        TextInputLayout location = view.findViewById(R.id.location);
        location.getEditText().setText(mModel.location);
        Disposable disposable;
        //noinspection ConstantConditions
        disposable = RxTextView.afterTextChangeEvents(location.getEditText())
                .skipInitialValue()
                .subscribe(e -> {
                    if (TextUtils.isEmpty(e.getEditable())) {
                        mModel.location = null;
                        mModel.latitude = null;
                        mModel.longitude = null;
                    }
                });
        mDisposables.add(disposable);
        if (!getResources().getBoolean(R.bool.locations_enabled)) {
            location.setVisibility(View.GONE);
        }
        location.setVisibility(View.GONE);
        location.setEndIconOnClickListener(v -> pickLocation());
        TextInputLayout description = view.findViewById(R.id.description);
        description.getEditText().setText(mModel.description);
        disposable = RxTextView.afterTextChangeEvents(description.getEditText())
                .skipInitialValue()
                .subscribe(e -> {
                    Editable editable = e.getEditable();
                    mModel.description = editable != null ? editable.toString() : null;
                });
        mDisposables.add(disposable);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.language_names, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner language = view.findViewById(R.id.language);
        language.setAdapter(adapter);
        List<String> codes = Arrays.asList(
                getResources().getStringArray(R.array.language_codes)
        );
        language.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mModel.language = codes.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        language.setSelection(codes.indexOf(mModel.language));
        SwitchMaterial isPrivate = view.findViewById(R.id.private2);
        isPrivate.setChecked(mModel.isPrivate);
        isPrivate.setOnCheckedChangeListener((button, checked) -> mModel.isPrivate = checked);
        SwitchMaterial hasComments = view.findViewById(R.id.comments);
        hasComments.setChecked(mModel.hasComments);
        hasComments.setOnCheckedChangeListener((button, checked) -> mModel.hasComments = checked);
        mModel.errors.observe(getViewLifecycleOwner(), errors -> {
            description.setError(null);
            isPrivate.setError(null);
            hasComments.setError(null);
            if (errors == null) {
                return;
            }

            if (errors.containsKey("location")) {
                location.setError(errors.get("location"));
            }

            if (errors.containsKey("description")) {
                description.setError(errors.get("description"));
            }

            if (errors.containsKey("private")) {
                isPrivate.setError(errors.get("private"));
            }

            if (errors.containsKey("comments")) {
                hasComments.setError(errors.get("comments"));
            }
        });
        View content = view.findViewById(R.id.content);
        View loading = view.findViewById(R.id.loading);
        mModel.state.observe(getViewLifecycleOwner(), state -> {
            loading.setVisibility(state == LoadingState.LOADING ? View.VISIBLE : View.GONE);
            content.setVisibility(state == LoadingState.LOADED ? View.VISIBLE : View.GONE);
        });
        mModel.clip.observe(getViewLifecycleOwner(), clip -> {
            location.getEditText().setText(mModel.location = clip.location);
            mModel.latitude = clip.latitude;
            mModel.longitude = clip.longitude;
            description.getEditText().setText(mModel.description = clip.description);
            language.setSelection(codes.indexOf(mModel.language = clip.language));
            isPrivate.setChecked(mModel.isPrivate = clip._private);
            hasComments.setChecked(mModel.hasComments = clip.comments);
        });
        EditText input = description.getEditText();
        SocialSpanUtil.apply(input, mModel.description, null);
        if (getResources().getBoolean(R.bool.autocomplete_enabled)) {
            AutocompleteUtil.setupForHashtags(requireContext(), input);
            AutocompleteUtil.setupForUsers(requireContext(), input);
        }

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
                        //tv_location.setText(list.get(0).getLocality()+", "+list.get(0).getAdminArea());
                        Log.i("lati", lat+longi);

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

        if(Build.VERSION.SDK_INT <23){
            startListening();
        }else {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 100, locationListener);
                Location location1 = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null) {
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
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    private void loadClip() {
        mModel.state.setValue(LoadingState.LOADING);
        REST rest = MainApplication.getContainer().get(REST.class);
        rest.clipsShow(mClip)
                .enqueue(new Callback<Wrappers.Single<Clip>>() {

                    @Override
                    public void onResponse(
                            @Nullable Call<Wrappers.Single<Clip>> call,
                            @Nullable Response<Wrappers.Single<Clip>> response
                    ) {
                        int code = response != null ? response.code() : -1;
                        Log.v(TAG, "Fetching clip returned " + code + '.');
                        if (response != null && response.isSuccessful()) {
                            //noinspection ConstantConditions
                            Clip clip = response.body().data;
                            mModel.clip.setValue(clip);
                            mModel.state.setValue(LoadingState.LOADED);
                        } else {
                            mModel.state.setValue(LoadingState.ERROR);
                        }
                    }

                    @Override
                    public void onFailure(
                            @Nullable Call<Wrappers.Single<Clip>> call,
                            @Nullable Throwable t
                    ) {
                        Log.e(TAG, "Failed when trying to fetch clip.", t);
                        mModel.state.setValue(LoadingState.ERROR);
                    }
                });
    }

    public static EditClipFragment newInstance(Clip clip) {
        Bundle arguments = new Bundle();
        arguments.putParcelable(ARG_CLIP, clip);
        EditClipFragment fragment = new EditClipFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    private void pickLocation() {
        List<Place.Field> fields =
                Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .build(requireContext());
        startActivityForResult(intent, SharedConstants.REQUEST_CODE_PICK_LOCATION);
    }

    private void setLocation(Place place) {
        Log.v(TAG, "User chose " + place.getId() + " place.");
        mModel.location = place.getName();
        mModel.latitude = place.getLatLng().latitude;
        mModel.longitude = place.getLatLng().longitude;
        TextInputLayout location = getView().findViewById(R.id.location);
        location.getEditText().setText(mModel.location);
    }

    private void showErrors(JSONObject json) throws Exception {
        JSONObject errors = json.getJSONObject("errors");
        Map<String, String> messages = new HashMap<>();
        String[] keys = new String[]{"description", "language", "private", "comments", "location", "latitude", "longitude"};
        for (String key : keys) {
            JSONArray fields = errors.optJSONArray(key);
            if (fields != null) {
                messages.put(key, fields.getString(0));
            }
        }

        mModel.errors.postValue(messages);
    }

    private void updateWithServer() {
        KProgressHUD progress = KProgressHUD.create(requireActivity())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel(getString(R.string.progress_title))
                .setCancellable(false)
                .show();
        mModel.errors.postValue(null);
        REST rest = MainApplication.getContainer().get(REST.class);
        Call<Wrappers.Single<Clip>> call = rest.clipsUpdate(
                mClip,
                mModel.description,
                mModel.language,
                mModel.isPrivate ? 1 : 0,
                mModel.hasComments ? 1 : 0,
                mModel.location,
                mModel.latitude,
                mModel.longitude
               // Double.valueOf(lat),
               // Double.valueOf(longi)
        );
        call.enqueue(new Callback<Wrappers.Single<Clip>>() {

            @Override
            public void onResponse(
                    @Nullable Call<Wrappers.Single<Clip>> call,
                    @Nullable Response<Wrappers.Single<Clip>> response
            ) {
                progress.dismiss();
                if (response != null) {
                    if (response.isSuccessful()) {
                        Toast.makeText(requireContext(), R.string.message_clip_updated, Toast.LENGTH_SHORT).show();
                        ((MainActivity)requireActivity()).popBackStack();
                    } else if (response.code() == 422) {
                        try {
                            String content = response.errorBody().string();
                            showErrors(new JSONObject(content));
                        } catch (Exception ignore) {
                        }
                    }
                }
            }

            @Override
            public void onFailure(
                    @Nullable Call<Wrappers.Single<Clip>> call,
                    @Nullable Throwable t
            ) {
                Log.e(TAG, "Failed when trying to update clip.", t);
                progress.dismiss();
            }
        });
    }

    public static class EditClipFragmentViewModel extends ViewModel {

        public String description;
        public String language;
        public boolean isPrivate;
        public boolean hasComments;

        public String location;
        public Double latitude;
        public Double longitude;

        public final MutableLiveData<Map<String, String>> errors = new MutableLiveData<>();
        public final MutableLiveData<LoadingState> state = new MutableLiveData<>();
        public final MutableLiveData<Clip> clip = new MutableLiveData<>();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            startListening();
        }else {

        }
    }

    public void startListening(){

        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
            Toast.makeText(getActivity(), "location ", Toast.LENGTH_SHORT).show();

        }
    }

}
