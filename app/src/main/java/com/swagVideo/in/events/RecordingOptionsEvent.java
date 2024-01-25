package com.swagVideo.in.events;

import androidx.annotation.Nullable;

import com.swagVideo.in.data.models.Clip;

public class RecordingOptionsEvent {

    private final Clip mClip;

    public RecordingOptionsEvent(@Nullable Clip clip) {
        mClip = clip;
    }

    public Clip getClip() {
        return mClip;
    }
}
