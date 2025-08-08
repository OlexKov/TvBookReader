package com.example.bookreader.presenters;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.RowHeaderPresenter;

public class DividerRowHeaderPresenter extends RowHeaderPresenter {

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = new View(parent.getContext());
        view.setFocusable(false);
        view.setFocusableInTouchMode(false);
        view.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                2 // або 1px, або dp, залежно від роздільника
        ));
        view.setBackgroundColor(Color.GRAY); // Колір роздільника
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Presenter.ViewHolder viewHolder, Object item) {}
}
