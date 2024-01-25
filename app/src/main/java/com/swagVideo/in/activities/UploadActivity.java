package com.swagVideo.in.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.os.ConfigurationCompat;
import androidx.core.os.LocaleListCompat;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.jakewharton.rxbinding4.widget.RxTextView;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.disposables.Disposable;
import com.swagVideo.in.MainApplication;
import com.swagVideo.in.R;
import com.swagVideo.in.SharedConstants;
import com.swagVideo.in.data.dbs.ClientDatabase;
import com.swagVideo.in.data.entities.Draft;
import com.swagVideo.in.utils.AutocompleteUtil;
import com.swagVideo.in.utils.LocaleUtil;
import com.swagVideo.in.utils.SocialSpanUtil;
import com.swagVideo.in.utils.TempUtil;
import com.swagVideo.in.utils.VideoUtil;
import com.swagVideo.in.workers.FixFastStartWorker2;
import com.swagVideo.in.workers.GeneratePreviewWorker;
import com.swagVideo.in.workers.UploadClipWorker;

public class UploadActivity extends AppCompatActivity {

    public static final String EXTRA_DRAFT = "draft";
    public static final String EXTRA_SONG_ID = "song_id";
    public static final String EXTRA_VIDEO = "video";
    private static final String TAG = "UploadActivity";
    private static final String WORK_UPLOAD = "upload";

    private final List<Disposable> mDisposables = new ArrayList<>();
    private boolean mDeleteOnExit = true;
    private Draft mDraft;
    private String mVideo;
    private UploadActivityViewModel mModel;
    private int mSongId;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Geocoder geocoder;
    private double lat , longi ;
    private String locality="";
    private String tag="";
    private EditText tvHashtag;
   // private double dll;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleUtil.wrap(base));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == SharedConstants.REQUEST_CODE_PICK_LOCATION && resultCode == Activity.RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            setLocation(place);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        mDraft = getIntent().getParcelableExtra(EXTRA_DRAFT);
        if (mDraft != null) {
            ImageButton delete = findViewById(R.id.header_back);
            delete.setImageResource(R.drawable.ic_baseline_delete_forever_24);
            delete.setOnClickListener(view -> deleteDraft());
        } else {
            ImageButton close = findViewById(R.id.header_back);
            close.setImageResource(R.drawable.ic_baseline_close_24);
            close.setOnClickListener(view -> finish());
        }

        TextView title = findViewById(R.id.header_title);
        title.setText(R.string.upload_label);
        ImageButton done = findViewById(R.id.header_more);
        done.setImageResource(R.drawable.ic_baseline_check_24);
       // tvHashtag = findViewById(R.id.tvHashtag);
        //  tvHashtag.getEditText().setText(mModel.tag);

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* tag="";
                tag = tvHashtag.getText().toString();
                if (tag.equals(""))
                    tvHashtag.setError("Please enter HASHTAG");
                else*/
                    uploadToServer();
            }
        });
        mModel = new ViewModelProvider(this).get(UploadActivityViewModel.class);
        if (mDraft != null) {
            mSongId = mDraft.songId != null ? mDraft.songId : -1;
            mVideo = mDraft.video;
            mModel.description = mDraft.description;
            mModel.language = mDraft.language;
            mModel.isPrivate = mDraft.isPrivate;
            mModel.hasComments = mDraft.hasComments;
            mModel.location = mDraft.location;
            mModel.latitude = mDraft.latitude;
            mModel.longitude = mDraft.longitude;
            mModel.tag = mDraft.tag;
        } else {
            mSongId = getIntent().getIntExtra(EXTRA_SONG_ID, -1);
            mVideo = getIntent().getStringExtra(EXTRA_VIDEO);
        }

        Bitmap image = VideoUtil.getFrameAtTime(mVideo, TimeUnit.SECONDS.toMicros(3));
        ImageView thumbnail = findViewById(R.id.thumbnail);
        thumbnail.setImageBitmap(image);
        TextInputLayout location = findViewById(R.id.location);

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

        location.setEndIconOnClickListener(v -> {
            WorkManager wm = WorkManager.getInstance(this);
            List<WorkInfo> infos = new ArrayList<>();
            try {
                infos = wm.getWorkInfosForUniqueWork(WORK_UPLOAD).get();
            } catch (Exception ignore) {
            }

            boolean pending = false;
            for (WorkInfo info : infos) {
                boolean ended = info.getState() == WorkInfo.State.CANCELLED
                        || info.getState() == WorkInfo.State.FAILED
                        || info.getState() == WorkInfo.State.SUCCEEDED;
                if (!ended) {
                    pending = true;
                    break;
                }
            }

            if (pending) {
                Toast.makeText(UploadActivity.this, R.string.message_uploading, Toast.LENGTH_SHORT).show();
            } else {
                pickLocation();
            }
        });
        TextInputLayout description = findViewById(R.id.description);
        description.getEditText().setText(mModel.description);
        disposable = RxTextView.afterTextChangeEvents(description.getEditText())
                .skipInitialValue()
                .subscribe(e -> {
                    Editable editable = e.getEditable();
                    mModel.description = editable != null ? editable.toString() : null;
                });
        mDisposables.add(disposable);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.language_names, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner language = findViewById(R.id.language);
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
        if (TextUtils.isEmpty(mModel.language)) {
            LocaleListCompat locales =
                    ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration());
            String locale = locales.get(0).getISO3Language();
            if (codes.contains(locale)) {
                mModel.language = locale;
            } else {
                mModel.language = codes.get(0);
            }
        }

        language.setSelection(codes.indexOf(mModel.language));
        thumbnail.setOnClickListener(v -> {
            Intent intent = new Intent(this, PreviewActivity.class);
            intent.putExtra(PreviewActivity.EXTRA_VIDEO, mVideo);
            startActivity(intent);
        });
        SwitchMaterial isPrivate = findViewById(R.id.private2);
        isPrivate.setChecked(mModel.isPrivate);
        isPrivate.setOnCheckedChangeListener((button, checked) -> mModel.isPrivate = checked);
        SwitchMaterial hasComments = findViewById(R.id.comments);
        hasComments.setChecked(mModel.hasComments);
        hasComments.setOnCheckedChangeListener((button, checked) -> mModel.hasComments = checked);
        EditText input = description.getEditText();
        SocialSpanUtil.apply(input, mModel.description, null);
        if (getResources().getBoolean(R.bool.autocomplete_enabled)) {
            AutocompleteUtil.setupForHashtags(this, input);
            AutocompleteUtil.setupForUsers(this, input);
        }

        geocoder = new Geocoder(UploadActivity.this, Locale.getDefault());

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
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
                        //tv_location.setText(list.get(0).getLocality()+", "+list.get(0).getAdminArea());


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
            if (ContextCompat.checkSelfPermission(UploadActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(UploadActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 100, locationListener);
                Location location1 = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location1 != null) {
                    try {
                        lat = location1.getLatitude();
                        longi = location1.getLongitude();


                        //Geocoder geocoder = new Geocoder(HomeFragment.this.getContext(), Locale.getDefault());
                        List<Address> list =geocoder.getFromLocation(location1.getLatitude(),location1.getLongitude(),1);
                        if(list != null && list.size() >0){
                            Log.i("place", list.toString());
                            locality = list.get(0).getLocality();
                            // tv_location.setText(list.get(0).getLocality()+", "+list.get(0).getAdminArea());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
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

        if(ContextCompat.checkSelfPermission(UploadActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            Toast.makeText(UploadActivity.this, "location ", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (Disposable disposable : mDisposables) {
            disposable.dispose();
        }

        mDisposables.clear();
        if (mDeleteOnExit && mDraft == null) {
            File video = new File(mVideo);
            if (!video.delete()) {
                Log.w(TAG, "Could not delete input video: " + video);
            }
        }
    }

    private void deleteDraft() {
        new MaterialAlertDialogBuilder(this)
                .setMessage(R.string.confirmation_delete_draft)
                .setNegativeButton(R.string.cancel_button, (dialog, i) -> dialog.cancel())
                .setPositiveButton(R.string.yes_button, (dialog, i) -> {
                    dialog.dismiss();
                    FileUtils.deleteQuietly(new File(mDraft.preview));
                    FileUtils.deleteQuietly(new File(mDraft.screenshot));
                    FileUtils.deleteQuietly(new File(mDraft.video));
                    ClientDatabase db = MainApplication.getContainer().get(ClientDatabase.class);
                    db.drafts().delete(mDraft);
                    setResult(RESULT_OK);
                    finish();
                })
                .show();
    }

    private void pickLocation() {
        List<Place.Field> fields =
                Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .build(this);
        startActivityForResult(intent, SharedConstants.REQUEST_CODE_PICK_LOCATION);
    }

    private void setLocation(Place place) {
        Log.v(TAG, "User chose " + place.getId() + " place.");
        mModel.location = place.getName();
        mModel.latitude = place.getLatLng().latitude;
        mModel.longitude = place.getLatLng().longitude;
        TextInputLayout location = findViewById(R.id.location);
        location.getEditText().setText(mModel.location);
    }

    private void uploadToServer() {
        WorkManager wm = WorkManager.getInstance(this);
        OneTimeWorkRequest request;
        if (mDraft != null) {
            Data data = new Data.Builder()
                    .putInt(UploadClipWorker.KEY_SONG, mSongId)
                    .putString(UploadClipWorker.KEY_VIDEO, mDraft.video)
                    .putString(UploadClipWorker.KEY_SCREENSHOT, mDraft.screenshot)
                    .putString(UploadClipWorker.KEY_PREVIEW, mDraft.preview)
                    .putString(UploadClipWorker.KEY_DESCRIPTION, mModel.description)
                    .putString(UploadClipWorker.KEY_LANGUAGE, mModel.language)
                    .putBoolean(UploadClipWorker.KEY_PRIVATE, mModel.isPrivate)
                    .putBoolean(UploadClipWorker.KEY_COMMENTS, mModel.hasComments)
                    .putString(UploadClipWorker.KEY_LOCATION, locality)
                    .putDouble(UploadClipWorker.KEY_LATITUDE, lat)
                    .putDouble(UploadClipWorker.KEY_LONGITUDE, longi)
                 //   .putString(UploadClipWorker.KEY_TAG, tag)
                    .build();
            request = new OneTimeWorkRequest.Builder(UploadClipWorker.class)
                    .setInputData(data)
                    .build();
            wm.enqueue(request);
            ClientDatabase db = MainApplication.getContainer().get(ClientDatabase.class);
            db.drafts().delete(mDraft);
        } else {
            File video = TempUtil.createNewFile(getFilesDir(), ".mp4");
            File preview = TempUtil.createNewFile(getFilesDir(), ".gif");
            File screenshot = TempUtil.createNewFile(getFilesDir(), ".png");
            Data data1 = new Data.Builder()
                    .putString(FixFastStartWorker2.KEY_INPUT, mVideo)
                    .putString(FixFastStartWorker2.KEY_OUTPUT, video.getAbsolutePath())
                    .build();
            OneTimeWorkRequest request1 = new OneTimeWorkRequest.Builder(FixFastStartWorker2.class)
                    .setInputData(data1)
                    .build();
            Data data2 = new Data.Builder()
                    .putString(GeneratePreviewWorker.KEY_INPUT, video.getAbsolutePath())
                    .putString(GeneratePreviewWorker.KEY_SCREENSHOT, screenshot.getAbsolutePath())
                    .putString(GeneratePreviewWorker.KEY_PREVIEW, preview.getAbsolutePath())
                    .build();
            OneTimeWorkRequest request2 = new OneTimeWorkRequest.Builder(GeneratePreviewWorker.class)
                    .setInputData(data2)
                    .build();
            Data data3 = new Data.Builder()
                    .putInt(UploadClipWorker.KEY_SONG, mSongId)
                    .putString(UploadClipWorker.KEY_VIDEO, video.getAbsolutePath())
                    .putString(UploadClipWorker.KEY_SCREENSHOT, screenshot.getAbsolutePath())
                    .putString(UploadClipWorker.KEY_PREVIEW, preview.getAbsolutePath())
                    .putString(UploadClipWorker.KEY_DESCRIPTION, mModel.description)
                    .putString(UploadClipWorker.KEY_LANGUAGE, mModel.language)
                    .putBoolean(UploadClipWorker.KEY_PRIVATE, mModel.isPrivate)
                    .putBoolean(UploadClipWorker.KEY_COMMENTS, mModel.hasComments)
                    .putString(UploadClipWorker.KEY_LOCATION, locality)
                    .putDouble(UploadClipWorker.KEY_LATITUDE, lat)
                    .putDouble(UploadClipWorker.KEY_LONGITUDE, longi)
                   // .putString(UploadClipWorker.KEY_TAG, tag)
                    .build();
            request = new OneTimeWorkRequest.Builder(UploadClipWorker.class)
                    .setInputData(data3)
                    .build();
            wm.beginUniqueWork(WORK_UPLOAD, ExistingWorkPolicy.REPLACE, request1)
                    .then(request2)
                    .then(request)
                    .enqueue();
        }

        if (getResources().getBoolean(R.bool.uploads_async_enabled)) {
            mDeleteOnExit = false;
            Toast.makeText(this, R.string.uploading_message, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            KProgressHUD progress = KProgressHUD.create(this)
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setLabel(getString(R.string.progress_title))
                    .setCancellable(false)
                    .show();
            wm.getWorkInfoByIdLiveData(request.getId())
                    .observe(this, info -> {
                        boolean ended = info.getState() == WorkInfo.State.CANCELLED
                                || info.getState() == WorkInfo.State.FAILED
                                || info.getState() == WorkInfo.State.SUCCEEDED;
                        if (ended) {
                            progress.dismiss();
                        }

                        if (info.getState() == WorkInfo.State.SUCCEEDED) {
                            setResult(RESULT_OK);
                            finish();
                        }
                    });
        }
    }

    public static class UploadActivityViewModel extends ViewModel {

        public String description = null;
        public String language = null;
        public boolean isPrivate = false;
        public boolean hasComments = true;

        public String location;
        public Double latitude;
        public Double longitude;
        public String tag;
    }
}
