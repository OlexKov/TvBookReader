package com.example.bookreader.activities;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.R;
import com.example.bookreader.fragments.LoadFilesFragment;
import com.example.bookreader.utility.LocaleHelper;

public class NewFilesActivity extends FragmentActivity {
    private final BookReaderApp app = BookReaderApp.getInstance();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, app.getLocalLanguage()));  // або інша мова
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentManager fm = getSupportFragmentManager();
        setContentView(R.layout.new_files_activity);
        fm.beginTransaction()
                .replace(R.id.load_files, new LoadFilesFragment())
                .commit();

    }
}
