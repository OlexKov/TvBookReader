package com.example.bookreader.presenters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.view.ViewCompat;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.RowHeaderPresenter;

import com.example.bookreader.R;
import com.example.bookreader.customclassses.TextIcon;
import com.example.bookreader.extentions.IconHeader;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class TextIconPresenter extends Presenter {
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.text_icon, null);
        view.setBackground(null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, @Nullable Object item) {
        if(item instanceof TextIcon){
            TextIcon icon = (TextIcon) item;
            View rootView = viewHolder.view;
            TextView view =  rootView.findViewById(R.id.header_label);
            view.setText(icon.name);
            ImageView iconView = rootView.findViewById(R.id.header_icon);
            iconView.setImageResource(((TextIcon) item).idIconRs);
            rootView.setOnFocusChangeListener((v, hasFocus) -> {
                float scale = hasFocus ? 1.3f : 1f;
                v.animate()
                        .scaleX(scale)
                        .scaleY(scale)
                        .setDuration(300) // або інша тривалість у мс
                        .start();
           });
        }

    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {

    }
}
