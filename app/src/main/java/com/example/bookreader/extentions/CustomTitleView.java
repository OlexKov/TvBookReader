package com.example.bookreader.extentions;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.leanback.widget.TitleViewAdapter;

import com.example.bookreader.R;

public class CustomTitleView extends FrameLayout implements TitleViewAdapter.Provider {

    private TextView vTitle;

    private TitleViewAdapter titleViewAdapter = new TitleViewAdapter() {
        @Override
        public View getSearchAffordanceView() {
            // Повертаємо null, бо у нас немає кнопки пошуку
            return null;
        }

        @Override
        public void setTitle(CharSequence titleText) {
            CustomTitleView.this.setTitle(titleText);
        }
    };

    public CustomTitleView(Context context) {
        this(context, null, 0);
    }

    public CustomTitleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomTitleView(Context context, AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.custom_header, this, true);
        vTitle = findViewById(R.id.vTitle);

    }

    private void setTitle(CharSequence title) {
        vTitle.setText(title);
    }

    @Override
    public TitleViewAdapter getTitleViewAdapter() {
        return titleViewAdapter;
    }
}
