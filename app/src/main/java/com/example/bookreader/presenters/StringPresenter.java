package com.example.bookreader.presenters;

import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.leanback.widget.Presenter;

public class StringPresenter extends Presenter {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        TextView textView = new TextView(parent.getContext());
        textView.setFocusable(true);
        textView.setFocusableInTouchMode(true);
        textView.setBackgroundColor(Color.DKGRAY);
        textView.setTextColor(Color.WHITE);
        textView.setPadding(40, 10, 40, 10);
        
        return new ViewHolder(textView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        String text = (String) item;
        ((TextView) viewHolder.view).setText(text);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        // Нічого очищати не потрібно
    }
}
