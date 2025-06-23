package com.example.bookreader.presenters;

import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.leanback.widget.Presenter;

import com.example.bookreader.R;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class BookPreviewPresenter extends Presenter {
    private static final int WIDTH_ITEM = 200;
    private static final int HEIGHT_ITEM = 300;
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        TextView view = new TextView(parent.getContext());
        view.setLayoutParams(new ViewGroup.LayoutParams(WIDTH_ITEM, HEIGHT_ITEM));
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.setGravity(Gravity.CENTER);
        view.setBackgroundColor(ContextCompat.getColor(parent.getContext(), R.color.default_background));
        view.setTextColor(Color.WHITE);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, @Nullable Object item) {
        TextView textView = (TextView) viewHolder.view;
        String str = (String) item;
        textView.setText(str);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {

    }
}
