package com.example.bookreader.listeners;

import android.view.View;

public class HeaderButtonOnFocusListener implements View.OnFocusChangeListener {
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        float scale = hasFocus ? 1.2f : 1f;
        v.animate().scaleX(scale).scaleY(scale).setDuration(150).start();
    }
}
