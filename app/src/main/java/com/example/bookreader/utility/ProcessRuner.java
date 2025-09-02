package com.example.bookreader.utility;

import android.os.Handler;
import android.os.Looper;

public class ProcessRuner {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pendingBackgroundUpdate = null;

    public void runDelayed(int delay, Runnable post) {
        if (pendingBackgroundUpdate != null) {
            handler.removeCallbacks(pendingBackgroundUpdate);
        }
        pendingBackgroundUpdate = () -> {
            post.run();
            pendingBackgroundUpdate = null;
        };
        handler.postDelayed(pendingBackgroundUpdate, delay);
    }
}
