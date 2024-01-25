package com.swagVideo.in.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.swagVideo.in.MainApplication;
import com.swagVideo.in.R;
import com.swagVideo.in.adapter.GpsTracker;
import com.swagVideo.in.data.api.REST;
import com.swagVideo.in.data.models.Exists;
import com.swagVideo.in.data.models.Token;
import com.swagVideo.in.utils.LocaleUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("LongLogTag")
public class PhoneLoginServerActivity extends PhoneLoginBaseActivity {

    private static final String TAG = "PhoneLoginServerActivity";
    private String currentLatitude;
    private String currentLongitude;
    private GpsTracker gpsTracker;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleUtil.wrap(base));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View generate = findViewById(R.id.generate);
      /*  generate.setOnClickListener(v -> generateOtp());*/
        View verify = findViewById(R.id.verify);
        verify.setOnClickListener(v -> verifyOtp());
    }

    private void generateOtp() {
        mModel.errors.postValue(null);
        KProgressHUD progress = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel(getString(R.string.progress_title))
                .setCancellable(false)
                .show();
        REST rest = MainApplication.getContainer().get(REST.class);
        rest.loginPhoneOtp(mModel.cc + "", mModel.phone)
                .enqueue(new Callback<Exists>() {

                    @Override
                    public void onResponse(
                            @Nullable Call<Exists> call,
                            @Nullable Response<Exists> response
                    ) {
                        int code = response != null ? response.code() : -1;
                        int message = -1;
                        if (code == 200) {
                            boolean exists = response.body().exists;
                            mModel.doesExist.postValue(exists);
                            mModel.isSent.postValue(true);
                            message = R.string.login_otp_sent;
                        } else if (code == 422) {
                            try {
                                String content = response.errorBody().string();
                                showErrors(new JSONObject(content));
                            } catch (Exception ignore) {
                            }
                        } else {
                            message = R.string.error_internet;
                        }

                        if (message != -1) {
                            Toast.makeText(com.swagVideo.in.activities.PhoneLoginServerActivity.this, message, Toast.LENGTH_SHORT).show();
                        }

                        progress.dismiss();
                    }

                    @Override
                    public void onFailure(
                            @Nullable Call<Exists> call,
                            @Nullable Throwable t
                    ) {
                        Log.e(TAG, "Failed when trying to generate OTP.", t);
                        Toast.makeText(com.swagVideo.in.activities.PhoneLoginServerActivity.this, R.string.error_internet, Toast.LENGTH_SHORT).show();
                        progress.dismiss();
                    }
                });
    }

    private void showErrors(JSONObject json) throws Exception {
        JSONObject errors = json.getJSONObject("errors");
        Map<String, String> messages = new HashMap<>();
        String[] keys = new String[]{"cc", "phone", "otp", "name"};
        for (String key : keys) {
            JSONArray fields = errors.optJSONArray(key);
            if (fields != null) {
                messages.put(key, fields.getString(0));
            }
        }

        mModel.errors.postValue(messages);
    }

    private void verifyOtp() {
        if (ContextCompat.checkSelfPermission(PhoneLoginServerActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PhoneLoginServerActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);

        } else if (getLocation()) {
            currentLatitude = String.valueOf(gpsTracker.getLatitude());
            currentLongitude = String.valueOf(gpsTracker.getLongitude());

            mModel.errors.postValue(null);
            KProgressHUD progress = KProgressHUD.create(this)
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setLabel(getString(R.string.progress_title))
                    .setCancellable(false)
                    .show();
            REST rest = MainApplication.getContainer().get(REST.class);
            rest.loginPhone(mModel.cc + "", mModel.phone, mModel.otp, mModel.name,currentLatitude,currentLongitude)
                    .enqueue(new Callback<Token>() {

                        @Override
                        public void onResponse(
                                @Nullable Call<Token> call,
                                @Nullable Response<Token> response
                        ) {
                            progress.dismiss();
                            if (response != null) {
                                if (response.isSuccessful()) {
                                    Intent data = new Intent();
                                    data.putExtra(EXTRA_TOKEN, response.body());
                                    setResult(RESULT_OK, data);
                                    finish();
                                    Log.e("responsedata", "" + response.body());
                                } else if (response.code() == 422) {
                                    try {
                                        String content = response.errorBody().string();
                                        showErrors(new JSONObject(content));
                                    } catch (Exception ignore) {
                                    }
                                } else {
                                    Toast.makeText(com.swagVideo.in.activities.PhoneLoginServerActivity.this, R.string.error_server, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(
                                @Nullable Call<Token> call,
                                @Nullable Throwable t
                        ) {
                            Log.e(TAG, "Failed when trying to verify OTP.", t);
                            Toast.makeText(com.swagVideo.in.activities.PhoneLoginServerActivity.this, R.string.error_internet, Toast.LENGTH_SHORT).show();
                            progress.dismiss();
                        }
                    });
        }
    }

    public boolean getLocation() {
        gpsTracker = new GpsTracker(this);
        if (gpsTracker.canGetLocation()) {
            /*currentLatitude = gpsTracker.getLatitude();
            currentLongitude = gpsTracker.getLongitude();
*/
            return true;
        } else {
            gpsTracker.showSettingsAlert();

            return false;
        }
    }
}
