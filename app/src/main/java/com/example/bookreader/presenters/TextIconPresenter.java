package com.example.bookreader.presenters;

import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
            View circle = rootView.findViewById(R.id.focus_circle);
            view.setText(icon.name);
            ImageView iconView = rootView.findViewById(R.id.header_icon);
            iconView.setImageResource(((TextIcon) item).idIconRs);
            rootView.setOnFocusChangeListener((v, hasFocus) -> {
                float scale = hasFocus ? 1.2f : 1f;
                if (hasFocus) {
                    circle.animate().alpha(0.2f).setDuration(200).start(); // Півпрозоре коло
                } else {
                    circle.animate().alpha(0f).setDuration(200).start(); // Приховане коло
                }
                v.animate()
                        .scaleX(scale)
                        .scaleY(scale)
                        .setDuration(300) // або інша тривалість у мс
                        .start();
           });
            Handler handler = new Handler(Looper.getMainLooper());

            rootView.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        // Швидка реакція – збільшуємо
                        circle.animate().alpha(0.3f).setDuration(50).start();
//                        v.animate()
//                                .scaleX(1.22f)
//                                .scaleY(1.22f)
//                                .setDuration(50)
//                                .start();

                    } else if (event.getAction() == KeyEvent.ACTION_UP) {
                        circle.animate().alpha(0.2f).setDuration(50).start();
                        // Затримуємо зменшення, щоб ефект не зникав миттєво
                        handler.postDelayed(() -> {
//                            v.animate()
//                                    .scaleX(1.2f)
//                                    .scaleY(1.2f)
//                                    .setDuration(50)
//                                    .start();
                        }, 50); // затримка ~80 мс

                    }
                }
                return false;
            });
        }
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {

    }
}
