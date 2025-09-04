package com.example.bookreader.activities;

import android.content.Context;

import androidx.fragment.app.FragmentActivity;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.utility.LocaleHelper;

public class BaseAppActivity extends FragmentActivity {
    BookReaderApp app = BookReaderApp.getInstance();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, app.getLocalLanguage()));
    }
}
