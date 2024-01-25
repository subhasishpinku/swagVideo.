package com.swagVideo.in.fragments;

import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.Collections;
import java.util.List;

import com.swagVideo.in.R;
import com.swagVideo.in.SharedConstants;
import com.swagVideo.in.activities.StickerPickerActivity;
import com.swagVideo.in.common.DiffUtilCallback;
import com.swagVideo.in.common.LoadingState;
import com.swagVideo.in.data.StickerDataSource;
import com.swagVideo.in.data.models.Sticker;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class StickerPickerFragment extends Fragment {

    private static final String ARG_SECTION = "section";

    private StickerPickerFragmentViewModel mModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int section = requireArguments().getInt(ARG_SECTION);
        StickerPickerFragmentViewModel.Factory factory =
                new StickerPickerFragmentViewModel.Factory(section);
        mModel = new ViewModelProvider(this, factory)
                .get(StickerPickerFragmentViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sticker_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        StickerAdapter adapter = new StickerAdapter();
        RecyclerView stickers = view.findViewById(R.id.stickers);
        stickers.setAdapter(adapter);
        stickers.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        OverScrollDecoratorHelper.setUpOverScroll(
                stickers, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
        View empty = view.findViewById(R.id.empty);
        View loading = view.findViewById(R.id.loading);
        mModel.stickers.observe(getViewLifecycleOwner(), adapter::submitList);
        mModel.state.observe(getViewLifecycleOwner(), state -> {
            List<?> list = mModel.stickers.getValue();
            if (state == LoadingState.LOADING) {
                empty.setVisibility(View.GONE);
            } else {
                empty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
            }

            loading.setVisibility(state == LoadingState.LOADING ? View.VISIBLE : View.GONE);
        });
    }

    public static StickerPickerFragment newInstance(int section) {
        Bundle arguments = new Bundle();
        arguments.putInt(ARG_SECTION, section);
        StickerPickerFragment fragment = new StickerPickerFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    private void submitSelection(Sticker sticker) {
        ((StickerPickerActivity)requireActivity()).closeWithSelection(sticker);
    }

    private class StickerAdapter extends PagedListAdapter<Sticker, StickerViewHolder> {

        protected StickerAdapter() {
            super(new DiffUtilCallback<>(i -> i.id));
        }

        @Override
        public void onBindViewHolder(@NonNull StickerViewHolder holder, int position) {
            Sticker sticker = getItem(position);
            //noinspection ConstantConditions
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setControllerListener(new BaseControllerListener<ImageInfo>() {

                        @Override
                        public void onFinalImageSet(String id, ImageInfo info, Animatable animatable) {
                            holder.shimmer.stopShimmer();
                            holder.shimmer.setVisibility(View.GONE);
                        }

                        @Override
                        public void onSubmit(String id, Object callerContext) {
                            holder.shimmer.setVisibility(View.VISIBLE);
                            holder.shimmer.startShimmer();
                        }
                    })
                    .setOldController(holder.image.getController())
                    .setUri(sticker.image)
                    .build();
            holder.image.setController(controller);
            holder.itemView.setOnClickListener(v -> submitSelection(sticker));
        }

        @NonNull
        @Override
        public StickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_sticker, parent, false);
            return new StickerViewHolder(view);
        }
    }

    public static class StickerPickerFragmentViewModel extends ViewModel {

        public StickerPickerFragmentViewModel(int section) {
            PagedList.Config config = new PagedList.Config.Builder()
                    .setPageSize(SharedConstants.DEFAULT_PAGE_SIZE)
                    .build();
            factory = new StickerDataSource.Factory(Collections.singletonList(section), null);
            state = Transformations.switchMap(factory.source, input -> input.state);
            stickers = new LivePagedListBuilder<>(factory, config).build();
        }

        public final LiveData<PagedList<Sticker>> stickers;
        public final StickerDataSource.Factory factory;
        public final LiveData<LoadingState> state;

        private static class Factory implements ViewModelProvider.Factory {

            private final int mSection;

            public Factory(int section) {
                mSection = section;
            }

            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                //noinspection unchecked
                return (T)new StickerPickerFragmentViewModel(mSection);
            }
        }
    }

    private static final class StickerViewHolder extends RecyclerView.ViewHolder {

        public SimpleDraweeView image;
        public ShimmerFrameLayout shimmer;

        public StickerViewHolder(@NonNull View root) {
            super(root);
            image = root.findViewById(R.id.image);
            shimmer = root.findViewById(R.id.shimmer);
        }
    }
}
