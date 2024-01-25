package com.swagVideo.in.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.bumptech.glide.Glide;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.pixplicity.easyprefs.library.Prefs;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.smarteist.autoimageslider.SliderViewAdapter;
import com.swagVideo.in.adapter.TextGradient;
import com.swagVideo.in.fragments.NearbyFragment;
import com.swagVideo.in.fragments.NearbyPlayerFragment;
import com.swagVideo.in.fragments.PlayerFragment;
import com.swagVideo.in.fragments.PlayerSliderFragment;
import com.swagVideo.in.pojo.SliderItem;
import com.swagVideo.in.workers.SaveToGalleryWorker;
import com.thefinestartist.finestwebview.FinestWebView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.swagVideo.in.BuildConfig;
import com.swagVideo.in.MainApplication;
import com.swagVideo.in.MainNavigationDirections;
import com.swagVideo.in.R;
import com.swagVideo.in.SharedConstants;
import com.swagVideo.in.ads.BannerAdProvider;
import com.swagVideo.in.common.LoadingState;
import com.swagVideo.in.common.SharingTarget;
import com.swagVideo.in.data.api.REST;
import com.swagVideo.in.data.dbs.ClientDatabase;
import com.swagVideo.in.data.models.Advertisement;
import com.swagVideo.in.data.models.Clip;
import com.swagVideo.in.data.models.Promotion;
import com.swagVideo.in.data.models.Token;
import com.swagVideo.in.data.models.UnreadNotifications;
import com.swagVideo.in.data.models.User;
import com.swagVideo.in.data.models.Wrappers;
import com.swagVideo.in.events.RecordingOptionsEvent;
import com.swagVideo.in.events.ResolveRecordingOptionsEvent;
import com.swagVideo.in.fragments.PromotionsDialogFragment;
import com.swagVideo.in.utils.AdsUtil;
import com.swagVideo.in.utils.IntentUtil;
import com.swagVideo.in.utils.LocaleUtil;
import com.swagVideo.in.utils.PackageUtil;
import com.swagVideo.in.utils.TempUtil;
import com.swagVideo.in.utils.VideoUtil;
import com.swagVideo.in.workers.DeviceTokenWorker;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String EXTRA_REGISTERED = "registered";
    public static final String EXTRA_USER = "user";
    private static final String TAG = "MainActivity";
    private BannerAdProvider mAd;
    private CallbackManager mCallbackManager;
    private boolean mExitRequested;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private MainActivityViewModel mModel;
    private GoogleSignInClient mSignInClient;
    private AppUpdateManager mUpdateManager;
    private SliderView sliderView;
    private SliderAdapterExample adapter;
    private ArrayList<SliderItem> sliderItems = new ArrayList<>();
    private final InstallStateUpdatedListener mUpdateListener = state -> {
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            mUpdateManager.completeUpdate();
        }
    };
   public static TextView badge;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleUtil.wrap(base));
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.v(TAG, "Received request:" + requestCode + ", result: " + resultCode + ".");
        if (requestCode == SharedConstants.REQUEST_CODE_LOGIN_GOOGLE && resultCode == RESULT_OK && data != null) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                loginWithGoogle(task.getResult(ApiException.class));
            } catch (ApiException e) {
                Log.e(TAG, "Unable to login with Google account.", e);
            }
        } else if (requestCode == SharedConstants.REQUEST_CODE_LOGIN_PHONE && resultCode == RESULT_OK && data != null) {
            Token token = data.getParcelableExtra(PhoneLoginBaseActivity.EXTRA_TOKEN);
            updateLoginState(token);
        } else if (requestCode == SharedConstants.REQUEST_CODE_LOGIN_EMAIL && resultCode == RESULT_OK && data != null) {
            Token token = data.getParcelableExtra(EmailLoginActivity.EXTRA_TOKEN);
            updateLoginState(token);
        } else if (requestCode == SharedConstants.REQUEST_CODE_PICK_VIDEO && resultCode == RESULT_OK && data != null) {
            proceedToTrimmer(data.getData());
        }

        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        try {
            if (getIntent().getExtras().getString("from").equals("nearby")) {
                super.onBackPressed();
                /*Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);*/
                finish();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        NavController controller = findNavController();
        if (controller.popBackStack()) {
            return;
        }
        int behavior = getResources().getInteger(R.integer.exit_behavior);
        switch (behavior) {
            case 1:
                if (mExitRequested) {
                    super.onBackPressed();
                    break;
                }
                Toast.makeText(this, R.string.exit_toast_message, Toast.LENGTH_SHORT).show();
                mExitRequested = true;
                mHandler.postDelayed(() -> mExitRequested = false, TimeUnit.SECONDS.toMillis(2));
                break;
            case 2:
                new MaterialAlertDialogBuilder(this)
                        .setMessage(R.string.confirmation_close_app)
                        .setNegativeButton(R.string.cancel_button, (dialog, i) -> dialog.cancel())
                        .setPositiveButton(R.string.close_button, (dialog, i) -> {
                            dialog.dismiss();
                            finish();
                        })
                        .show();
                break;
            default:
                super.onBackPressed();
                break;
        }
    }

    @Override
    @SuppressLint("NonConstantResourceId")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            if (getIntent().getExtras().getString("from").equals("nearby")) {
                Bundle args = new Bundle();
                //args.putString("joinAs", "");
                Fragment fr = new NearbyPlayerFragment();
                fr.setArguments(args);
                //getSupportFragmentManager().beginTransaction().replace(R.id.host, fr).addToBackStack("dd").commit();

                NavController controller = findNavController();
               controller.navigate(MainNavigationDirections.actionShowPlayerSlider(1,args));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Advertisement ad = AdsUtil.findByLocationAndType("login", "banner");
        if (ad != null) {
            mAd = new BannerAdProvider(ad);
        } else {
            Log.w(TAG, "No ad configured for login page.");
        }

        mUpdateManager = AppUpdateManagerFactory.create(this);
        mModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        User user = getIntent().getParcelableExtra(EXTRA_USER);
        if (user != null) {
            mModel.user.postValue(user);
            mModel.state.postValue(LoadingState.LOADED);
        }

        View content = findViewById(R.id.content);
        View loading = findViewById(R.id.loading);
        mModel.state.observe(this, state -> {
            content.setVisibility(state == LoadingState.LOADED ? View.VISIBLE : View.GONE);
            loading.setVisibility(state == LoadingState.LOADING ? View.VISIBLE : View.GONE);
        });
        badge = findViewById(R.id.notification_badge);
        mModel.notifications.observe(this, count -> {
            if (count > 99) {
                count = 99;
            }

            if (count > 0) {
                badge.setText(count < 10 ? (count + "+") : String.valueOf(count));
                badge.setVisibility(View.VISIBLE);
            } else {
                badge.setVisibility(View.GONE);
            }
        });
        mModel.user.observe(this, u -> {
            if (u != null) {
                refreshUnreadCount();
                boolean registered = getIntent().getBooleanExtra(EXTRA_REGISTERED, false);
                if (registered && getResources().getBoolean(R.bool.profile_edit_after_registration)) {
                    findNavController().navigate(MainNavigationDirections.actionShowEditProfile());
                }
                if (registered && !getResources().getBoolean(R.bool.skip_suggestions_screen)) {
                    startActivity(new Intent(this, SuggestionsActivity.class));
                    Log.e("Data","sentexists");
                }
            }
        });
        int launches = Prefs.getInt(SharedConstants.PREF_LAUNCH_COUNT, 0);
        Prefs.putInt(SharedConstants.PREF_LAUNCH_COUNT, ++launches);
        View sheet = findViewById(R.id.login_sheet);
        BottomSheetBehavior<View> bsb = BottomSheetBehavior.from(sheet);
        ImageButton close = sheet.findViewById(R.id.header_back);
        close.setImageResource(R.drawable.ic_baseline_close_24);
        close.setOnClickListener(view -> bsb.setState(BottomSheetBehavior.STATE_COLLAPSED));
        TextView title = sheet.findViewById(R.id.header_title);

        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.bottomToBottom = ConstraintSet.PARENT_ID;
        layoutParams.endToEnd = ConstraintSet.PARENT_ID;
        // layoutParams.startToStart = ConstraintSet.PARENT_ID;
        layoutParams.topToTop = ConstraintSet.PARENT_ID;
        layoutParams.rightMargin = 30;
        title.setLayoutParams(layoutParams);

        title.setText(R.string.login_label);
        sheet.findViewById(R.id.header_more).setVisibility(View.GONE);
        String pp = getString(R.string.privacy_policy);
        String tou = getString(R.string.term_of_use);
        String text = getString(R.string.privacy_terms_description, tou, pp);
        SpannableString spanned = new SpannableString(text);
        spanned.setSpan(
                createSpan(getString(R.string.link_privacy_policy)),
                text.indexOf(pp),
                text.indexOf(pp) + pp.length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        );
        spanned.setSpan(
                createSpan(getString(R.string.link_terms_of_use)),
                text.indexOf(tou),
                text.indexOf(tou) + tou.length(),
                Spanned.SPAN_COMPOSING
        );
        TextView terms1 = sheet.findViewById(R.id.terms_notice);
        terms1.setMovementMethod(LinkMovementMethod.getInstance());
        terms1.setText(spanned, TextView.BufferType.SPANNABLE);
        CheckBox terms2 = sheet.findViewById(R.id.terms_checkbox);
        terms2.setMovementMethod(LinkMovementMethod.getInstance());
        terms2.setText(spanned, TextView.BufferType.SPANNABLE);
        boolean acceptance = getResources().getBoolean(R.bool.explicit_acceptance_required);
        if (acceptance) {
            terms1.setVisibility(View.GONE);
            terms2.setVisibility(View.VISIBLE);
        } else {
            terms1.setVisibility(View.VISIBLE);
            terms2.setVisibility(View.GONE);
        }
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance()
                .registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {

                    @Override
                    public void onCancel() {
                        Log.e("Facebook", "0");
                        Log.w(TAG, "Login with Facebook was cancelled.");
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.e(TAG, "Login with Facebook returned error.", error);
                        Toast.makeText(MainActivity.this, R.string.error_internet, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess(LoginResult result) {
                        Log.e("Facebook","1"+ String.valueOf(result));
                        loginWithFacebook(result);
                    }
                });
        View facebook = sheet.findViewById(R.id.facebook);
        facebook.setOnClickListener(view -> {
            if (acceptance && !terms2.isChecked()) {
                Toast.makeText(this, R.string.message_accept_policies, Toast.LENGTH_SHORT).show();
                return;
            }

            bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
            LoginManager lm = LoginManager.getInstance();
            try {
                lm.logOut();
            } catch (Exception ignore) {
            }

            lm.logInWithReadPermissions(
                    MainActivity.this, Collections.singletonList("email"));
        });
        if (!getResources().getBoolean(R.bool.facebook_login_enabled)) {
            facebook.setVisibility(View.GONE);
        }

        GoogleSignInOptions options =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestProfile()
                        .build();
        mSignInClient = GoogleSignIn.getClient(this, options);
        View google = sheet.findViewById(R.id.google);
        google.setOnClickListener(view -> {
            if (acceptance && !terms2.isChecked()) {
                Toast.makeText(this, R.string.message_accept_policies, Toast.LENGTH_SHORT).show();
                return;
            }
            bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
            startActivityForResult(
                    mSignInClient.getSignInIntent(), SharedConstants.REQUEST_CODE_LOGIN_GOOGLE);
        });
        if (!getResources().getBoolean(R.bool.google_login_enabled)) {
            google.setVisibility(View.GONE);
        }
        View phone = sheet.findViewById(R.id.phone);
        phone.setOnClickListener(view -> {
            if (acceptance && !terms2.isChecked()) {
                Toast.makeText(this, R.string.message_accept_policies, Toast.LENGTH_SHORT).show();
                return;
            }
            bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
           /* if (TextUtils.equals(getString(R.string.sms_login_service), "firebase")) {
                startActivityForResult(
                        new Intent(this, PhoneLoginFirebaseActivity.class),
                        SharedConstants.REQUEST_CODE_LOGIN_PHONE
                );
            } else {*/
            startActivityForResult(
                    new Intent(this, PhoneLoginServerActivity.class),
                    SharedConstants.REQUEST_CODE_LOGIN_PHONE
            );
            // }
        });
        if (!getResources().getBoolean(R.bool.sms_login_enabled)) {
            phone.setVisibility(View.GONE);
        }

        View email = sheet.findViewById(R.id.email);
        email.setOnClickListener(view -> {
            if (acceptance && !terms2.isChecked()) {
                Toast.makeText(this, R.string.message_accept_policies, Toast.LENGTH_SHORT).show();
                return;
            }

            bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
            startActivityForResult(
                    new Intent(this, EmailLoginActivity.class),
                    SharedConstants.REQUEST_CODE_LOGIN_EMAIL
            );
        });
        if (!getResources().getBoolean(R.bool.email_login_enabled)) {
            email.setVisibility(View.GONE);
        }

        NavController controller = findNavController();
        ImageButton clips = findViewById(R.id.clips);
        clips.setOnClickListener(v ->
                controller.navigate(MainNavigationDirections.actionShowClips())
        );
        ImageButton discover = findViewById(R.id.discover);
       /* discover.setOnClickListener(v ->
                controller.navigate(MainNavigationDirections.actionShowDiscover()));*/
        discover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TrendingActivity.class);
                // intent.putExtra("from","nearby");
                //startActivity(intent);
                //finish();
                controller.navigate(MainNavigationDirections.actionShowTranding());
            }
        });
        findViewById(R.id.record).setOnClickListener(v -> {
            if (mModel.isLoggedIn()) {
                if (getResources().getBoolean(R.bool.skip_recording_options)) {
                    String[] permissions = new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    };
                    if (EasyPermissions.hasPermissions(MainActivity.this, permissions)) {
                        chooseVideoForUpload();
                    } else {
                        EasyPermissions.requestPermissions(
                                this,
                                getString(R.string.permission_rationale_upload),
                                SharedConstants.REQUEST_CODE_PERMISSIONS_UPLOAD,
                                permissions);
                    }
                } else if (EventBus.getDefault().hasSubscriberForEvent(ResolveRecordingOptionsEvent.class)) {
                    EventBus.getDefault().post(new ResolveRecordingOptionsEvent());
                } else {
                    startActivity(new Intent(this, RecorderActivity.class));
                }
            } else {
                showLoginSheet();
            }
        });
        ImageButton notifications = findViewById(R.id.notifications);
        notifications.setOnClickListener(v -> {
            if (mModel.isLoggedIn()) {
                controller.navigate(MainNavigationDirections.actionShowNotifications());
            } else {
                showLoginSheet();
            }
        });
        ImageButton profile = findViewById(R.id.profile);
        profile.setOnClickListener(v -> {
            if (mModel.isLoggedIn()) {
                controller.navigate(MainNavigationDirections.actionShowProfileSelf());
            } else {
                showLoginSheet();
            }
        });
        int active = ContextCompat.getColor(this, R.color.colorNavigationActive);
        int inactive = ContextCompat.getColor(this, R.color.colorNavigationInactive);
        boolean immersive = getResources().getBoolean(R.bool.immersive_mode_enabled);
        View container = findViewById(R.id.host);
        View toolbar = findViewById(R.id.toolbar);
        controller.addOnDestinationChangedListener((c, destination, arguments) -> {
            NavBackStackEntry previous = controller.getPreviousBackStackEntry();
            if (previous != null && previous.getDestination().getId() == R.id.fragment_notifications && mModel.isLoggedIn()) {
                refreshUnreadCount();
            }
            ImageViewCompat.setImageTintList(
                    clips, ColorStateList.valueOf(destination.getId() == R.id.fragment_player_tabs ? active : inactive));
            ImageViewCompat.setImageTintList(
                    discover, ColorStateList.valueOf(destination.getId() == R.id.fragment_discover ? active : inactive));
            ImageViewCompat.setImageTintList(
                    notifications, ColorStateList.valueOf(destination.getId() == R.id.fragment_notifications ? active : inactive));
            ImageViewCompat.setImageTintList(
                    profile, ColorStateList.valueOf(destination.getId() == R.id.fragment_profile ? active : inactive));
            boolean player = destination.getId() == R.id.fragment_player_slider
                    || destination.getId() == R.id.fragment_player_tabs;
            if (player && immersive) {
                container.setLayoutParams(
                        new RelativeLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT));
                //  toolbar.setBackgroundResource(android.R.color.transparent);
            } else if (immersive) {
                RelativeLayout.LayoutParams params =
                        new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
               params.addRule(RelativeLayout.ABOVE, R.id.toolbar);
                container.setLayoutParams(params);
               // toolbar.setBackgroundResource(R.color.colorNavigationBar);
            }
        });
        View options2 = findViewById(R.id.sharing_sheet);
        BottomSheetBehavior<View> bsb2 = BottomSheetBehavior.from(options2);
        ImageButton close2 = options2.findViewById(R.id.header_back);
        close2.setImageResource(R.drawable.ic_baseline_close_24);
        close2.setOnClickListener(v -> bsb2.setState(BottomSheetBehavior.STATE_COLLAPSED));
        options2.findViewById(R.id.header_more).setVisibility(View.GONE);
        View options3 = findViewById(R.id.qr_sheet);
        BottomSheetBehavior<View> bsb3 = BottomSheetBehavior.from(options3);
        ImageButton close3 = options3.findViewById(R.id.header_back);
        close3.setImageResource(R.drawable.ic_baseline_close_24);
        close3.setOnClickListener(v -> bsb3.setState(BottomSheetBehavior.STATE_COLLAPSED));
        TextView title3 = options3.findViewById(R.id.header_title);
        title3.setText(R.string.qr_label);
        options3.findViewById(R.id.header_more).setVisibility(View.GONE);
        syncFcmToken();
        showPromotionsIfAvailable();
        if (getResources().getBoolean(R.bool.in_app_review_enabled)) {
            long review = Prefs.getLong(SharedConstants.PREF_REVIEW_PROMPTED_AT, 0);
            if (review <= 0 && launches == getResources().getInteger(R.integer.in_app_review_delay)) {
                Prefs.putLong(SharedConstants.PREF_REVIEW_PROMPTED_AT, System.currentTimeMillis());
                askForReview();
            } else if (launches % getResources().getInteger(R.integer.in_app_review_interval) == 0) {
                Prefs.putLong(SharedConstants.PREF_REVIEW_PROMPTED_AT, System.currentTimeMillis());
                askForReview();
            }
        }

        boolean update = getResources().getBoolean(R.bool.in_app_update_enabled)
                && launches % getResources().getInteger(R.integer.in_app_update_interval) == 0;
        if (update) {
            checkForUpdate();
        }
        sliderView = findViewById(R.id.imageSlider);

        setSlider();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUpdateManager.unregisterListener(mUpdateListener);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecordingOptionsEvent(RecordingOptionsEvent event) {
        View sheet = findViewById(R.id.recording_options_sheet);
        TextView title = sheet.findViewById(R.id.header_title);
        title.setText(R.string.recording_options_title);
        BottomSheetBehavior<View> bsb = BottomSheetBehavior.from(sheet);
        sheet.findViewById(R.id.header_back).setVisibility(View.GONE);
        ImageButton close = sheet.findViewById(R.id.header_more);
        close.setImageResource(R.drawable.ic_baseline_close_24);
        close.setOnClickListener(v -> bsb.setState(BottomSheetBehavior.STATE_COLLAPSED));
        sheet.findViewById(R.id.solo)
                .setOnClickListener(v -> {
                    bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    startActivity(new Intent(this, RecorderActivity.class));
                });
        sheet.findViewById(R.id.solo_music)
                .setOnClickListener(v -> {
                    bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    submitForUseAudio(event.getClip());
                });
        bsb.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUpdateManager.registerListener(mUpdateListener);
        com.google.android.play.core.tasks.Task<AppUpdateInfo> task =
                mUpdateManager.getAppUpdateInfo();
        task.addOnSuccessListener(info -> {
            if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                Log.w(TAG, "There is a previous update pending.");
                try {
                    mUpdateManager.startUpdateFlowForResult(
                            info,
                            AppUpdateType.IMMEDIATE,
                            this,
                            SharedConstants.REQUEST_CODE_UPDATE_APP);
                } catch (Exception e) {
                    Log.e(TAG, "Could not initial in-app update.", e);
                }
            }
        });
        if (!mModel.isLoggedIn() && mModel.state.getValue() != LoadingState.LOADING) {
            reloadProfile();
        }
    }//check kor

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void askForReview() {
        ReviewManager manager = ReviewManagerFactory.create(this);
        com.google.android.play.core.tasks.Task<ReviewInfo> task1 = manager.requestReviewFlow();
        task1.addOnSuccessListener(info -> {
            com.google.android.play.core.tasks.Task<Void> task2 =
                    manager.launchReviewFlow(this, info);
            task2.addOnCompleteListener(x ->
                    Log.w(TAG, "Review could be cancelled or submitted, whichever."));
        });
    }

    private void checkForUpdate() {
        com.google.android.play.core.tasks.Task<AppUpdateInfo> task =
                mUpdateManager.getAppUpdateInfo();
        task.addOnSuccessListener(info -> {
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                Log.v(TAG, "There is an new update available.");
                try {
                    mUpdateManager.startUpdateFlowForResult(
                            info,
                            AppUpdateType.FLEXIBLE,
                            this,
                            SharedConstants.REQUEST_CODE_UPDATE_APP);
                } catch (Exception e) {
                    Log.e(TAG, "Could not initial in-app update.", e);
                }
            }
        });
    }

    @AfterPermissionGranted(SharedConstants.REQUEST_CODE_PERMISSIONS_UPLOAD)
    private void chooseVideoForUpload() {
        IntentUtil.startChooser(
                this,
                SharedConstants.REQUEST_CODE_PICK_VIDEO,
                "video/mp4");
    }

    private ClickableSpan createSpan(String url) {
        return new ClickableSpan() {

            @Override
            public void onClick(@NonNull View widget) {
                showUrlBrowser(url, null, false);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ds.linkColor);
                ds.setUnderlineText(true);
            }
        };
    }

    public NavController findNavController() {
        NavHostFragment fragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.host);
        return fragment.getNavController();
    }

    private boolean isResumed() {
        return getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED);
    }

    private void loginWithFacebook(LoginResult result) {
        Log.d(TAG, "User logged in with Facebook ID " + result.getAccessToken().getUserId() + '.');
        String token = result.getAccessToken().getToken();
        Log.e("Facebook",token);
        REST rest = MainApplication.getContainer().get(REST.class);
        rest.loginFacebook(result.getAccessToken().getToken())
                .enqueue(new Callback<Token>() {
                    @Override
                    public void onResponse(
                            @Nullable Call<Token> call,
                            @Nullable Response<Token> response

                    ) {
                        if (response != null && response.isSuccessful()) {
                            mHandler.post(() -> updateLoginState(response.body()));
                            Log.e("Facebook","2"+response.body());
                        }
                    }
                    @Override
                    public void onFailure(
                            @Nullable Call<Token> call,
                            @Nullable Throwable t
                    ) {
                        Log.e(TAG, "Login request with Facebook has failed.", t);
                        Log.e("Facebook","1");
                    }
                });
    }

    private void loginWithGoogle(@Nullable GoogleSignInAccount account) {
        if (account == null) {
            Log.v(TAG, "Could not retrieve a Google account after login.");
            return;
        }

        Log.d(TAG, "User logged in with Google ID " + account.getId() + '.');
        String name = account.getDisplayName();
        String token = account.getIdToken();
        String id = account.getId();
        String email = account.getEmail();
        String picture = String.valueOf(account.getPhotoUrl());
        Log.e("GMAILLOGIN",name+" "+token+" "+id+" "+email+" "+picture);
        REST rest = MainApplication.getContainer().get(REST.class);
        rest.loginGoogle(name, token, id, email, picture)
                .enqueue(new Callback<Token>() {

                    @Override
                    public void onResponse(
                            @Nullable Call<Token> call,
                            @Nullable Response<Token> response
                    ) {
                        if (response != null && response.isSuccessful()) {
                            mHandler.post(() -> updateLoginState(response.body()));
                            Log.e("GMAILLOGINresponse","Login");
                        }
                    }

                    @Override
                    public void onFailure(
                            @Nullable Call<Token> call,
                            @Nullable Throwable t
                    ) {
                        Log.e(TAG, "Login request with Google has failed.", t);
                    }
                });
    }


    public void logout() {
        KProgressHUD progress = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel(getString(R.string.progress_title))
                .setCancellable(false)
                .show();
        FirebaseMessaging.getInstance().deleteToken()
                .addOnCompleteListener(this, task -> {
                    Log.v(TAG, "Firebase token deletion was successful.");
                    if (progress.isShowing()) {
                        progress.dismiss();
                    }

                    Prefs.remove(SharedConstants.PREF_CLIPS_SEEN_UNTIL);
                    Prefs.remove(SharedConstants.PREF_DEVICE_ID);
                    Prefs.remove(SharedConstants.PREF_FCM_TOKEN_SYNCED_AT);
                    Prefs.remove(SharedConstants.PREF_LAUNCH_COUNT);
                    Prefs.remove(SharedConstants.PREF_PREFERRED_LANGUAGES);
                    Prefs.remove(SharedConstants.PREF_PROMOTIONS_SEEN_UNTIL);
                    Prefs.remove(SharedConstants.PREF_SERVER_TOKEN);
                    ClientDatabase database =
                            MainApplication.getContainer().get(ClientDatabase.class);
                    database.drafts().delete();
                    Clip.LIKED.clear();
                    User.FOLLOWING.clear();
                    restartActivity(null, null);
                });
    }

    public void popBackStack() {
        findNavController().popBackStack();
    }

    private void proceedToAdjustment(File file) {
        Intent intent = new Intent(this, AdjustAudioActivity.class);
        intent.putExtra(AdjustAudioActivity.EXTRA_VIDEO, file.getAbsolutePath());
        startActivity(intent);
        finish();
    }

    private void proceedToFilter(File file) {
        Intent intent;
       /* if (getResources().getBoolean(R.bool.filters_enabled)) {
            intent = new Intent(this, FilterActivity.class);
            intent.putExtra(FilterActivity.EXTRA_VIDEO, file.getAbsolutePath());
        } else {*/
        intent = new Intent(this, UploadActivity.class);
        intent.putExtra(UploadActivity.EXTRA_VIDEO, file.getAbsolutePath());
        //      }

        startActivity(intent);
        finish();
    }

    private void proceedToTrimmer(Uri uri) {
        File copy = TempUtil.createCopy(this, uri, ".mp4");
        if (getResources().getBoolean(R.bool.skip_trimming_screen)) {
            if (getResources().getBoolean(R.bool.skip_adjustment_screen)) {
                proceedToAdjustment(copy);
            } else {
                proceedToFilter(copy);
            }
        } else {
            Intent intent = new Intent(this, TrimmerActivity.class);
            intent.putExtra(TrimmerActivity.EXTRA_VIDEO, copy.getAbsolutePath());
            startActivity(intent);
        }

        finish();
    }

    private void refreshUnreadCount() {
        REST rest = MainApplication.getContainer().get(REST.class);
        rest.notificationsUnread()
                .enqueue(new Callback<UnreadNotifications>() {
                    @Override
                    public void onResponse(
                            @Nullable Call<UnreadNotifications> call,
                            @Nullable Response<UnreadNotifications> response
                    ) {
                        int code = response != null ? response.code() : -1;
                        Log.w(TAG, "Fetching unread notifications count returned " + code + ".");
                        if (response != null && response.isSuccessful()) {
                            //noinspection ConstantConditions
                            int count = response.body().count;
                            mModel.notifications.postValue(count);
                        }
                    }
                    @Override
                    public void onFailure(
                            @Nullable Call<UnreadNotifications> call,
                            @Nullable Throwable t
                    ) {
                        Log.e(TAG, "Failed to fetch unread notifications count.", t);
                    }
                });
    }

    private void reloadProfile() {
        mModel.state.postValue(LoadingState.LOADING);
        REST rest = MainApplication.getContainer().get(REST.class);
        rest.profileShow()
                .enqueue(new Callback<Wrappers.Single<User>>() {
                    @Override
                    public void onResponse(
                            @Nullable Call<Wrappers.Single<User>> call,
                            @Nullable Response<Wrappers.Single<User>> response
                    ) {
                        int code = response != null ? response.code() : -1;
                        Log.w(TAG, "Fetching profile from server returned " + code + ".");
                        if (response != null && response.isSuccessful()) {
                            mModel.user.postValue(response.body().data);
                            mModel.state.postValue(LoadingState.LOADED);
                        } else {
                            mModel.state.postValue(LoadingState.LOADED);
                        }
                    }
                    @Override
                    public void onFailure(
                            @Nullable Call<Wrappers.Single<User>> call,
                            @Nullable Throwable t
                    ) {
                        Log.e(TAG, "Failed to fetch profile from server.", t);
                        mModel.state.postValue(LoadingState.LOADED);
                    }
                });
    }

    public void reportSubject(String type, int id) {
        Intent intent = new Intent(this, ReportActivity.class);
        intent.putExtra(ReportActivity.EXTRA_REPORT_SUBJECT_TYPE, type);
        intent.putExtra(ReportActivity.EXTRA_REPORT_SUBJECT_ID, id);
        startActivity(intent);
    }

    public void restartActivity(@Nullable Token token, @Nullable Uri data) {
        Intent intent = Intent.makeRestartActivityTask(getComponentName());
        if (token != null && !token.existing) {
            intent.putExtra(EXTRA_REGISTERED, true);
        }

        if (data != null) {
            intent.setData(data);
        }

        startActivity(intent);
    }

    public void share(Clip clip, @Nullable SharingTarget target) {
        if (getResources().getBoolean(R.bool.sharing_links_enabled)) {
            shareLink(clip, target);
        } else {
            File clips = new File(getFilesDir(), "clips");
            if (!clips.exists() && !clips.mkdirs()) {
                Log.w(TAG, "Could not create directory at " + clips);
            }

            File fixed = TempUtil.createNewFile(clips, ".mp4");
            if (fixed.exists()) {
                shareVideoFile(this, fixed, target);
                return;
            }

            WorkManager wm = WorkManager.getInstance(this);
            boolean async = getResources().getBoolean(R.bool.sharing_async_enabled);
            OneTimeWorkRequest request;
            if (getResources().getBoolean(R.bool.sharing_watermark_enabled)) {
                File original = TempUtil.createNewFile(this, ".mp4");
                File watermarked = TempUtil.createNewFile(this, ".mp4");
                wm.beginWith(VideoUtil.createDownloadRequest(clip.video, original, async))
                        .then(VideoUtil.createWatermarkRequest(clip, original, watermarked, async))
                        .then(request = VideoUtil.createFastStartRequest(watermarked, fixed))
                        .enqueue();
            } else {
                wm.enqueue(request = VideoUtil.createDownloadRequest(clip.video, fixed, async));
            }

            if (async) {
                Toast.makeText(this, R.string.message_sharing_async, Toast.LENGTH_SHORT).show();
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
                        });
            }

            wm.getWorkInfoByIdLiveData(request.getId())
                    .observe(this, info -> {
                        if (info.getState() == WorkInfo.State.SUCCEEDED) {
                            shareVideoFile(this, fixed, target);
                        }
                    });
        }
    }

    private void shareToTarget(Intent intent, @Nullable SharingTarget target) {
        if (target != null && PackageUtil.isInstalled(this, target.pkg)) {
            intent.setPackage(target.pkg);
            startActivity(intent);
        } else {
            startActivity(Intent.createChooser(intent, getString(R.string.share_clip_chooser)));
        }
    }

    private void shareVideoFile(Context context, File file, @Nullable SharingTarget target) {
        Log.v(TAG, "Showing sharing options for " + file);
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName(), file);
        Intent intent = ShareCompat.IntentBuilder.from(this)
                .setStream(uri)
                .setText(getString(R.string.share_clip_text, context.getPackageName()))
                .setType("video/*")
                .getIntent()
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareToTarget(intent, target);
    }

    private void shareLink(Uri uri, @Nullable SharingTarget target) {
        Log.v(TAG, "Showing sharing options for " + uri);
        Intent intent = ShareCompat.IntentBuilder.from(this)
                .setText(uri.toString())
                .setType("text/plain")
                .getIntent();
        shareToTarget(intent, target);
    }

    private void shareLink(Clip clip, @Nullable SharingTarget target) {
        KProgressHUD progress = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel(getString(R.string.progress_title))
                .setCancellable(false)
                .show();
        DynamicLink.SocialMetaTagParameters.Builder smtp =
                new DynamicLink.SocialMetaTagParameters.Builder();
        smtp.setDescription(getString(R.string.share_clip_description, getString(R.string.app_name)));
        smtp.setImageUrl(Uri.parse(clip.screenshot));
        if (TextUtils.isEmpty(clip.description)) {
            smtp.setTitle(getString(R.string.share_clip_title, '@' + clip.user.username));
        } else {
            smtp.setTitle(clip.description);
        }

        Uri base = Uri.parse(getString(R.string.server_url));
        Uri link = base.buildUpon()
                .path("links/clips")
                .appendQueryParameter("first", clip.id + "")
                .appendQueryParameter("package", BuildConfig.APPLICATION_ID)
                .build();
        Task<ShortDynamicLink> task = FirebaseDynamicLinks.getInstance()
                .createDynamicLink()
                .setLink(link)
                .setDomainUriPrefix(String.format(Locale.US, "https://%s/", getString(R.string.sharing_links_domain)))
                .setSocialMetaTagParameters(smtp.build())
                .buildShortDynamicLink();
        task.addOnCompleteListener(this, result -> {
            progress.dismiss();
            if (result.isSuccessful()) {
                ShortDynamicLink sdl = result.getResult();
                shareLink(sdl.getShortLink(), target);
            } else {
                Log.e(TAG, "Could not generate short dynamic link for clip.", task.getException());
            }
        });
    }

    private void shareLink(User user, @Nullable SharingTarget target) {
        KProgressHUD progress = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel(getString(R.string.progress_title))
                .setCancellable(false)
                .show();
        DynamicLink.SocialMetaTagParameters.Builder smtp =
                new DynamicLink.SocialMetaTagParameters.Builder();
        smtp.setDescription(getString(R.string.share_user_description, getString(R.string.app_name)));
        if (!TextUtils.isEmpty(user.photo)) {
            smtp.setImageUrl(Uri.parse(user.photo));
        }

        if (TextUtils.isEmpty(user.bio)) {
            smtp.setTitle(getString(R.string.share_user_title, '@' + user.username));
        } else {
            smtp.setTitle(user.bio);
        }

        Uri base = Uri.parse(getString(R.string.server_url));
        Uri link = base.buildUpon()
                .path("links/users")
                .appendQueryParameter("user", user.id + "")
                .appendQueryParameter("package", BuildConfig.APPLICATION_ID)
                .build();
        Task<ShortDynamicLink> task = FirebaseDynamicLinks.getInstance()
                .createDynamicLink()
                .setLink(link)
                .setDomainUriPrefix(String.format(Locale.US, "https://%s/", getString(R.string.sharing_links_domain)))
                .setSocialMetaTagParameters(smtp.build())
                .buildShortDynamicLink();
        task.addOnCompleteListener(this, result -> {
            progress.dismiss();
            if (result.isSuccessful()) {
                ShortDynamicLink sdl = result.getResult();
                shareLink(sdl.getShortLink(), target);
            } else {
                Log.e(TAG, "Could not generate short dynamic link for user.", task.getException());
            }
        });
    }

    public void showAbout() {
        NavDirections direction = MainNavigationDirections.actionShowAbout();
        findNavController().navigate(direction);
    }

    public void showClips(String title, Bundle params) {
        NavDirections direction = MainNavigationDirections.actionShowClipsGrid(title, params, true);
        findNavController().navigate(direction);
    }

    public void showCommentsPage(int clip) {
        NavDirections direction = MainNavigationDirections.actionShowComments(clip);
        findNavController().navigate(direction);
    }

    public void showEditClip(int clip) {
        NavDirections direction = MainNavigationDirections.actionShowEditClip(clip);
        findNavController().navigate(direction);
    }

    public void showEditProfile() {
        NavDirections direction = MainNavigationDirections.actionShowEditProfile();
        findNavController().navigate(direction);
    }

    public void showFollowerFollowing(int user, boolean following) {
        NavDirections direction = MainNavigationDirections.actionShowFollowers(user, following);
        findNavController().navigate(direction);
    }

    public void showLoginSheet() {
        View sheet = findViewById(R.id.login_sheet);
        if (mAd != null) {
            View ad = mAd.create(this);
            if (ad != null) {
                LinearLayout banner = findViewById(R.id.banner);
                banner.removeAllViews();
                banner.addView(ad);
            }
        }

        BottomSheetBehavior<View> bsb = BottomSheetBehavior.from(sheet);
        bsb.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public void showMessages(String title, int thread) {
        NavDirections direction = MainNavigationDirections.actionShowMessages(title, thread);
        findNavController().navigate(direction);
    }

    public void showNews() {
        findNavController().navigate(MainNavigationDirections.actionShowNews());
    }

    public void showPhotoViewer(String title, Uri url) {
        NavDirections direction = MainNavigationDirections.actionShowPhotoViewer(title, url);
        findNavController().navigate(direction);
    }

    public void showPlayerSlider(int clip, Bundle params) {
        NavDirections direction = MainNavigationDirections.actionShowPlayerSlider(clip, params);
        findNavController().navigate(direction);
    }
    public void showTrendingDetails(Bundle params) {
        NavDirections direction = MainNavigationDirections.actionShowTrandingDetails(params);
        findNavController().navigate(direction);
    }

    public void showProfilePage(int user) {
        NavDirections direction = MainNavigationDirections.actionShowProfile(user);
        findNavController().navigate(direction);
    }

    public void showProfilePage(String username) {
        KProgressHUD progress = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel(getString(R.string.progress_title))
                .setCancellable(false)
                .show();
        REST rest = MainApplication.getContainer().get(REST.class);
        rest.usersFind(username)
                .enqueue(new Callback<Wrappers.Single<User>>() {
                    @Override
                    public void onResponse(
                            @Nullable Call<Wrappers.Single<User>> call,
                            @Nullable Response<Wrappers.Single<User>> response
                    ) {
                        int code = response != null ? response.code() : -1;
                        Log.v(TAG, "Finding user returned " + code + '.');
                        if (code == 200) {
                            User user = response.body().data;
                            showProfilePage(user.id);
                        }

                        progress.dismiss();
                    }

                    @Override
                    public void onFailure(
                            @Nullable Call<Wrappers.Single<User>> call,
                            @Nullable Throwable t
                    ) {
                        Log.e(TAG, "Failed to find user with username " + username + ".", t);
                        progress.dismiss();
                    }
                });
    }

    private void showPromotionsIfAvailable() {
        long until = Prefs.getLong(SharedConstants.PREF_PROMOTIONS_SEEN_UNTIL, 0);
        REST rest = MainApplication.getContainer().get(REST.class);
        rest.promotionsIndex(until)
                .enqueue(new Callback<Wrappers.Paginated<Promotion>>() {
                    @Override
                    public void onResponse(
                            @Nullable Call<Wrappers.Paginated<Promotion>> call,
                            @Nullable Response<Wrappers.Paginated<Promotion>> response
                    ) {
                        int code = response != null ? response.code() : -1;
                        Log.v(TAG, "Fetching promotions returned " + code + '.');
                        if (response != null && response.isSuccessful()) {
                            Prefs.putLong(
                                    SharedConstants.PREF_PROMOTIONS_SEEN_UNTIL,
                                    System.currentTimeMillis());
                            List<Promotion> promotions = response.body().data;
                            if (promotions.isEmpty()) {
                                Log.w(TAG, "There are no banners to show.");
                            } else if (isResumed()) {
                                PromotionsDialogFragment.newInstance(promotions)
                                        .show(getSupportFragmentManager(), null);
                            }
                        }
                    }

                    @Override
                    public void onFailure(
                            @Nullable Call<Wrappers.Paginated<Promotion>> call,
                            @Nullable Throwable t
                    ) {
                        Log.e(TAG, "Failed to load promotions data from server.", t);
                    }
                });
    }

    public void showQrSheet(User user) {
        View sheet = findViewById(R.id.qr_sheet);
        Button show = findViewById(R.id.show);

        SpannableString gradientText = new SpannableString("SHOW");
        gradientText.setSpan(new TextGradient(Color.RED, Color.YELLOW, 4),
                0, gradientText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableStringBuilder sb = new SpannableStringBuilder();
        sb.append(gradientText);
        show.setText(sb);

        BottomSheetBehavior<View> bsb = BottomSheetBehavior.from(sheet);
        sheet.findViewById(R.id.scan).setOnClickListener(v -> {
            bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
            NavDirections direction = MainNavigationDirections.actionShowQrScanner();
            findNavController().navigate(direction);
        });
        sheet.findViewById(R.id.show).setOnClickListener(v -> {
            bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
            NavDirections direction = MainNavigationDirections.actionShowQr();
            findNavController().navigate(direction);
        });
        bsb.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public void showRequestVerification() {
        NavDirections direction = MainNavigationDirections.actionShowRequestVerification();
        findNavController().navigate(direction);
    }

    public void showSearch() {
        NavDirections direction = MainNavigationDirections.actionShowSearch();
        findNavController().navigate(direction);
    }

    public void showSharingOptions(Clip clip) {
        if (getResources().getBoolean(R.bool.sharing_sheet_enabled)) {
            View options = findViewById(R.id.sharing_sheet);
            TextView title = options.findViewById(R.id.header_title);
            title.setText(R.string.share_clip_chooser);
            BottomSheetBehavior<View> bsb = BottomSheetBehavior.from(options);
            options.findViewById(R.id.facebook)
                    .setOnClickListener(v -> {
                        bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        share(clip, SharingTarget.FACEBOOK);
                    });
            options.findViewById(R.id.facebookStory)
                    .setOnClickListener(v -> {
                        bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        share(clip, SharingTarget.FACEBOOK);
                    });
            options.findViewById(R.id.instagram)
                    .setOnClickListener(v -> {
                        bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        share(clip, SharingTarget.INSTAGRAM);
                    });
            options.findViewById(R.id.instagramStory)
                    .setOnClickListener(v -> {
                        bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        share(clip, SharingTarget.INSTAGRAM);
                    });
            options.findViewById(R.id.twitter)
                    .setOnClickListener(v -> {
                        bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        share(clip, SharingTarget.TWITTER);
                    });
            options.findViewById(R.id.whatsapp)
                    .setOnClickListener(v -> {
                        bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        share(clip, SharingTarget.WHATSAPP);
                    });
            options.findViewById(R.id.whatsappStatus)
                    .setOnClickListener(v -> {
                        bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        share(clip, SharingTarget.WHATSAPP);
                    });
            options.findViewById(R.id.download)
                    .setOnClickListener(v -> {
                        bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
                       submitForDownload(clip);
                    });
            options.findViewById(R.id.other)
                    .setOnClickListener(v -> {
                        bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        share(clip, null);
                    });
            bsb.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            share(clip, null);
        }
    }

    @AfterPermissionGranted(SharedConstants.REQUEST_CODE_PERMISSIONS_DOWNLOAD)
    private void submitForDownload(Clip mClip) {
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mClip.video));
            //request.addRequestHeader("Accept", "application/pdf");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            String filename = "VDO_" + mClip.id + "_" + System.currentTimeMillis() + ".mp4";
// Save the file in the "Downloads" folder of SDCARD
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);

            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            downloadManager.enqueue(request);

            Toast.makeText(getBaseContext(), R.string.message_downloading_async, Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }

       /* WorkManager wm = WorkManager.getInstance(getBaseContext());
        File fixed = TempUtil.createNewFile(getBaseContext(), ".mp4");
        boolean async = getResources().getBoolean(R.bool.downloads_async_enabled);
        String name = "VDO_" + mClip.id + "_" + System.currentTimeMillis() + ".mp4";
        Data data = new Data.Builder()
                .putString(SaveToGalleryWorker.KEY_FILE, fixed.getAbsolutePath())
                .putString(SaveToGalleryWorker.KEY_NAME, name)
                .putBoolean(SaveToGalleryWorker.KEY_NOTIFICATION, async)
                .build();
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(SaveToGalleryWorker.class)
                .setInputData(data)
                .build();
        if (getResources().getBoolean(R.bool.downloads_watermark_enabled)) {
            File original = TempUtil.createNewFile(getBaseContext(), ".mp4");
            File watermarked = TempUtil.createNewFile(getBaseContext(), ".mp4");
            wm.beginWith(VideoUtil.createDownloadRequest(mClip.video, original, async))
                    .then(VideoUtil.createWatermarkRequest(mClip, original, watermarked, async))
                    .then(VideoUtil.createFastStartRequest(watermarked, fixed))
                    .then(request)
                    .enqueue();
        } else {
            wm.beginWith(VideoUtil.createDownloadRequest(mClip.video, fixed, async))
                    .then(request)
                    .enqueue();
        }

        if (async) {
            Toast.makeText(getBaseContext(), R.string.message_downloading_async, Toast.LENGTH_SHORT).show();
        } else {
            KProgressHUD progress = KProgressHUD.create(getBaseContext())
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
                            Toast.makeText(getBaseContext(), R.string.message_clip_downloaded, Toast.LENGTH_SHORT).show();
                        }
                    });
        }*/
    }


    public void showSharingOptions(User user) {
        if (getResources().getBoolean(R.bool.sharing_sheet_enabled)) {
            View options = findViewById(R.id.sharing_sheet);
            TextView title = options.findViewById(R.id.header_title);
            title.setText(R.string.share_user_chooser);
            BottomSheetBehavior<View> bsb = BottomSheetBehavior.from(options);
            options.findViewById(R.id.facebook)
                    .setOnClickListener(v -> {
                        bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        shareLink(user, SharingTarget.FACEBOOK);
                    });

            options.findViewById(R.id.facebookStory)
                    .setOnClickListener(v -> {
                        bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        shareLink(user, SharingTarget.FACEBOOK);
                    });
            options.findViewById(R.id.instagram)
                    .setOnClickListener(v -> {
                        bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        shareLink(user, SharingTarget.INSTAGRAM);
                    });

            options.findViewById(R.id.instagramStory)
                    .setOnClickListener(v -> {
                        bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        shareLink(user, SharingTarget.INSTAGRAM);
                    });
            options.findViewById(R.id.twitter)
                    .setOnClickListener(v -> {
                        bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        shareLink(user, SharingTarget.TWITTER);
                    });
            options.findViewById(R.id.whatsapp)
                    .setOnClickListener(v -> {
                        bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        shareLink(user, SharingTarget.WHATSAPP);
                    });
            options.findViewById(R.id.whatsappStatus)
                    .setOnClickListener(v -> {
                        bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        shareLink(user, SharingTarget.WHATSAPP);
                    });

            options.findViewById(R.id.other)
                    .setOnClickListener(v -> {
                        bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        shareLink(user, null);
                    });
            bsb.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            shareLink(user, null);
        }
    }

    private void setSlider() {
        sliderItems.clear();
        //  for (int i=0; i<homeDashBoardlists.get(0).getBannerData().size(); i++) {
        sliderItems.add(new SliderItem("Text Here", R.drawable.slideone));
        sliderItems.add(new SliderItem("Text Here", R.drawable.slidetwo));
        sliderItems.add(new SliderItem("Text Here", R.drawable.slidethree));
        //};
        adapter = new SliderAdapterExample(getApplicationContext(), sliderItems);
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

    public void showThreads() {
        findNavController().navigate(MainNavigationDirections.actionShowThreads());
    }

    public void showUrlBrowser(String url, @Nullable String title, boolean internal) {
        if (internal || !getResources().getBoolean(R.bool.external_browser_enabled)) {
            FinestWebView.Builder builder = new FinestWebView.Builder(this);
            if (title != null) {
                builder.titleDefault(title);
            }

            builder.show(url);
        } else {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        }
    }

    private void submitForUseAudio(Clip clip) {
        KProgressHUD progress = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel(getString(R.string.progress_title))
                .setCancellable(false)
                .show();
        File downloaded = TempUtil.createNewFile(this, ".mp4");
        OneTimeWorkRequest request = VideoUtil.createDownloadRequest(clip.video, downloaded, false);
        WorkManager wm = WorkManager.getInstance(this);
        wm.enqueue(request);
        wm.getWorkInfoByIdLiveData(request.getId())
                .observe(this, info -> {
                    boolean ended = info.getState() == WorkInfo.State.CANCELLED
                            || info.getState() == WorkInfo.State.FAILED
                            || info.getState() == WorkInfo.State.SUCCEEDED;
                    if (ended) {
                        progress.dismiss();
                    }

                    if (info.getState() == WorkInfo.State.SUCCEEDED) {
                        Intent intent = new Intent(this, RecorderActivity.class);
                        intent.putExtra(RecorderActivity.EXTRA_AUDIO, Uri.fromFile(downloaded));
                        startActivity(intent);
                    }
                });
    }

    private void syncFcmToken() {
        String token = Prefs.getString(SharedConstants.PREF_FCM_TOKEN, null);
        if (TextUtils.isEmpty(token)) {
            return;
        }

        long synced = Prefs.getLong(SharedConstants.PREF_FCM_TOKEN_SYNCED_AT, 0);
        if (synced <= 0 || synced < (System.currentTimeMillis() - SharedConstants.SYNC_FCM_INTERVAL)) {
            Prefs.putLong(SharedConstants.PREF_FCM_TOKEN_SYNCED_AT, System.currentTimeMillis());
            WorkRequest request = OneTimeWorkRequest.from(DeviceTokenWorker.class);
            WorkManager.getInstance(this).enqueue(request);
        }
    }

    private void updateLoginState(Token token) {
        Log.v(TAG, "Received token from server i.e., " + token);
        Prefs.putString(SharedConstants.PREF_SERVER_TOKEN, token.token);
        Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();
        restartActivity(token, null);
    }

    public static class MainActivityViewModel extends ViewModel {

        public boolean areThreadsInvalid;
        public boolean isProfileInvalid;
        public final MutableLiveData<Integer> notifications = new MutableLiveData<>(0);
        public final MutableLiveData<String> searchTerm = new MutableLiveData<>();
        public final MutableLiveData<User> user = new MutableLiveData<>();
        public final MutableLiveData<LoadingState> state = new MutableLiveData<>(LoadingState.IDLE);

        public boolean isLoggedIn() {
            return user.getValue() != null;
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
        public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
            View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_slider_layout_item, null);
            return new SliderAdapterVH(inflate);
        }

        @Override
        public void onBindViewHolder(SliderAdapterVH viewHolder, final int position) {

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
