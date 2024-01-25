package com.swagVideo.in.activities;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputLayout;
import com.hbb20.CountryCodePicker;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.swagVideo.in.MainApplication;
import com.swagVideo.in.R;
import com.swagVideo.in.adapter.GpsTracker;
import com.swagVideo.in.data.api.REST;
import com.swagVideo.in.data.models.Exists;
import com.swagVideo.in.utils.LocaleUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhoneLoginBaseActivity extends AppCompatActivity {

    public static final String EXTRA_TOKEN = "token";
    private TextInputLayout name, phone, otp;
    private CountryCodePicker cc;
    private Boolean sent;
    private Boolean exists;
    private View verify;
    private View generate,resend;
    protected PhoneLoginActivityViewModel mModel;
    private static final String TAG = "PhoneLoginBaseActivity";
    private String s = "";
    private String s1 = "";
    private String s2 = "";
    private String s3 = "";
    int cc1;
    private String otpp;
    private EditText edit_one_mpin, edit_two_mpin, edit_three_mpin, edit_four_mpin;
    private LinearLayout boxotp;
    private double currentLatitude, currentLongitude;
    private GpsTracker gpsTracker;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleUtil.wrap(base));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);
        ImageButton close = findViewById(R.id.header_back);
        close.setImageResource(R.drawable.ic_baseline_close_24);
        close.setOnClickListener(view -> finish());
        TextView title = findViewById(R.id.header_title);
        title.setText(R.string.login_label);
        findViewById(R.id.header_more).setVisibility(View.GONE);
        int dcc = getResources().getInteger(R.integer.default_calling_code);
        mModel = new ViewModelProvider(this, new PhoneLoginActivityViewModel.Factory(dcc))
                .get(PhoneLoginActivityViewModel.class);
        cc = findViewById(R.id.cc);
        cc.setCountryForPhoneCode(mModel.cc);
        boxotp = (LinearLayout) findViewById(R.id.boxotp);
        boxotp.setVisibility(View.GONE);
        cc.setOnCountryChangeListener(() -> mModel.cc = cc.getSelectedCountryCodeAsInt());
        phone = findViewById(R.id.phone);
        cc.registerCarrierNumberEditText(phone.getEditText());
        phone.getEditText().setText(mModel.phone);
        phone.getEditText().addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable editable) {
                mModel.phone = editable.toString();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        otp = findViewById(R.id.otp);
        otp.getEditText().setText(mModel.otp);
        otp.getEditText().addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable editable) {
//                mModel.otp = editable.toString();
                mModel.otp = otpp;

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        //////////////////////////////////////////////////////
        edit_one_mpin = (EditText) findViewById(R.id.edit_one_mpin);
        edit_two_mpin = (EditText) findViewById(R.id.edit_two_mpin);
        edit_three_mpin = (EditText) findViewById(R.id.edit_three_mpin);
        edit_four_mpin = (EditText) findViewById(R.id.edit_four_mpin);
        edit_one_mpin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //  Log.e("sos",""+s);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //   Log.e("count",""+String.valueOf(s.length()));
                if (edit_one_mpin.getText().toString().length() == 1) {
                    edit_two_mpin.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable edit) {
                Log.e("sos", "" + s);
                s = edit.toString();
            }
        });

        edit_two_mpin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //  Log.e("sos",""+s);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //   Log.e("count",""+String.valueOf(s.length()));
                if (edit_two_mpin.getText().toString().length() == 1) {
                    edit_three_mpin.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable edit) {
                Log.e("sos", "" + s);
                s1 = edit.toString();
            }
        });
        edit_three_mpin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //  Log.e("sos",""+s);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //   Log.e("count",""+String.valueOf(s.length()));
                if (edit_three_mpin.getText().toString().length() == 1) {
                    edit_four_mpin.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable edit) {
                Log.e("sos", "" + s);
                s2 = edit.toString();
            }
        });

        edit_four_mpin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //  Log.e("sos",""+s);
            }

            @Override
            public void onTextChanged(CharSequence ss, int start, int before, int count) {
                //   Log.e("count",""+String.valueOf(s.length()));
                if (edit_three_mpin.getText().toString().length() == 1) {
                    // Log.e("model",""+mModel.cc+" "+mModel.phone+" "+mModel.otp+" "+mModel.name);
                    //verifyOtp(phone);
                }
            }

            @Override
            public void afterTextChanged(Editable edit) {
                Log.e("sos", "" + s);
                s3 = edit.toString();
                otpp = s + s1 + s2 + s3;
                Log.e("otp", otpp);
                mModel.otp = otpp;
                otp.getEditText().setText(otpp);
                Log.e("model", "" + mModel.cc + " " + mModel.phone + " " + mModel.otp + " " + mModel.name);
//                verifyOtp(cc1,phonee,otpp,namee);
            }
        });


        ///////////////////////////////////////////////////
        name = findViewById(R.id.name);
        name.getEditText().setText(mModel.name);
        name.getEditText().addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable editable) {
                mModel.name = editable.toString();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        generate = findViewById(R.id.generate);
        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateOtp();
            }
        });
        resend = findViewById(R.id.resend);
        resend.setVisibility(View.GONE);
        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateOtp();
            }
        });
        verify = findViewById(R.id.verify);
        mModel.doesExist.observe(this, exists -> {
            sent = mModel.isSent.getValue();
            name.setVisibility(sent && !exists ? View.VISIBLE : View.GONE);
            Log.e("Data", sent + "" + exists);
        });
        mModel.isSent.observe(this, sent -> {
            exists = mModel.doesExist.getValue();
            name.setVisibility(sent && !exists ? View.VISIBLE : View.GONE);
            Log.e("Data1", sent + "" + exists);
            otp.setVisibility(sent ? View.VISIBLE : View.GONE);
            if (sent) {
                otp.requestFocus();
            }

            verify.setEnabled(sent);
        });
        generate.setVisibility(View.VISIBLE);
        verify.setVisibility(View.GONE);
        mModel.errors.observe(this, errors -> {
            phone.setError(null);
            otp.setError(null);
            name.setError(null);
            if (errors == null) {
                return;
            }
            if (errors.containsKey("phone")) {
                phone.setError(errors.get("phone"));
            }
            if (errors.containsKey("otp")) {
                otp.setError(errors.get("otp"));
            }
            if (errors.containsKey("name")) {
                name.setError(errors.get("name"));
            }
        });
        phone.getEditText().requestFocus();
    }

    public static class PhoneLoginActivityViewModel extends ViewModel {
        public int cc;
        public String phone = "";
        public String otp = "";
        public String name = "";
        public MutableLiveData<Boolean> doesExist = new MutableLiveData<>(false);
        public MutableLiveData<Boolean> isSent = new MutableLiveData<>(false);
        public MutableLiveData<Map<String, String>> errors = new MutableLiveData<>();

        public PhoneLoginActivityViewModel(int cc) {
            this.cc = cc;
        }

        private static class Factory implements ViewModelProvider.Factory {

            private final int mCallingCode;

            public Factory(int cc) {
                mCallingCode = cc;
            }

            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                //noinspection unchecked
                return (T) new PhoneLoginActivityViewModel(mCallingCode);
            }
        }
    }

    private void generateOtp() {
        if (ContextCompat.checkSelfPermission(PhoneLoginBaseActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PhoneLoginBaseActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);

        } else if (getLocation()) {
           /* currentLatitude = gpsTracker.getLatitude();
            currentLongitude = gpsTracker.getLongitude();*/

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
                                boolean existss = response.body().exists;
                                mModel.doesExist.postValue(existss);
                                mModel.isSent.postValue(true);
                                message = R.string.login_otp_sent;
                                phone.setVisibility(View.GONE);
                                cc.setVisibility(View.GONE);
                                generate.setVisibility(View.GONE);
                                verify.setVisibility(View.VISIBLE);
                                resend.setVisibility(View.VISIBLE);
                                boxotp.setVisibility(View.VISIBLE);
                                mModel.doesExist.observe(PhoneLoginBaseActivity.this, exists -> {
                                    sent = mModel.isSent.getValue();
                                    name.setVisibility(sent && !exists ? View.GONE : View.GONE);
                                    Log.e("Data", sent + "" + exists);
                                });
                                mModel.isSent.observe(PhoneLoginBaseActivity.this, sent -> {
                                    exists = mModel.doesExist.getValue();
                                    name.setVisibility(sent && !exists ? View.GONE : View.GONE);
                                    Log.e("Data1", sent + "" + exists);
                                    otp.setVisibility(sent ? View.GONE : View.GONE);
                                    if (sent) {
                                        otp.requestFocus();
                                    }
                                    verify.setEnabled(sent);
                                });
                            } else if (code == 422) {
                                try {
                                    String content = response.errorBody().string();
                                    showErrors(new JSONObject(content));
                                } catch (Exception ignore) {
                                }
                            } else {
                                message = R.string.error_something_wrong;
                            }

                            if (message != -1) {
                                Toast.makeText(PhoneLoginBaseActivity.this, message, Toast.LENGTH_SHORT).show();
                            }

                            progress.dismiss();
                        }

                        @Override
                        public void onFailure(
                                @Nullable Call<Exists> call,
                                @Nullable Throwable t
                        ) {
                            Log.e(TAG, "Failed when trying to generate OTP.", t);
                            Toast.makeText(PhoneLoginBaseActivity.this, R.string.error_server, Toast.LENGTH_SHORT).show();
                            progress.dismiss();
                        }
                    });
        }
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

    public boolean getLocation() {
        gpsTracker = new GpsTracker(this);
        if (gpsTracker.canGetLocation()) {
            currentLatitude = gpsTracker.getLatitude();
            currentLongitude = gpsTracker.getLongitude();

            return true;
        } else {
            gpsTracker.showSettingsAlert();

            return false;
        }
    }

}
