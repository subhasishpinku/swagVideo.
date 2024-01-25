package com.swagVideo.in.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.pixplicity.easyprefs.library.Prefs;
import com.swagVideo.in.MainApplication;
import com.swagVideo.in.R;
import com.swagVideo.in.SharedConstants;
import com.swagVideo.in.activities.MainActivity;
import com.swagVideo.in.activities.RecorderActivity;
import com.swagVideo.in.ads.BannerAdProvider;
import com.swagVideo.in.common.DiffUtilCallback;
import com.swagVideo.in.common.LoadingState;
import com.swagVideo.in.data.ClipDataSource;
import com.swagVideo.in.data.ClipItemDataSource;
import com.swagVideo.in.data.api.REST;
import com.swagVideo.in.data.models.Advertisement;
import com.swagVideo.in.data.models.Clip;
import com.swagVideo.in.data.models.Song;
import com.swagVideo.in.data.models.Wrappers;
import com.swagVideo.in.utils.AdsUtil;
import com.swagVideo.in.utils.TextFormatUtil;
import com.swagVideo.in.utils.VideoUtil;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClipGridFragment extends Fragment {

    private static final String ARG_ADS = "ads";
    private static final String ARG_PARAMS = "params";
    private static final String ARG_TITLE = "title";
    private static final String TAG = "ClipGridFragment";

    private BannerAdProvider mAd;
    private boolean mAds;
    private ClipGridFragmentViewModel mModel1;
    private MainActivity.MainActivityViewModel mModel2;
    private Bundle mParams;
    private String mTitle;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        ClipGridFragmentViewModel.Factory factory =
                new ClipGridFragmentViewModel.Factory(mParams);
        mModel1 = new ViewModelProvider(this, factory).get(ClipGridFragmentViewModel.class);
        mModel2 = new ViewModelProvider(requireActivity())
                .get(MainActivity.MainActivityViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_clip_grid, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        int song = mParams.getInt(ClipDataSource.PARAM_SONG);
        if (song > 0) {
            LoadingState state = mModel1.state2.getValue();
            if (state != LoadingState.LOADED && state != LoadingState.LOADING) {
                fetchSong(song);
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (TextUtils.isEmpty(mTitle)) {
            view.findViewById(R.id.header).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.header_back)
                    .setOnClickListener(v -> ((MainActivity) requireActivity()).popBackStack());
            TextView title = view.findViewById(R.id.header_title);
            //title.setText(mTitle);
            title.setText("");
            view.findViewById(R.id.header_more).setVisibility(View.GONE);
        }

        MaterialCardView mcvHas = view.findViewById(R.id.mcvHas);
        TextView tvUserName = view.findViewById(R.id.tvUserName);
        if(mTitle != null){
            mcvHas.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.bg_round_red_yellow_sheet3));
            tvUserName.setText(mTitle.split("#")[1]);
            mcvHas.setVisibility(View.VISIBLE);
            tvUserName.setVisibility(View.VISIBLE);
        }

        RecyclerView clips = view.findViewById(R.id.clips);
        ClipGridAdapter adapter = new ClipGridAdapter();
        clips.setAdapter(new SlideInBottomAnimationAdapter(adapter));
        GridLayoutManager glm = new GridLayoutManager(requireContext(), 3);
        //LinearLayoutManager glm =new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false);
        clips.setLayoutManager(glm);
        mModel1.clips.observe(getViewLifecycleOwner(), adapter::submitList);
        SwipeRefreshLayout swipe = view.findViewById(R.id.swipe);
        swipe.setOnRefreshListener(() -> {
            ClipItemDataSource source = mModel1.factory.source.getValue();
            if (source != null) {
                source.invalidate();
            }
        });
        View empty = view.findViewById(R.id.empty);
        View loading = view.findViewById(R.id.loading);
        mModel1.state1.observe(getViewLifecycleOwner(), state -> {
            if (state != LoadingState.LOADING) {
                swipe.setRefreshing(false);
            }

            List<?> list = mModel1.clips.getValue();
            if (state == LoadingState.LOADING) {
                empty.setVisibility(View.GONE);
            } else {
                empty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
            }

            loading.setVisibility(state == LoadingState.LOADING ? View.VISIBLE : View.GONE);
        });
        View song = view.findViewById(R.id.song);
        SimpleDraweeView icon = song.findViewById(R.id.icon);
        TextView title = song.findViewById(R.id.title);
        TextView info = song.findViewById(R.id.info);
        View record = song.findViewById(R.id.record);
        View details = song.findViewById(R.id.details);
        mModel1.song.observe(getViewLifecycleOwner(), model -> {
            if (model == null) {
                song.setVisibility(View.GONE);
            } else {
                if (TextUtils.isEmpty(model.cover)) {
                    icon.setActualImageResource(R.drawable.image_placeholder);
                } else {
                    icon.setImageURI(model.cover);
                }

                title.setText(model.title);
                List<String> information = new ArrayList<>();
                if (!TextUtils.isEmpty(model.album)) {
                    information.add(model.album);
                }

                if (!TextUtils.isEmpty(model.artist)) {
                    information.add(model.artist);
                }

                info.setText(StringUtils.join(information, " | "));
                record.setOnClickListener(v -> {
                    if (mModel2.isLoggedIn()) {
                        downloadForUse(model);
                    } else {
                        ((MainActivity) requireActivity()).showLoginSheet();
                    }
                });
                if (TextUtils.isEmpty(model.details)) {
                    details.setVisibility(View.GONE);
                } else {
                    details.setOnClickListener(v ->
                            ((MainActivity) requireActivity())
                                    .showUrlBrowser(model.details, null, false));
                    details.setVisibility(View.VISIBLE);
                }

                song.setVisibility(View.VISIBLE);
            }
        });
        if (mAds && mAd != null) {
            View ad = mAd.create(requireContext());
            if (ad != null) {
                LinearLayout banner = view.findViewById(R.id.banner);
                banner.removeAllViews();
                banner.addView(ad);
            }
        }
    }

    private void downloadForUse(Song song) {
        File songs = new File(requireContext().getFilesDir(), "songs");
        if (!songs.exists() && !songs.mkdirs()) {
            Log.w(TAG, "Could not create directory at " + songs);
        }

        String extension = song.audio.substring(song.audio.lastIndexOf(".") + 1);
        File audio = new File(songs, song.id + extension);
        if (audio.exists()) {
            openRecorder(song, audio);
            return;
        }

        KProgressHUD progress = KProgressHUD.create(requireActivity())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel(getString(R.string.progress_title))
                .setCancellable(false)
                .show();
        WorkRequest request = VideoUtil.createDownloadRequest(song.audio, audio, false);
        WorkManager wm = WorkManager.getInstance(requireContext());
        wm.enqueue(request);
        wm.getWorkInfoByIdLiveData(request.getId())
                .observe(getViewLifecycleOwner(), info -> {
                    boolean ended = info.getState() == WorkInfo.State.CANCELLED
                            || info.getState() == WorkInfo.State.FAILED
                            || info.getState() == WorkInfo.State.SUCCEEDED;
                    if (ended) {
                        progress.dismiss();
                    }

                    if (info.getState() == WorkInfo.State.SUCCEEDED) {
                        openRecorder(song, audio);
                    }
                });
    }

    private void fetchSong(int id) {
        mModel1.state2.postValue(LoadingState.LOADING);
        REST rest = MainApplication.getContainer().get(REST.class);
        rest.songsShow(id)
                .enqueue(new Callback<Wrappers.Single<Song>>() {

                    @Override
                    public void onResponse(
                            @Nullable Call<Wrappers.Single<Song>> call,
                            @Nullable Response<Wrappers.Single<Song>> response
                    ) {
                        int code = response != null ? response.code() : -1;
                        Log.v(TAG, "Loading song information returned " + code + '.');
                        if (response != null && response.isSuccessful()) {
                            //noinspection ConstantConditions
                            mModel1.song.postValue(response.body().data);
                            mModel1.state2.postValue(LoadingState.LOADED);
                        } else {
                            mModel1.state2.postValue(LoadingState.ERROR);
                        }
                    }

                    @Override
                    public void onFailure(
                            @Nullable Call<Wrappers.Single<Song>> call,
                            @Nullable Throwable t
                    ) {
                        Log.e(TAG, "Failed to load song information.", t);
                        mModel1.state2.postValue(LoadingState.ERROR);
                    }
                });
    }

    public static ClipGridFragment newInstance(
            @Nullable Bundle params,
            @Nullable String title,
            boolean ads
    ) {
        ClipGridFragment fragment = new ClipGridFragment();
        Bundle arguments = new Bundle();
        arguments.putBundle(ARG_PARAMS, params);
        arguments.putString(ARG_TITLE, title);
        arguments.putBoolean(ARG_ADS, ads);
        fragment.setArguments(arguments);
        return fragment;
    }

    private void openRecorder(Song song, File file) {
        Intent intent = new Intent(requireContext(), RecorderActivity.class);
        intent.putExtra(RecorderActivity.EXTRA_AUDIO, Uri.fromFile(file));
        intent.putExtra(RecorderActivity.EXTRA_SONG, song);
        startActivity(intent);
    }

    private void showClipPlayer(int clip) {
        ((MainActivity) requireActivity()).showPlayerSlider(clip, mParams);
    }

    private class ClipGridAdapter extends PagedListAdapter<Clip, ClipGridViewHolder> {

        protected ClipGridAdapter() {
            super(new DiffUtilCallback<>(i -> i.id));
        }

        @NonNull
        @Override
        public ClipGridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_clip, parent, false);
            return new ClipGridViewHolder(view);
        }

        @Override
        @SuppressLint("SetTextI18n")
        public void onBindViewHolder(@NonNull ClipGridViewHolder holder, int position) {
            try {
                Clip clip = getItem(position);
                //noinspection ConstantConditions
                holder.likes.setText(TextFormatUtil.toShortNumber(clip.likesCount));
                holder.preview.setImageURI(clip.screenshot);
                holder.itemView.setOnClickListener(v -> showClipPlayer(clip.id));

                holder.tv_user_name.setText(clip.getUser().name);
                holder.tvView.setText(clip.viewsCount + "");
                Glide.with(getContext()).load(clip.getUser().photo).apply(new RequestOptions().circleCrop())
                        .error(R.drawable.user).into(holder.iv_user);

                if (mParams.containsKey(ClipDataSource.PARAM_MINE)) {
                    holder._private.setVisibility(clip._private ? View.VISIBLE : View.GONE);
                    holder.disapproved.setVisibility(clip.approved ? View.GONE : View.VISIBLE);
                } else {
                    holder._private.setVisibility(View.GONE);
                    holder.disapproved.setVisibility(View.GONE);
                }

                holder.ivDelete.setVisibility(clip.user.me ? View.VISIBLE : View.GONE);

                holder.ivDelete.setOnClickListener(v -> {
                    confirmDeletion(clip.id);
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static class ClipGridFragmentViewModel extends ViewModel {

        public ClipGridFragmentViewModel(@NonNull Bundle params) {
            PagedList.Config config = new PagedList.Config.Builder()
                    .setPageSize(SharedConstants.DEFAULT_PAGE_SIZE)
                    .build();
            factory = new ClipItemDataSource.Factory(params);
            state1 = Transformations.switchMap(factory.source, input -> input.state);
            clips = new LivePagedListBuilder<>(factory, config).build();
        }

        public final LiveData<PagedList<Clip>> clips;
        public final ClipItemDataSource.Factory factory;
        public final MutableLiveData<Song> song = new MutableLiveData<>();
        public final LiveData<LoadingState> state1;
        public final MutableLiveData<LoadingState> state2 = new MutableLiveData<>(LoadingState.IDLE);

        private static class Factory implements ViewModelProvider.Factory {

            @NonNull
            private final Bundle mParams;

            public Factory(@NonNull Bundle params) {
                mParams = params;
            }

            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                //noinspection unchecked
                return (T) new ClipGridFragmentViewModel(mParams);
            }
        }
    }

    private static class ClipGridViewHolder extends RecyclerView.ViewHolder {

        public SimpleDraweeView preview;
        public View _private;
        public View disapproved;
        public TextView likes,tv_user_name,tvView;
        public ImageView ivDelete,iv_user;

        public ClipGridViewHolder(@NonNull View root) {
            super(root);
            preview = root.findViewById(R.id.preview);
            _private = root.findViewById(R.id._private);
            disapproved = root.findViewById(R.id.disapproved);
            likes = root.findViewById(R.id.likes);
            ivDelete = root.findViewById(R.id.iv_delete);
            iv_user = root.findViewById(R.id.iv_user);
            tv_user_name = root.findViewById(R.id.tv_user_name);
            tvView = root.findViewById(R.id.tvView);
        }
    }

    private void confirmDeletion(int id) {
        new MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.confirmation_delete_clip)
                .setNegativeButton(R.string.cancel_button, (dialog, i) -> dialog.cancel())
                .setPositiveButton(R.string.yes_button, (dialog, i) -> {
                    dialog.dismiss();
                    deleteClip(id);
                })
                .show();
    }

    private void deleteClip(int id) {
        KProgressHUD progress = KProgressHUD.create(requireActivity())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel(getString(R.string.progress_title))
                .setCancellable(false)
                .show();
        REST rest = MainApplication.getContainer().get(REST.class);
        rest.clipsDelete(id)
                .enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(
                            @Nullable Call<ResponseBody> call,
                            @Nullable Response<ResponseBody> response
                    ) {
                        int code = response != null ? response.code() : -1;
                        Log.v(TAG, "Deleting clip returned " + code + '.');
                        progress.dismiss();
                        if (code == 200) {
                            ClipItemDataSource source = mModel1.factory.source.getValue();
                            if (source != null) {
                                source.invalidate();
                            }
                        }
                    }

                    @Override
                    public void onFailure(
                            @Nullable Call<ResponseBody> call,
                            @Nullable Throwable t
                    ) {
                        Log.e(TAG, "Failed to delete selected clip.", t);
                        Toast.makeText(getActivity(), "Failed to delete selected clip!", Toast.LENGTH_SHORT).show();
                        progress.dismiss();
                    }
                });
    }

}
