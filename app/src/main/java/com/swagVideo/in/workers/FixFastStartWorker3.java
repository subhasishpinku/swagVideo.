package com.swagVideo.in.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.arthenica.mobileffmpeg.FFmpeg;

import java.io.File;

public class FixFastStartWorker3 extends Worker {

    public static final String KEY_INPUT = "input";
    public static final String KEY_OUTPUT = "output";
    private static final String TAG = "FixFastStartWorker3";

    public FixFastStartWorker3(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    @SuppressWarnings("ConstantConditions")
    public Result doWork() {
        File input = new File(getInputData().getString(KEY_INPUT));
        File output = new File(getInputData().getString(KEY_OUTPUT));
        boolean success = doActualWork(input, output);
        if (!success && !input.delete()) {
            Log.w(TAG, "Could not delete input file: " + input);
        }

        if (!success && !output.delete()) {
            Log.w(TAG, "Could not delete failed output file: " + input);
        }

        return success ? Result.success() : Result.failure();
    }

    private boolean doActualWork(File input, File output) {
        int code = FFmpeg.execute(new String[] {
                "-i", input.getAbsolutePath(),
                "-c", "copy", "-movflags", "faststart",
                output.getAbsolutePath()
        });
        return code == 0;
    }
}
