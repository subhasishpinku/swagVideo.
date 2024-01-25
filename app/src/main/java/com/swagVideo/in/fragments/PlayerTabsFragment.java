package com.swagVideo.in.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import com.swagVideo.in.R;
import com.swagVideo.in.activities.MainActivity;
import com.swagVideo.in.data.ClipDataSource;
import com.swagVideo.in.data.models.Clip;

import static com.swagVideo.in.fragments.NearbyFragment.clipStat;

public class PlayerTabsFragment extends Fragment {

    private MainActivity.MainActivityViewModel mModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mModel = new ViewModelProvider(requireActivity())
                .get(MainActivity.MainActivityViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_player_tabs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewPager2 pager = view.findViewById(R.id.pager);
        pager.setAdapter(new PlayerTabPagerAdapter(this));
        pager.setCurrentItem(1, false);
        TabLayout tabs = view.findViewById(R.id.tabs);
      //  tabs.setBackgroundResource(android.R.color.transparent);
        new TabLayoutMediator(tabs, pager, (tab, position) -> {
            int text; /*= position == 0
                    ? R.string.following_label
                    : R.string.for_you_label;*/
            if(position==0)
                text =R.string.following_label;
            else if(position ==1)
                text = R.string.for_you_label;
            else
                text = R.string.nearBy;;
            tab.setText(text);
        }).attach();
    }

    public static PlayerTabsFragment newInstance() {
        return new PlayerTabsFragment();
    }

    private class PlayerTabPagerAdapter extends FragmentStateAdapter {

        public PlayerTabPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                if (mModel.isLoggedIn()) {
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(ClipDataSource.PARAM_FOLLOWING, true);
                    return PlayerSliderFragment.newInstance(bundle);
                }
                return LoginRequiredFragment.newInstance();
            }else if (position == 1){
                if (mModel.isLoggedIn())
                    return PlayerSliderFragment.newInstance(null);
                else
                    return PlayerSliderFragment.newInstance(null);
            }

            return NearbyFragment.newInstance(null,null);
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        clipStat = new Clip();
    }
}
