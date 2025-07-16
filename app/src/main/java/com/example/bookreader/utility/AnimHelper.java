package com.example.bookreader.utility;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class AnimHelper {
    public static void scale(View view,float scaleFactor,boolean isScale,int scaleDuration){
        float target = isScale ? scaleFactor : 1f;

        view.animate()
                .scaleX(target)
                .scaleY(target)
                .setDuration(scaleDuration)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    public  static int convertToDp(Context context, int px){
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                32,
                context.getResources().getDisplayMetrics());
    }
}
