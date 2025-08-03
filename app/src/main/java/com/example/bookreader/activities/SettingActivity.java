package com.example.bookreader.activities;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.fragments.settings.appsettings.SettingRootFragment;
import com.example.bookreader.utility.LocaleHelper;

public class SettingActivity extends FragmentActivity {
    private final BookReaderApp app = BookReaderApp.getInstance();
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, app.getLocalLanguage()));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, new SettingRootFragment())
                    .commit();
        }
    }
}
