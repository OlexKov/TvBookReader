package com.example.bookreader.listeners;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.example.bookreader.R;

public class HeaderButtonOnFocusListener implements View.OnFocusChangeListener {
    private Animation pulse;
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        float scale = hasFocus ? 1.2f : 1f;
        v.animate().scaleX(scale).scaleY(scale).setDuration(150).start();

        if(hasFocus){
            if(pulse == null){
                pulse = AnimationUtils.loadAnimation(v.getContext(), R.anim.button_puls);
            }
            v.startAnimation(pulse);
        }
        else{
            v.clearAnimation();
        }

    }
}
