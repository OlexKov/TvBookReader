package com.example.bookreader.customclassses;

import static com.example.bookreader.utility.ImageHelper.getBlurBitmap;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;
import androidx.leanback.app.BackgroundManager;

import com.example.bookreader.utility.ProcessRuner;

public class BackgroundController {
    private final ProcessRuner processRuner = new ProcessRuner();
    private final BackgroundManager mBackgroundManager;
    private final Activity activity;

    public BackgroundController(@NonNull Activity activity){
        this.activity = activity;
        mBackgroundManager = BackgroundManager.getInstance(activity);
        if (!mBackgroundManager.isAttached()) {
            mBackgroundManager.attach(activity.getWindow());
        }
        DisplayMetrics mMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    public void setBitmapBackground(Bitmap background){
        mBackgroundManager.setBitmap(background);
    }

    public void setDrawableBackground(Drawable backgroundDrawable){
        mBackgroundManager.setDrawable(backgroundDrawable);
    }

    public void setBlurBackground(String imagePath,int radius,int sampling){
        getBlurBitmap(activity,radius,sampling,imagePath,mBackgroundManager::setBitmap);
    }

    public void setBitmapBackgroundDelayed(Bitmap background,int delay){
       processRuner.runDelayed(delay,()->setBitmapBackground(background));
    }

    public void setDrawableBackgroundDelayed(Drawable backgroundDrawable,int delay){
        processRuner.runDelayed(delay,()->setDrawableBackground(backgroundDrawable));
    }

    public void setBlurBackgroundDelayed(String imagePath,int radius,int sampling,int delay){
        processRuner.runDelayed(delay,()->setBlurBackground(imagePath,radius,sampling));
    }

}
