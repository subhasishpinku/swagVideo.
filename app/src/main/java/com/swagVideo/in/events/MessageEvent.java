package com.swagVideo.in.events;

public class MessageEvent {

    private final int mThread;

    public MessageEvent(int thread) {
        mThread = thread;
    }

    public int getThread() {
        return mThread;
    }
}
